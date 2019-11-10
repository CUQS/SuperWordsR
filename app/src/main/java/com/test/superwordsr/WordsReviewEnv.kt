package com.test.superwordsr

import ObjBox.ObjectBox
import ObjBox.WordsObjBox
import ObjBox.WordsObjBox_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor

class WordsReviewEnv {
    private lateinit var wordsObjBox: Box<WordsObjBox>  // WordsObjBox
    lateinit var wordsObjList: List<WordsObjBox>  // 全部单词的Obj
    private lateinit var rememberList: MutableList<Int>  // 复习用记忆序列（逐个pop）
    lateinit var saveList: MutableList<Int>  // 用于保存记忆序列（Easy的pop）
    private var remLimitNum: Int = 30  // 记忆数量限制
    var remNow: Int = 0  // 当前记忆位置
    var wordsAllNum: Int = 0  // 单词总量
    private var easyFlag: Boolean = false  // 是否为简单词
    var remNum = 0  // 记住的量（即确认为 easy）
    /**
     * 初始化词库环境
     */
    fun initEnv() {
        wordsObjBox = ObjectBox.boxStore.boxFor()  // 初始化 wordsObjBox
        wordsObjList = wordsObjBox.query().order(WordsObjBox_.wordId).build().find()  // 初始化所有单词的Obj
        rememberList = ArrayList()  // 清空 rememberList
        easyFlag  = false  // 初始化简单词 flag
        wordsAllNum = wordsObjList.size - 1 // 初始化单词总量（去掉第0个Env信息的obj）
        // 如果词库为空初始化词库
        if (wordsAllNum < 1) {
            initWordsObjBoxFirst()
            wordsAllNum = 1
            wordsObjList = wordsObjBox.query().order(WordsObjBox_.wordId).build().find()  // 初始化所有单词的Obj
        }
        // 获取当前记忆位置
        remNow = wordsObjList[0].pronounce.toInt()
        // 取出字符串单词序列
        val stringRememberList = wordsObjList[0].word.split(",")
        // 获取需要复习的单词个数
        val needRemWordsNum = stringRememberList.size
        // String 2 Int
        for (strRem in stringRememberList) {
            if (strRem=="") {
                rememberList.add(1)
                break
            }
            rememberList.add(strRem.toInt())
        }
        // 判断是否够记忆数量，不够填补
        if (needRemWordsNum<=remLimitNum) {
            val addNum = remLimitNum-needRemWordsNum
            for (i in 0 until addNum) {
                if (remNow<wordsAllNum) {
                    remNow += 1
                    if (remNow !in rememberList) {
                        rememberList.add(remNow)
                    }
                }
                else {
                    remNow = 1
                    if (remNow !in rememberList) {
                        rememberList.add(remNow)
                    }
                }
            }
        }
        // 赋值保存序列
        saveList = ArrayList(rememberList)
        // 打乱记忆序列
        rememberList.shuffle()
    }
    /**
     *
     */
    fun step(action: String): String {
        // rememberList中没有单词即结束
        if (rememberList.size==0) return "FINISH"
        // 保存
        if (action=="SAVE") {
            wordsObjList[0].word = saveList.joinToString(",")
            wordsObjList[0].pronounce = remNow.toString()
            wordsObjBox.put(wordsObjList[0])
            return "SAVED!!"
        }
        // 当前单词的全部信息
        if (action=="REMEMBER" || action=="FORGET") return getAllInfo(rememberList[0])
        // 下一个单词信息，如果 easyFlag 则将 saveList 中的对应元素一并删除
        if (action=="NEXT") {
            if (easyFlag) {
                saveList.removeAll { x -> x == rememberList[0] }
                remNum += 1
                easyFlag = false
            }
            rememberList.removeAt(0)
            return if (rememberList.size==0) "FINISH"
            else getRemWord()
        }
        // 将当前单词放入 rememberList的11个以后，下一个单词信息
        if (action=="MISTAKE") {
            if (easyFlag) easyFlag = false
            val rSize = rememberList.size
            if (rSize>11) rememberList.add(11, rememberList[0])
            else rememberList.add(rSize, rememberList[0])
            rememberList.removeAt(0)
            return getRemWord()
        }
        // 单词全部信息
        if (action=="EASY") {
            easyFlag = true
            return getAllInfo(rememberList[0])
        }
        return ""
    }
    /**
     * 设置记忆数量
     */
    fun setRemLimitNum(limitNum: Int): Boolean {
        return if (limitNum>0) {
            remLimitNum = limitNum
            true
        } else {
            false
        }
    }
    /**
     * 返回需要记忆的单词数量，当前记忆位置，单词总量
     */
    fun getEnvAllInfo(): String {
        return "${rememberList.size}, $remNow, $wordsAllNum"
    }
    /**
     * 获取单词
     */
    fun getRemWord(): String {
        return "${wordsObjList[rememberList[0]].word} "
    }
    /**
     * 获取单词的全部信息
     */
    fun getAllInfo(wordIndex: Int): String {
        return "${wordsObjList[wordIndex].word}\n" +
                "${wordsObjList[wordIndex].pronounce}\n" +
                "${wordsObjList[wordIndex].meaning}\n" +
                "${wordsObjList[wordIndex].sentenceJP}\n" +
                "${wordsObjList[wordIndex].sentenceCN} "
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