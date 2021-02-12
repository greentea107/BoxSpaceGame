package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal

/**
 * 子弹击中物体时的特效
 */
class BulletEffect : BaseEffect() {
    private var currentFrame = 0

    companion object {
        private const val FRAME_COUNT = 15
        fun init() {
            val paint = Paint()
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            repeat(FRAME_COUNT) {
                val bmp = Bitmap.createBitmap(
                    AppGobal.unitSize.toInt(),
                    AppGobal.unitSize.toInt(),
                    Bitmap.Config.ARGB_8888
                )
                Canvas(bmp).apply {
                    val unit = AppGobal.unitSize / 2f
                    paint.alpha = 255 - (255 / FRAME_COUNT * it)
                    paint.shader = RadialGradient(
                        unit, unit, unit + 0.1f,
                        intArrayOf(Color.WHITE, Color.TRANSPARENT), null,
                        Shader.TileMode.CLAMP
                    )
                    this.drawCircle(unit, unit, unit, paint)
                }
                AppGobal.bmpCache.put("bulletEffect_$it", bmp)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        val bmp = AppGobal.bmpCache["bulletEffect_$currentFrame"]
        val ex = x - AppGobal.unitSize / 2
        val ey = y - AppGobal.unitSize / 2
        canvas.drawBitmap(bmp, ex, ey, null)
        currentFrame++
        if (currentFrame >= FRAME_COUNT) {
            currentFrame = 0
            free = true
        }
    }

    fun play(x: Float, y: Float) {
        free = false
        this.x = x
        this.y = y
        this.currentFrame = 0
    }

}