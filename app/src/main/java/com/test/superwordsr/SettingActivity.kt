package com.test.superwordsr

import ObjBox.HourlyObjBox
import ObjBox.ObjectBox
import ObjBox.WordsObjBox
import ObjBox.WordsObjBox_
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.SimpleAdapter
import android.widget.Toast
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_setting.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SettingActivity : Activity(), View.OnLongClickListener {

    private lateinit var hourlyObjBox: Box<HourlyObjBox>  // HourlyObjBox
    private lateinit var wordsObjBox: Box<WordsObjBox>  // WordsObjBox
    private val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH", Locale.CHINA)  // 日期格式
    private lateinit var listData: ArrayList<HashMap<String, String>>  // 存ObjectBox中的全部数据
    private var map: HashMap<String, String> = HashMap()  // 每条数据
    private val from = arrayOf("Data")  // map中的数据类型名称
    private val to = intArrayOf(R.id.tvSettingData)  //key所对应的控件id
    private var settingMode = "HourlyObjBox"  // 添加单词模式

    /**
     * 初始化UI
     * 初始化ObjectBox
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initUI()  // 初始化UI
        hourlyObjBox = ObjectBox.boxStore.boxFor()  // 初始化 hourlyObjBox
        wordsObjBox = ObjectBox.boxStore.boxFor()  // 初始化 wordsObjBox
    }
    /**
     * btnSettingAdd
     * btnSettingRemoveAll
     */
    override fun onLongClick(v: View?): Boolean {
        when (v) {
            // 长按添加数据
            btnSettingAdd -> {
                val stringAdd: String = etPutObj.text.toString()
                val stringLines = stringAdd.split("\n")
                if (stringLines.size > 1) {
                    if (settingMode=="HourlyObjBox") {
                        for (line in stringLines) {
                            val hourlyObj = HourlyObjBox()
                            val stringItem = line.split(",")
                            if (stringItem.size!=2) {
                                Toast.makeText(this, "数据格式不匹配", Toast.LENGTH_SHORT).show()
                                break
                            }
                            hourlyObj.pmData = stringItem[0].toInt()
                            hourlyObj.createAt = string2date(stringItem[1])
                            hourlyObjBox.put(hourlyObj)
                        }
                    }
                    if (settingMode=="WordsObjBox") {
                        val wordsObjAll: List<WordsObjBox> = wordsObjBox.query().order(WordsObjBox_.wordId).build().find()
                        var wordsIdNow = 0
                        if (wordsObjAll.isNotEmpty()) wordsIdNow = wordsObjAll[wordsObjAll.size-1].wordId + 1
                        for (line in stringLines) {
                            val wordsObj = WordsObjBox()
                            val stringItem = line.split("##")
                            if (stringItem.size!=5) {
                                Toast.makeText(this, "数据格式不匹配", Toast.LENGTH_SHORT).show()
                                break
                            }
                            wordsObj.wordId = wordsIdNow
                            wordsObj.word = stringItem[0]
                            wordsObj.pronounce = stringItem[1]
                            wordsObj.meaning = stringItem[2]
                            wordsObj.sentenceJP = stringItem[3]
                            wordsObj.sentenceCN = stringItem[4]
                            wordsIdNow += 1
                            wordsObjBox.put(wordsObj)
                        }
                    }
                }
                displayListView(settingMode)
            }
            // 长按删除所有数据
            btnSettingRemoveAll -> {
                if (settingMode=="HourlyObjBox") hourlyObjBox.removeAll()
                if (settingMode=="WordsObjBox") wordsObjBox.removeAll()
                Toast.makeText(this, "删除了 $settingMode 所有数据", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    private fun displayListView(mode:String) {
        if (mode=="HourlyObjBox") {
            val hourlyObjects = hourlyObjBox.all
            listData = ArrayList()
            for (i in 0 until hourlyObjects.size) {
                val createAt = hourlyObjects[i].createAt
                map = HashMap()
                map["Data"] = "${hourlyObjects[i].pmData.toString()}\n${date2string(createAt!!)}"
                listData.add(map)
            }
            listViewSetting.adapter = SimpleAdapter(this, listData, R.layout.listview_setting, from, to)
        }
        if (mode=="WordsObjBox") {
            val wordsObjects = wordsObjBox.all
            listData = ArrayList()
            for (i in 0 until wordsObjects.size) {
                map = HashMap()
                map["Data"] = "${wordsObjects[i].wordId}: ${wordsObjects[i].word}\n" +
                        "${wordsObjects[i].pronounce}\n" +
                        "${wordsObjects[i].meaning}\n" +
                        "${wordsObjects[i].sentenceJP}\n" +
                        "${wordsObjects[i].sentenceCN} "
                listData.add(map)
            }
            listViewSetting.adapter = SimpleAdapter(this, listData, R.layout.listview_setting, from, to)
        }
    }
    /**
     * 初始化控件
     * swSettingMode
     */
    private fun initUI() {
        btnSettingAdd.setOnLongClickListener(this)
        btnSettingRemoveAll.setOnLongClickListener(this)
        // 模式显示
        tvSettingMode.text = settingMode
        // 设置添加单词模式
        swSettingMode.setOnCheckedChangeListener { _, b ->
            if (b) {
                settingMode = "WordsObjBox"
                Toast.makeText(this, "设置为添加 WordsObjBox 数据", Toast.LENGTH_SHORT).show()
            }
            else {
                settingMode = "HourlyObjBox"
                Toast.makeText(this, "设置为添加 HourlyObjBox 数据", Toast.LENGTH_SHORT).show()
            }
            tvSettingMode.text = settingMode
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