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
    private var onFinished: (() -> Unit)? = null // 动画播放完毕后的回调函数

    companion object {
        const val FRAME_COUNT = 20 // 动画的总帧数
        const val BOMB_STYLE_COUNT = 5// 爆炸样式总数
        var size = SizeF(0f, 0f)

        fun init() {
            // 爆炸的大小为基准单位的2倍
            size = SizeF(AppGobal.unitSize * 2, AppGobal.unitSize * 2)
            buildBmp()
        }

        /**
         * 生成碎片的静态Bitmap
         */
        private fun buildBmp() {
            // 用一个常量循环生成，以造成碎片样式的多样化
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
                    // 在循环体绘制碎片，循环一次就是绘制一个碎片，这里用帧数代表碎片总数
                    repeat(FRAME_COUNT) {
                        // 用随机数绘制碎片的位置和大小，碎片就是一个实心圆
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
                // 对绘制的碎片对象进行缓存
                AppGobal.bmpCache.put("${AppGobal.BMP_BOMB}_$it", bmp)
            }
        }
    }

    init {
        // 对象在实例化时会随机的从缓存中取出碎片Bitmap
        val styleIndex = Random().nextInt(BOMB_STYLE_COUNT)
        bmp = AppGobal.bmpCache["${AppGobal.BMP_BOMB}_$styleIndex"]
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        paint.style = Paint.Style.FILL_AND_STROKE
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
     * 直接在屏幕上绘制爆炸图像，除了碎片外冲击波和光球由代码绘制
     * 此方法每调用一次就是绘制一帧
     */
    override fun draw(canvas: Canvas) {
        val inc = size.width / FRAME_COUNT
        frameIndex++
        // 判断当前绘制的是否是动画的最后帧，最后帧表示动画播放完毕
        if (frameIndex >= FRAME_COUNT) {
            // 播放完毕则将对象设为闲置
            frameIndex = 0
            free = true // 标记为空闲
            // 重新从缓存中随机取出一张碎片Bitmap
            val styleIndex = Random().nextInt(BOMB_STYLE_COUNT)
            bmp = AppGobal.bmpCache["${AppGobal.BMP_BOMB}_$styleIndex"]
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
            paint.alpha = 255 - incStep.toInt()
            paint.shader = null
            canvas.drawCircle(x, y, AppGobal.unitSize / 3, paint)
        }
    }
}