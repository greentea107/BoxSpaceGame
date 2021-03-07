package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal

/**
 *  爆雷：全屏清敌
 */
class GrenadeEffect : BaseEffect() {
    private val paint = Paint()
    private var centerX = 0f
    private var centerY = 0f
    private var currentFrame = 0
    private var onFinished: (() -> Unit)? = null // 动画播放完毕后的回调函数

    init {
        paint.style = Paint.Style.FILL_AND_STROKE
    }

    companion object {
        const val FRAME_COUNT = 16
    }

    fun play(centerX: Float, centerY: Float, onFinished: (() -> Unit)?) {
        this.centerX = centerX
        this.centerY = centerY
        this.onFinished = onFinished
        this.free = false
        currentFrame = 0
    }

    override fun draw(canvas: Canvas) {
        currentFrame++
        if (currentFrame >= FRAME_COUNT) {
            this.free = true
            onFinished?.let { it() }
        } else {
            val r = AppGobal.screenWidth / FRAME_COUNT * currentFrame
            paint.shader = RadialGradient(
                centerX,
                centerY,
                r.toFloat(),
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#33333333"),
                    Color.parseColor("#66333333"),
                    Color.parseColor("#66FFFFFF")
                ), null, Shader.TileMode.CLAMP
            )
            paint.color = Color.WHITE
            canvas.drawCircle(centerX, centerY, r.toFloat(), paint)
        }
    }

}