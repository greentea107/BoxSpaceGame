package com.bamboo.boxspacegame.view

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.BmpCache
import com.bamboo.boxspacegame.spirit.Player

object MapBackground {
    fun init() {
        val bmp = buildBmp()
        BmpCache.put(BmpCache.BMP_BACKGROUND, bmp)
    }

    private fun buildBmp(): Bitmap {
        // 创建地图的BITMAP
        val bmp = Bitmap.createBitmap(
            AppGobal.screenWidth, AppGobal.screenHeight, Bitmap.Config.ARGB_8888
        )
        // 绘制地图
        Canvas(bmp).apply {
            drawBackground(this)
            drawMapLine(this)
        }
        return bmp
    }

    /**
     * 在地图上绘制线条
     */
    private fun drawMapLine(canvas: Canvas) {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = 1f
        val step = AppGobal.screenWidth / 20f
        paint.color = Color.WHITE
        // 绘制四条往中心的短射线，呈现一种透视效果
        canvas.drawLine(0f, 0f, step, step, paint)
        canvas.drawLine(
            AppGobal.screenWidth - 1f, 0f,
            AppGobal.screenWidth - step - 1f, step,
            paint
        )
        canvas.drawLine(
            0f, AppGobal.screenHeight - 1f,
            step, AppGobal.screenHeight - 1f - step,
            paint
        )
        canvas.drawLine(
            AppGobal.screenWidth - 1f,
            AppGobal.screenHeight - 1f,
            AppGobal.screenWidth - 1f - step,
            AppGobal.screenHeight - 1f - step,
            paint
        )
        paint.color = Color.parseColor("#ECECFF")
        paint.strokeWidth = 0.5f
        canvas.drawRect(
            RectF(
                step + 1f, step + 1f,
                AppGobal.screenWidth - 1f - step,
                AppGobal.screenHeight - 1f - step
            ),
            paint
        )
        paint.color = Color.WHITE
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(
            0f, 0f,
            AppGobal.screenWidth - 1f, AppGobal.screenHeight - 1f, paint
        )
    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        val r = Rect(0, 0, AppGobal.screenWidth, AppGobal.screenHeight)
        canvas.drawRect(r, paint) // 背景黑

        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.parseColor("#5277D0")
        paint.maskFilter = BlurMaskFilter(AppGobal.unitSize / 2, BlurMaskFilter.Blur.OUTER)
        val rin = RectF(
            AppGobal.unitSize,
            AppGobal.unitSize,
            AppGobal.screenWidth - AppGobal.unitSize,
            AppGobal.screenHeight - AppGobal.unitSize,
        )
        canvas.drawRect(rin, paint)
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
    }

    /**
     * 在主循环绘制背景BITMAP
     */
    fun draw(canvas: Canvas) {
        val bmp = BmpCache.get(BmpCache.BMP_BACKGROUND)
        bmp?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        drawMapGrid(canvas)
    }

    private fun drawMapGrid(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.shader = RadialGradient(
            Player.x,
            Player.y,
            AppGobal.screenHeight / 2f,
            intArrayOf(Color.rgb(85, 130, 255), Color.BLACK), null, Shader.TileMode.CLAMP
        )
        repeat(20) {
            val x = it * AppGobal.unitSize + AppGobal.unitSize
            canvas.drawLine(
                x, AppGobal.unitSize,
                x, AppGobal.screenHeight - AppGobal.unitSize,
                paint
            )
            val y = it * AppGobal.unitSize + AppGobal.unitSize
            if (y < AppGobal.screenHeight - AppGobal.unitSize) {
                canvas.drawLine(
                    AppGobal.unitSize, y,
                    AppGobal.screenWidth - AppGobal.unitSize, y,
                    paint
                )
            }
        }
    }
}