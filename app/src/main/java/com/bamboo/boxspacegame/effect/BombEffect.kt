package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal

class BombEffect : BaseEffect() {
    private val paint = Paint()
    private var currentFrame = 1
    private var onFinished: (() -> Unit)? = null

    companion object {
        const val FRAME_COUNT = 20
    }

    fun play(x: Float, y: Float, onFinished: (() -> Unit)? = null) {
        this.free = false
        this.x = x
        this.y = y
        this.currentFrame = 1
        this.onFinished = onFinished
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        val inc = AppGobal.unitSize / FRAME_COUNT
        currentFrame++
        if (currentFrame > FRAME_COUNT) {
            currentFrame = 1
            free = true
            onFinished?.let { it() }
        } else {
            paint.style = if (currentFrame >= FRAME_COUNT / 2) Paint.Style.STROKE
            else Paint.Style.FILL
            canvas.drawCircle(x, y, inc * currentFrame, paint)
        }
    }
}