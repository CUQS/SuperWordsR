package com.test.superwordsr

import ObjBox.HourlyObjBox
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

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var hourlyObjBox: Box<HourlyObjBox>  // 小时数据库
    private var floatFlag: Boolean = true  // 是否显示悬浮窗flag
    private lateinit var pmGetTd: Thread  // 爬取pm线程
    private val patternDate = Pattern.compile("（AQI）。 ([-0-9]+)")  // 提取pm数据
    private val patternTime = Pattern.compile("更新时间[^\\d]{0,20}([0-9]+):[0-9]+")  // 提取温度和时间数据
    private var todayPm = -1  // 爬取的pm
    private lateinit var todayPmDate: Date  // 爬取的pm的更新时间
    private var tdFlag = false  // 是否爬取结束标志
    private val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH", Locale.CHINA)  // 日期格式

    /**
     * 初始化UI
     * 初始化ObjectBox
     * 初始化悬浮窗
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()  // 初始化UI
        hourlyObjBox = ObjectBox.boxStore.boxFor()  // 初始化ObjectBox
        checkPermission()  // 初始化悬浮窗
        pmGetTd.start()  // 开启线程
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
     * btnSetting
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
                startActivity(Intent(this, SettingActivity::class.java))
            }
            // 下一个单词
            btnMainNext -> {
                tvMainWordDisplay.text = "pm: $todayPm\ndate: ${date2string(todayPmDate)}"
            }
        }
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
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "回到主页", Toast.LENGTH_SHORT).show()
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
        todayPmDate = string2date("2012-02-22-02")!!
        pmGetTd = Thread {
            val doc: Document = Jsoup.connect("https://aqicn.org/city/beijing/shijingshangucheng/cn/")
                .userAgent("Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79 Safari/535.12")
                .timeout(20000)
                .get()
            val dataAll = doc.text()
            val matcherPm = patternDate.matcher(dataAll)
            val matcherTime = patternTime.matcher(dataAll)
            while (matcherPm.find()) {
                for (i in 1 until matcherPm.groupCount()+1) {
                    val foundPm: String? = matcherPm.group(1)
                    var pmInt: Int?
                    pmInt = foundPm?.toIntOrNull() ?: -1
                    todayPm = pmInt
                }
            }
            var dateString = formatter.format(Date()).substring(0,10)
            var pmTime: Int = -1
            while (matcherTime.find()) {
                for (i in 1 until matcherTime.groupCount()+1) {
                    pmTime = matcherTime.group(1)?.toInt() ?: -1
                }
            }
            dateString = if ((pmTime > -1) && (pmTime < 24))
                String.format("%s-%2d", dateString, pmTime) else "2222-02-22-02"
            todayPmDate = string2date(dateString)!!
            tdFlag = true
        }

    }
    /**
     * 字符串转日期
     */
    private fun string2date(date:String):Date?{
        return formatter.parse(date)
    }
    /**
     * 日期转字符串
     */
    private fun date2string(date:Date):String{
        return formatter.format(date)
    }
}
