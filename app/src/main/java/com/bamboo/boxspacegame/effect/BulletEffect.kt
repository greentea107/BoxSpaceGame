package com.bamboo.boxspacegame.effect

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.utils.LogEx

/**
 * 子弹击中物体时的特效
 */
class BulletEffect : BaseEffect() {
    private var currentFrame = 0

    companion object {
        private const val FRAME_COUNT = 15 // 动画的总帧数

        /**
         * 初始化弹痕的Bitmap并缓存
         */
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
                    val unit = AppGobal.unitSize / 3f
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

    /**
     * 根据当前帧的编号绘制动画
     */
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

    /**
     * 播放动画并设置动画播放的坐标
     */
    fun play(x: Float, y: Float) {
        free = false
        this.x = x
        this.y = y
        this.currentFrame = 0
    }

}