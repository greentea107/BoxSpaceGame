package com.bamboo.boxspacegame.effect

import android.graphics.*
import android.util.SizeF
import com.bamboo.boxspacegame.AppGobal
import com.jeremyliao.liveeventbus.LiveEventBus
import java.util.*

/**
 * 爆炸动画特效
 */
class BombEffect : BaseEffect() {
    private val paint = Paint()
    private var frameIndex = 1
    private var bmp: Bitmap? = null
    private var size = SizeF(0f, 0f)
    private var onFinished: (() -> Unit)? = null // 动画播放完毕后的回调函数

    companion object {
        const val FRAME_COUNT = 20 // 动画的总帧数
        const val BOMB_STYLE_COUNT = 5// 爆炸样式总数
    }

    init {
        val styleIndex = Random().nextInt(BOMB_STYLE_COUNT)
        bmp = AppGobal.bmpCache["${AppGobal.BMP_BOMB}_$styleIndex"]
        if (bmp == null) {
            buildBmp()
            bmp = AppGobal.bmpCache["${AppGobal.BMP_BOMB}_$styleIndex"]
        }
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        paint.style = Paint.Style.FILL_AND_STROKE
    }

    private fun buildBmp() {
        size = SizeF(AppGobal.unitSize * 2, AppGobal.unitSize * 2)
        repeat(BOMB_STYLE_COUNT) {
            val bmp = Bitmap.createBitmap(
                size.width.toInt(),
                size.height.toInt(),
                Bitmap.Config.ARGB_8888
            )
            Canvas(bmp).apply {
                val paint = Paint()
                paint.color = Color.WHITE
                paint.style = Paint.Style.FILL_AND_STROKE
                repeat(FRAME_COUNT) {
                    val x = Random().nextInt(bmp.width).toFloat()
                    val y = Random().nextInt(bmp.height).toFloat()
                    val r = Random().nextInt(2) + 2f
                    paint.shader =
                        RadialGradient(
                            x, y, r,
                            intArrayOf(
                                Color.WHITE,
                                Color.parseColor("#33FFFFFF")
                            ),
                            null, Shader.TileMode.CLAMP
                        )
                    drawCircle(x, y, r, paint)
                }
            }
            AppGobal.bmpCache.put("${AppGobal.BMP_BOMB}_$it", bmp)
        }
    }

    /**
     * 播放动画
     * @param onFinished 动画播放完毕后的回调函数，默认可以不传
     */
    fun play(x: Float, y: Float, onFinished: (() -> Unit)? = null) {
        this.free = false
        this.x = x
        this.y = y
        this.frameIndex = 0
        this.onFinished = onFinished
        // 通过事件机制播放音效
        LiveEventBus.get(AppGobal.EVENT_BOMB_SFX).post(true)
    }

    /**
     * 直接在屏幕上绘制图像，并不在游戏初始化时缓存
     */
    override fun draw(canvas: Canvas) {
        val inc = size.width / FRAME_COUNT
        frameIndex++
        // 判断当前绘制的是否是动画的最后帧，最后帧表示动画播放完毕
        if (frameIndex >= FRAME_COUNT) {
            // 播放完毕则将对象设为闲置
            frameIndex = 0
            free = true
            onFinished?.let { it() } // 调用完毕后的响应方法
        } else {
            // 绘制冲击波效果
            val incStep = inc * frameIndex
            paint.alpha = 255 - incStep.toInt()
            paint.shader = RadialGradient(
                x, y, incStep, intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#66FFFFFF")
                ), null, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(x, y, incStep, paint)
            // 绘制碎片效果
            bmp?.let {
                canvas.drawBitmap(
                    it, Rect(0, 0, it.width, it.height),
                    RectF(x - incStep, y - incStep, x + incStep, y + incStep),
                    null
                )
            }
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.alpha = 200
            paint.shader = null
            canvas.drawCircle(x, y, AppGobal.unitSize / 3, paint)
        }
    }
}