package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.BmpCache

/**
 * 瞬移的动画特效
 * 动画可以正序或倒序播放
 */
class FlashEffect : BaseEffect() {
    private var isInvert: Boolean = false // 判断动画是正序播放还是倒序播放
    private var currentFrame = 0
    private var onFinished: (() -> Unit)? = null // 动画播放完毕后的回调函数

    companion object {
        private const val FRAME_COUNT = 30

        fun init() {
            val unit = AppGobal.unitSize / 2
            val paint = Paint()
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 1f
            repeat(FRAME_COUNT) {
                // 绘制瞬移的各帧图像
                val bmp = Bitmap.createBitmap(
                    (AppGobal.unitSize * 2).toInt(),
                    (AppGobal.unitSize * 2).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                Canvas(bmp).apply {
                    paint.shader = RadialGradient(
                        unit, unit, unit,
                        intArrayOf(
                            Color.parseColor("#33FFFFFF"),
                            Color.parseColor("#66FFFFFF"),
                            Color.WHITE,
                        ), null,
                        Shader.TileMode.CLAMP
                    )
                    val step = (unit / FRAME_COUNT) * it
                    this.drawCircle(unit, unit, unit - step, paint)
                    paint.shader = RadialGradient(
                        unit, unit, unit,
                        intArrayOf(Color.WHITE, Color.parseColor("#33FFFFFF")), null,
                        Shader.TileMode.CLAMP
                    )
                    this.drawOval(
                        unit - (unit + step), unit - 2,
                        unit + (unit + step) - 1, unit + 2,
                        paint
                    )
                    this.drawOval(unit - 1, unit - 6, unit + 1, unit + 6, paint)
                }
                BmpCache.put(BmpCache.BMP_FLASH + "_$it", bmp)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        val bmp = BmpCache.get(BmpCache.BMP_FLASH + "_$currentFrame")
        bmp?.let { canvas.drawBitmap(it, x, y, null) }
        if (isInvert) {
            currentFrame--
            if (currentFrame < 0) {
                currentFrame = 15
                free = true
                onFinished?.let { it() }
            }
        } else {
            currentFrame++
            if (currentFrame >= FRAME_COUNT) {
                currentFrame = 0
                free = true
                onFinished?.let { it() }
            }
        }
    }

    /**
     * 播放瞬移动画
     * @param isInvert 是否倒置播放动画
     * @param onFinished 动画播放完毕后响应
     */
    fun play(x: Float, y: Float, isInvert: Boolean = false, onFinished: (() -> Unit)? = null) {
        this.x = x
        this.y = y
        this.free = false
        this.currentFrame = if (isInvert) FRAME_COUNT - 1 else 0
        this.isInvert = isInvert
        this.onFinished = onFinished
    }
}