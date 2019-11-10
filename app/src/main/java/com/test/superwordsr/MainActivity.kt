package com.test.superwordsr

import ObjBox.HourlyObjBox
import ObjBox.HourlyObjBox_
import ObjBox.ObjectBox
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnInvokeView
import com.lzf.easyfloat.permission.PermissionUtils
import com.squareup.picasso.Picasso
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.Window
import io.objectbox.query.QueryBuilder


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    private lateinit var hourlyObjBox: Box<HourlyObjBox>  // 小时数据库
    private var floatFlag: Boolean = true  // 是否显示悬浮窗flag
    private lateinit var pmGetTd: Thread  // 爬取pm线程
    private val patternDate = Pattern.compile("（AQI）。 ([-0-9]+)")  // 提取pm数据
    private val patternTime = Pattern.compile("更新时间[^\\d]{0,20}([0-9]+):[0-9]+")  // 提取温度和时间数据
    private var hourlyObjNow = HourlyObjBox()  // 存储当前小时的obj数据
    private var tdFlag = true  // 是否爬取结束标志
    private lateinit var wordsReviewEnv: WordsReviewEnv  // 记单词环境框架
    private var pmTxtDrawFlag = true  // 是否绘制了pm
    private var jsoupFlag = false  // jsoup是否完成爬取
    // 画笔绘pm数据
    private lateinit var baseBitmap: Bitmap
    private lateinit var pmCanvas: Canvas
    private lateinit var paint: Paint

    /**
     * 初始化UI
     * 初始化ObjectBox
     * 初始化悬浮窗
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)  // 去除title
        setContentView(R.layout.activity_main)
        initUI()  // 初始化UI
        hourlyObjBox = ObjectBox.boxStore.boxFor()  // 初始化ObjectBox
        wordsReviewEnv = WordsReviewEnv()  // 初始化记单词环境框架
        checkPermission()  // 初始化悬浮窗
        val dateNow = Date()
        val hourlyObjNowTemp = hourlyObjBox.query().order(HourlyObjBox_.createAt, QueryBuilder.DESCENDING).build().findFirst()
        if (hourlyObjNowTemp!=null) {
            val timeDiff = getHourDiff(dateNow, hourlyObjNowTemp.createAt!!)
            if (timeDiff in 0..0) {
                tvMainHourly.text = "have data in time"
                tdFlag = false
                hourlyObjNow.pmData = hourlyObjNowTemp.pmData
                hourlyObjNow.createAt = hourlyObjNowTemp.createAt!!
            }
        }
        if (tdFlag) {
            tvMainHourly.text = "loading..."
            pmGetTd.start()
        }  // 开启线程
        // 作pm图
        initialPmCanvas()
    }
    /**
     * 退出时dismiss悬浮窗
     */
    override fun onDestroy() {
        EasyFloat.dismissAppFloat(this)
        super.onDestroy()
    }
    /**
     * btnFloatShow
     * btnMain2Setting
     * btnMainNext、btnMainBack、btnMainEasy、btnMainSave
     */
    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v) {
            // 显示和隐藏悬浮窗按钮
            btnFloatShow -> {
                if (!floatFlag) EasyFloat.showAppFloat(this)
                else EasyFloat.hideAppFloat(this)
                floatFlag = !floatFlag
            }
            // 启动SettingActivity
            btnMain2Setting -> {
                startActivity(Intent(this, WordsActivity::class.java))
            }
            // 下一个单词按钮
            btnMainNext -> {
                when (btnMainNext.text) {
                    // 初始化记单词框架
                    "START" -> {
                        wordsReviewEnv.initEnv()
                        tvMainWordDisplay.textSize = 26.0F
                        tvMainWordDisplay.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        tvMainWordDisplay.text = wordsReviewEnv.getRemWord()
                        btnMainNext.text = "REMEMBER"
                        btnMainBack.text = "FORGET"
                        btnMainSave.isEnabled = true
                        btnMainBack.isEnabled = true
                        btnMainEasy.isEnabled = true
                        tvMainEnvInfo.text = wordsReviewEnv.getEnvAllInfo()
                    }
                    // 记得
                    "REMEMBER" -> {
                        tvMainWordDisplay.textSize = 18.0F
                        tvMainWordDisplay.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                        tvMainWordDisplay.text = wordsReviewEnv.step("REMEMBER")
                        btnMainNext.text = "NEXT"
                        btnMainBack.text = "MISTAKE"
                        btnMainEasy.isEnabled = false
                    }
                    // 下一个
                    "NEXT" -> {
                        tvMainWordDisplay.textSize = 26.0F
                        tvMainWordDisplay.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        tvMainWordDisplay.text = wordsReviewEnv.step("NEXT")
                        if (tvMainWordDisplay.text == "FINISH") {
                            btnMainEasy.isEnabled = false
                            btnMainNext.isEnabled = true
                            btnMainBack.isEnabled = false
                            btnMainNext.text = "START"
                        }
                        else {
                            btnMainNext.text = "REMEMBER"
                            btnMainBack.text = "FORGET"
                            btnMainEasy.isEnabled = true
                        }
                        tvMainEnvInfo.text = wordsReviewEnv.getEnvAllInfo()
                    }
                }
                initialPmCanvas()
            }
            // 上一个单词按钮
            btnMainBack -> {
                when (btnMainBack.text) {
                    // 忘记
                    "FORGET" -> {
                        tvMainWordDisplay.textSize = 18.0F
                        tvMainWordDisplay.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                        tvMainWordDisplay.text = wordsReviewEnv.step("FORGET")
                        btnMainNext.text = "NEXT"
                        btnMainBack.text = "MISTAKE"
                        btnMainEasy.isEnabled = false
                    }
                    // 误操作
                    "MISTAKE" -> {
                        tvMainWordDisplay.textSize = 26.0F
                        tvMainWordDisplay.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        tvMainWordDisplay.text = wordsReviewEnv.step("MISTAKE")
                        btnMainNext.text = "REMEMBER"
                        btnMainBack.text = "FORGET"
                        btnMainEasy.isEnabled = true
                    }
                }
                initialPmCanvas()
            }
            // Easy 按钮
            btnMainEasy -> {
                tvMainWordDisplay.textSize = 18.0F
                tvMainWordDisplay.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                tvMainWordDisplay.text = wordsReviewEnv.step("EASY")
                btnMainNext.text = "NEXT"
                btnMainBack.text = "MISTAKE"
                btnMainEasy.isEnabled = false
            }
            // 保存
            btnMainSave -> {
                tvMainWordDisplay.text = wordsReviewEnv.step("SAVE")
                if (tdFlag) {
                    val dateString = formatter.format(Date()).substring(0,13)
                    hourlyObjNow.wordsRemember = wordsReviewEnv.remNum
                    hourlyObjNow.createAt = string2date(dateString)
                    hourlyObjBox.put(hourlyObjNow)
                }
                else {
                    hourlyObjNow.wordsRemember = wordsReviewEnv.remNum
                    hourlyObjBox.put(hourlyObjNow)
                }
            }
        }
    }
    /**
     * btnMain2Setting
     */
    override fun onLongClick(v: View?): Boolean {
        when (v) {
            btnMain2Setting -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
        }
        return true
    }
    /**
     * 显示悬浮窗
     */
    private fun showAppFloat() {
        EasyFloat.with(this)
            .setShowPattern(ShowPattern.ALL_TIME)
            .setGravity(Gravity.CENTER)
            .setSidePattern(SidePattern.RESULT_SIDE)
            .setLayout(R.layout.float_layout, OnInvokeView {
                // 悬浮窗的点击事件
                it.findViewById<ImageView>(R.id.imgViewFloat).setOnClickListener {
                    startActivity(Intent(this, WordsActivity::class.java))
                    Toast.makeText(this, "添加单词", Toast.LENGTH_SHORT).show()
                }
                // load the image with Picasso
                Picasso.get().load(R.drawable.pokemon).into(it.findViewById<ImageView>(R.id.imgViewFloat))
            })
            .show()
    }
    /**
     * 检测浮窗权限是否开启，若没有给与申请提示框（非必须，申请依旧是EasyFloat内部内保进行）
     */
    private fun checkPermission() {
        if (PermissionUtils.checkPermission(this)) {
            showAppFloat()
        } else {
            AlertDialog.Builder(this)
                .setMessage("使用浮窗功能，需要您授权悬浮窗权限。")
                .setPositiveButton("去开启") { _, _ ->
                    showAppFloat()
                }
                .setNegativeButton("取消") { _, _ -> }
                .show()
        }
    }
    /**
     * 初始化控件
     * 初始化默认日期
     * 初始化pmGet线程
     */
    private fun initUI() {
        btnFloatShow.setOnClickListener(this)
        btnMain2Setting.setOnClickListener(this)
        btnMainNext.setOnClickListener(this)
        btnMainBack.setOnClickListener(this)
        btnMainEasy.setOnClickListener(this)
        btnMainSave.setOnClickListener(this)
        btnMain2Setting.setOnLongClickListener(this)
        btnMainSave.isEnabled = false
        btnMainBack.isEnabled = false
        btnMainEasy.isEnabled = false
        hourlyObjNow.createAt = Date()
        pmGetTd = Thread {
            val doc: Document = Jsoup.connect("https://aqicn.org/city/beijing/shijingshangucheng/cn/")
                .userAgent("Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/535.12")
                .timeout(0)
                .get()
            val dataAll = doc.text()
            val matcherPm = patternDate.matcher(dataAll)
            val matcherTime = patternTime.matcher(dataAll)
            while (matcherPm.find()) {
                for (i in 1 until matcherPm.groupCount()+1) {
                    val foundPm: String? = matcherPm.group(1)
                    var pmInt: Int?
                    pmInt = foundPm?.toIntOrNull() ?: 0
                    runOnUiThread { hourlyObjNow.pmData = pmInt }
                }
            }
            var dateString = formatter.format(Date()).substring(0,10)
            var pmTime: Int = -1
            while (matcherTime.find()) {
                for (i in 1 until matcherTime.groupCount()+1) {
                    pmTime = matcherTime.group(1)?.toInt() ?: -1
                }
            }
            runOnUiThread {
                if ((pmTime > -1) && (pmTime < 24)) {
                    dateString = String.format("%s-%2d", dateString, pmTime)
                    hourlyObjNow.createAt = string2date(dateString)!!
                }
                jsoupFlag = true
            }
        }
    }
    /**
     * 绘制pm数据
     */
    @SuppressLint("SetTextI18n")
    private fun initialPmCanvas() {
        if (jsoupFlag && !pmTxtDrawFlag) {
            jsoupFlag = false
            pmTxtDrawFlag = true
        }
        if (pmTxtDrawFlag) {
            if (hourlyObjNow.pmData>0) tvMainHourly.text = "${hourlyObjNow.pmData}: ${date2string(hourlyObjNow.createAt!!)}"
            val baseX = 180f
            val d = 15f
            val gap = 4
            // 笔触宽度为5，颜色为红色
            paint = Paint()
            paint.color = (0xffc00000).toInt()
            baseBitmap = Bitmap.createBitmap(180, 180, Bitmap.Config.ARGB_8888)
            pmCanvas = Canvas(baseBitmap)
            pmCanvas.drawColor(0x00000000)
            val hourlyObjAll = hourlyObjBox.query().order(HourlyObjBox_.createAt).build().find()
            var count = hourlyObjAll.size
            var timeGap = 0
            var paintFlag = false
            var pmDataPaint = 0f
            var createAtPaint: Date
            var dateNowTemp = Date()
            for (j in 0 until 2) {
                for (i in 0 until 12) {
                    if (timeGap>0) timeGap -= 1
                    if (count>0 && !paintFlag) {
                        count -= 1
                        if (j==0 && i==0) {
                            pmDataPaint = hourlyObjNow.pmData.toFloat()
                            createAtPaint = dateNowTemp
                            count += 1
                        }
                        else {
                            pmDataPaint = hourlyObjAll[count].pmData.toFloat()
                            createAtPaint = hourlyObjAll[count].createAt!!
                        }
                        val hourDiff = getHourDiff(dateNowTemp, createAtPaint)
                        dateNowTemp = createAtPaint
                        if (hourDiff >= 1) timeGap = (hourDiff-1).toInt()
                        paintFlag = true
                    }
                    if (count == 0) pmDataPaint = 0f
                    var pmDataTemp = pmDataPaint
                    if (timeGap==0) {
                        paintFlag = false
                        if (pmDataTemp>0f) {
                            if (pmDataTemp<=50f) paint.color = 0xff007d00.toInt()
                            if (pmDataTemp in 51f..100f) paint.color = 0xffc1c742.toInt()
                            if (pmDataTemp in 101f..150f) paint.color = 0xffb35d00.toInt()
                            if (pmDataTemp in 151f..200f) paint.color = 0xffc00000.toInt()
                            if (pmDataTemp in 201f..300f) paint.color = 0xff400093.toInt()
                            if (300f<pmDataTemp) paint.color = 0xff5c1919.toInt()
                        }
                        else {
                            pmDataTemp = 15f
                            paint.color = 0xffffffff.toInt()
                        }
                    }
                    else {
                        pmDataTemp = 15f
                        paint.color = 0xffa9a9a9.toInt()
                    }
                    if (pmDataTemp >= 300f) pmDataTemp = 300f
                    pmDataTemp /= 5f
                    val rect = if (j == 0)
                        RectF(baseX - (i + 1) * d,140f - pmDataTemp,baseX - i * d - gap,140f)  // 第二行
                    else
                        RectF(baseX - (i + 1) * d,60f - pmDataTemp,baseX - i * d - gap,60f)  // 第一行
                    pmCanvas.drawRect(rect, paint)
                }
            }
            imgPm.setImageBitmap(baseBitmap)
            pmTxtDrawFlag = false
        }
    }

}
