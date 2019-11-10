package com.test.superwordsr

import ObjBox.ObjectBox
import ObjBox.WordsObjBox
import ObjBox.WordsObjBox_
import android.app.Activity
import android.os.Bundle
import android.view.View
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_words.*
import ListViewControl.CheckBoxAdapter
import ListViewControl.ViewHolder
import android.view.Gravity
import android.widget.Toast


class WordsActivity: Activity(), View.OnLongClickListener, View.OnClickListener {

    private lateinit var wordsObjBox: Box<WordsObjBox>  // WordsObjBox
    private lateinit var reviewListHash: ArrayList<HashMap<String,Any>>  // 词条哈希表
    private var displayPos = 1  // 词条显示页数
    private val displayNum = 20  // 词条每页显示个数
    private var addWordFlag = true  // 添加了单词重新初始化单词环境框架
    private lateinit var wordsReviewEnv: WordsReviewEnv  // 记单词环境框架
    private lateinit var cbAdapter: CheckBoxAdapter  // listView显示用

    /**
     * 初始化 UI
     * 初始化 ObjectBox
     * 初始化 WordsReviewEnv
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words)
        initUI()  // 初始化UI
        wordsObjBox = ObjectBox.boxStore.boxFor()  // 初始化 wordsObjBox
        wordsReviewEnv = WordsReviewEnv()  // 初始化记单词环境框架
        displayListView()  // 显示词条
    }
    /**
     * btnWordsAdd
     */
    override fun onLongClick(v: View?): Boolean {
        when (v) {
            btnWordsAdd -> {
                val wordsObjAll: List<WordsObjBox> = wordsObjBox.query().order(WordsObjBox_.wordId).build().find()
                var wordsIdNow = 2
                if (wordsObjAll.isNotEmpty()) wordsIdNow = wordsObjAll[wordsObjAll.size-1].wordId + 1
                else initWordsObjBoxFirst()
                val wordsObj = WordsObjBox()
                wordsObj.wordId = wordsIdNow
                wordsObj.word = tvWords1.text.toString()
                wordsObj.pronounce = tvWords2.text.toString()
                wordsObj.meaning = tvWords3.text.toString()
                wordsObj.sentenceJP = tvWords4.text.toString()
                wordsObj.sentenceCN = tvWords4.text.toString()
                wordsObjBox.put(wordsObj)
                tvWords1.setText("")
                tvWords2.setText("")
                tvWords3.setText("")
                tvWords4.setText("・")
                tvWords5.setText("・")
                addWordFlag = true
                displayListView()
            }
        }
        return true
    }
    /**
     * btnWordsExit
     */
    override fun onClick(v: View?) {
        when (v) {
            btnWordsExit -> {
                finish()
            }
            btnWordsNext -> {
                if ((wordsReviewEnv.wordsAllNum-displayPos*displayNum)>0) {
                    displayPos += 1
                    displayListView()
                }
            }
            btnWordsBack -> {
                if (displayPos>1) {
                    displayPos -= 1
                    displayListView()
                }
            }
        }
    }
    /**
     * 初始化控件
     * listViewWords
     */
    private fun initUI() {
        btnWordsExit.setOnClickListener(this)
        btnWordsAdd.setOnLongClickListener(this)
        btnWordsNext.setOnClickListener(this)
        btnWordsBack.setOnClickListener(this)
        listViewWords.setOnItemClickListener { _, view, position, _ ->
            val clickIndex = (displayPos-1)*displayNum+position+1
            val wordsAllInfo = wordsReviewEnv.getAllInfo(clickIndex)
            val toast: Toast = Toast.makeText(this, wordsAllInfo, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            val holder: ViewHolder = view.tag as ViewHolder
            if (holder.cb.isChecked) {
                if (clickIndex in wordsReviewEnv.saveList) wordsReviewEnv.saveList.removeAll {x -> x == clickIndex}
            }
            else {
                if (clickIndex !in wordsReviewEnv.saveList) wordsReviewEnv.saveList.add(clickIndex)
            }
            holder.cb.toggle()
            reviewListHash[position]["boolean"] = holder.cb.isChecked
            wordsReviewEnv.step("SAVE")
            cbAdapter.notifyDataSetChanged()
        }
        listViewWords.setOnItemLongClickListener { _, _, position, _ ->
            if (wordsReviewEnv.remNow>=(displayPos-1)*displayNum+1 && wordsReviewEnv.remNow<displayPos*displayNum+1) {
                reviewListHash[wordsReviewEnv.remNow-displayNum*(displayPos-1)-1]["color"] = "TRANSPARENT"
            }
            wordsReviewEnv.remNow = (displayPos-1)*displayNum+position+1
            wordsReviewEnv.step("SAVE")
            reviewListHash[position]["color"] = "MAGENTA"
            cbAdapter.notifyDataSetChanged()
            true
        }
    }
    /**
     * 显示ListView
     */
    private fun displayListView() {
        if (addWordFlag) {
            wordsReviewEnv.initEnv()
            wordsReviewEnv.step("SAVE")
            addWordFlag = false
        }
        reviewListHash = ArrayList()
        for (i in displayNum*(displayPos-1)+1 until displayPos*displayNum+1) {
            if (i<=wordsReviewEnv.wordsAllNum) {
                val map: HashMap<String, Any> = HashMap()
                map["name"] = wordsReviewEnv.wordsObjList[i].word
                map["boolean"] = i in wordsReviewEnv.saveList
                if (i==wordsReviewEnv.remNow) map["color"] = "MAGENTA"
                else map["color"] = "TRANSPARENT"
                reviewListHash.add(map)
            }
            else break
        }
        cbAdapter = CheckBoxAdapter(this, reviewListHash)
        listViewWords.adapter = cbAdapter
    }
    /**
     * 添加 WordsObjBox 的首个obj作为记忆环境配置
     */
    private fun initWordsObjBoxFirst() {
        val wordsObjZero = WordsObjBox()
        wordsObjZero.wordId = 0
        wordsObjZero.word = "1"  // 记忆序列
        wordsObjZero.pronounce = "1"  // 当前记忆wordId位置
        wordsObjZero.meaning = ""
        wordsObjZero.sentenceJP = ""
        wordsObjZero.sentenceCN = ""
        wordsObjBox.put(wordsObjZero)
        // 添加第一个单词
        val wordsObjFirst = WordsObjBox()
        wordsObjFirst.wordId = 1
        wordsObjFirst.word = "単語"  // 记忆序列
        wordsObjFirst.pronounce = "発音"  // 当前记忆wordId位置
        wordsObjFirst.meaning = "意味"
        wordsObjFirst.sentenceJP = "例JP"
        wordsObjFirst.sentenceCN = "例CN"
        wordsObjBox.put(wordsObjFirst)
    }
}