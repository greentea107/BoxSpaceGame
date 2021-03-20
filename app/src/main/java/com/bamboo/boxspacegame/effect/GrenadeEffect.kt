package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.BmpCache

/**
 *  爆雷：全屏清敌
 */
class GrenadeEffect : BaseEffect() {
    private val paint = Paint()
    private var centerX = 0f
    private var centerY = 0f
    private var currentFrame = 0
    private var onFinished: (() -> Unit)? = null // 动画播放完毕后的回调函数

    companion object {
        const val FRAME_COUNT = 16
        fun init() {
            val paint = Paint()
            paint.color = Color.WHITE
            repeat(FRAME_COUNT) {
                val bmp = Bitmap.createBitmap(
                    AppGobal.screenWidth,
                    AppGobal.screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                Canvas(bmp).apply {
                    paint.maskFilter = BlurMaskFilter(
                        AppGobal.unitSize + it * it,
                        BlurMaskFilter.Blur.SOLID
                    )
                    this.drawCircle(
                        bmp.width / 2f,
                        bmp.height / 2f,
                        AppGobal.screenHeight / 3f,
                        paint
                    )
                }
                BmpCache.put(BmpCache.BMP_GRENADE + "_$it", bmp)
            }
        }
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
            paint.style = Paint.Style.FILL
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
            canvas.drawCircle(centerX, centerY, r.toFloat(), paint)
            val bmp = BmpCache.get(BmpCache.BMP_GRENADE + "_$currentFrame")
            bmp?.let {
                val cx = centerX - (it.width / 2)
                val cy = centerY - (it.height / 2)
                canvas.drawBitmap(it, cx, cy, null)
            }
        }
    }

}