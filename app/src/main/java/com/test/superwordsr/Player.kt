package com.test.superwordsr

import ObjBox.ObjectBox
import ObjBox.WordsObjBox
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import io.objectbox.Box
import io.objectbox.kotlin.boxFor

class Player {
    private var x: Float = 0F
    private var y: Float = 0F

    private val raw: Int = 3
    private val col: Int = 4

    private val raw_all: Int
    private val col_all: Int

    private var startIdx: Int = 0

    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private val screenHeight = Resources.getSystem().displayMetrics.heightPixels

    private val cardWidth = screenWidth / col
    private val cardHeight = screenHeight / raw

    private var wordsObjBox: Box<WordsObjBox>  // WordsObjBox
    private val wordsObjects: List<WordsObjBox>
    private var listData: ArrayList<ArrayList<String>> = ArrayList() // 存当前需要显示的数据
    private val words_num: Int

    private var paint: Paint = Paint()
    private val textHeight: Float

    init {
        paint.color = (0xff000000).toInt()
        paint.textSize = 40F
        textHeight = 40F

        wordsObjBox = ObjectBox.boxStore.boxFor()  // 初始化 wordsObjBox
        wordsObjects = wordsObjBox.all
        words_num = wordsObjects.size
        col_all = 10
        raw_all = words_num / col_all + 1
        getDataByIndex(startIdx)
    }

    /**
     * Draws the object on to the canvas.
     */
    fun draw(canvas: Canvas) {
        val y_idx: Int = startIdx / col_all
        val x_idx: Int = startIdx - y_idx * col_all

        canvas.drawText("eye_x,      eye_y: $x,      $y", 0F, 40F, paint)
        canvas.drawText("x_idx,      y_idx: $x_idx,      $y_idx", 0F, 80F, paint)

        for (r in 0 until raw + 1) {
            for (c in 0 until col + 1) {
                drawText(canvas, listData[r * (col + 1) + c],
                    (x_idx+c) * cardWidth - x, (y_idx+r) * cardHeight - y)
            }
        }
    }

    private fun drawText(canvas: Canvas, strings: ArrayList<String>, posX: Float, posY: Float) {
        for (i in 0 until 5) {
            canvas.drawText(strings[i], posX+textHeight, posY+i*textHeight+textHeight*4, paint)
        }
    }

    /**
     * 更新视角左上角相在全图鉴的位置
     */
    fun updateEyePos(x_move: Float, y_move: Float) {
        val x_t = x - x_move
        val y_t = y - y_move
        if (x_t > 0F && x_t < cardWidth * (col_all - col)) x = x_t
        if (y_t > 0F && y_t < cardHeight * (raw_all - raw)) y = y_t
        val start_t = (x/cardWidth).toInt() + (y/cardHeight).toInt() * col_all
        if (start_t != startIdx) {
            startIdx = start_t
            getDataByIndex(startIdx)
        }
    }

    private fun getDataByIndex(start: Int) {
        // 导入全部数据
        listData.clear()
        listData = ArrayList()
        var count = 0
        var t_idx: Int
        for (r in 0 until (raw + 1)) {
            for (c in 0 until (col + 1)) {
                t_idx = start + c + r * col_all + 1  // 加 1 (首个不是单词)
                val t = ArrayList<String>()
                listData.add(t)
                if (t_idx >= words_num) t_idx = 0
                listData[count].add("${wordsObjects[t_idx].wordId}: ${wordsObjects[t_idx].word}")
                listData[count].add(wordsObjects[t_idx].pronounce)
                listData[count].add(wordsObjects[t_idx].meaning)
                listData[count].add(wordsObjects[t_idx].sentenceJP)
                listData[count].add(wordsObjects[t_idx].sentenceCN)
                count += 1
            }
        }
    }
}