package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.utils.LogEx

/**
 * 爆炸动画特效
 */
class BombEffect : BaseEffect() {
    private val paint = Paint()
    private var currentFrame = 1
    private var onFinished: (() -> Unit)? = null // 动画播放完毕后的回调函数

    companion object {
        const val FRAME_COUNT = 20 // 动画的总帧数
    }

    /**
     * 播放动画
     * @param onFinished 动画播放完毕后的回调函数，默认可以不传
     */
    fun play(x: Float, y: Float, onFinished: (() -> Unit)? = null) {
        this.free = false
        this.x = x
        this.y = y
        this.currentFrame = 0
        this.onFinished = onFinished
    }

    /**
     * 直接在屏幕上绘制图像，并不在游戏初始化时缓存
     */
    override fun draw(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        val inc = AppGobal.unitSize / FRAME_COUNT
        currentFrame++
        if (currentFrame >= FRAME_COUNT) {
            currentFrame = 0
            free = true
            onFinished?.let { it() }
        } else {
            if (currentFrame >= FRAME_COUNT / 2) {
                paint.style = Paint.Style.STROKE
                paint.alpha = 255
            } else {
                paint.style = Paint.Style.FILL
                paint.alpha = 255 - (255 / FRAME_COUNT * 2 * currentFrame)
            }
            canvas.drawCircle(x, y, inc * currentFrame, paint)
        }
    }
}