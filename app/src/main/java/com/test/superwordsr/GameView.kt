package com.test.superwordsr

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView (context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val thread: GameThread
    private var player: Player? = null

    private var x_move: Float = 0F
    private var y_move: Float = 0F

    private var touched: Boolean = false
    private var touched_x: Float = 0F
    private var touched_y: Float = 0F

    private var touched_start_x: Float = 0F
    private var touched_start_y: Float = 0F

    init {
        // add callback
        holder.addCallback(this)
        // instantiate the game thread
        thread = GameThread(holder, this)
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        player = Player()
        // start the game thread
        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        var retry = true
        while (retry) {
            try {
                thread.setRunning(false)
                thread.join()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            retry = false
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    /**
     * Function to update the positions of player and game objects
     */
    fun update() {
        if (touched) {
            player!!.updateEyePos(x_move, y_move)
        }
    }

    /**
     * Everything that has to be drawn on Canvas
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor((0xffc7edcc).toInt())
        player!!.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // when ever there is a touch on the screen,
        // we can get the position of touch
        // which we may use it for tracking some of the game objects
        touched_x = event.x
        touched_y = event.y

        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                touched_start_x = touched_x
                touched_start_y = touched_y
            }
            MotionEvent.ACTION_MOVE -> {
                touched = true
                x_move = touched_x - touched_start_x
                y_move = touched_y - touched_start_y
                touched_start_x = touched_x
                touched_start_y = touched_y
            }
            MotionEvent.ACTION_UP -> touched = false
            MotionEvent.ACTION_CANCEL -> touched = false
            MotionEvent.ACTION_OUTSIDE -> touched = false
        }
        return true
    }
}