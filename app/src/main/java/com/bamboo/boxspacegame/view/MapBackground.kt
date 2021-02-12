package com.bamboo.boxspacegame.view

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal

object MapBackground {
    fun init() {
        val bmp = buildBmp()
        AppGobal.bmpCache.put("background", bmp)
    }

    private fun buildBmp(): Bitmap {
        // 创建地图的BITMAP
        val bmp = Bitmap.createBitmap(
            AppGobal.screenWidth, AppGobal.screenHeight, Bitmap.Config.ARGB_8888
        )
        // 绘制地图
        Canvas(bmp).apply {
            val paint = Paint()
            drawBlackBgWhiteBorder(this, paint)
            drawMapLine(this, paint)
        }
        return bmp
    }

    /**
     * 在地图上绘制线条
     */
    private fun drawMapLine(canvas: Canvas, paint: Paint) {
        val step = AppGobal.screenWidth / 20f
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
        canvas.drawRect(
            RectF(
                step, step,
                AppGobal.screenWidth - 1f - step,
                AppGobal.screenHeight - 1f - step
            ),
            paint
        )
    }

    private fun drawBlackBgWhiteBorder(canvas: Canvas, paint: Paint) {
        paint.color = Color.BLACK
        val r = Rect(0, 0, AppGobal.screenWidth, AppGobal.screenHeight)
        canvas.drawRect(r, paint) // 背景黑
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        // 屏幕白框
        canvas.drawRect(
            0f, 0f,
            AppGobal.screenWidth - 1f, AppGobal.screenHeight - 1f,
            paint
        )
    }

    /**
     * 在主循环绘制背景BITMAP
     */
    fun draw(canvas: Canvas?) {
        val bg = AppGobal.bmpCache["background"]
        bg?.let { canvas?.drawBitmap(it, 0f, 0f, null) }
    }
}