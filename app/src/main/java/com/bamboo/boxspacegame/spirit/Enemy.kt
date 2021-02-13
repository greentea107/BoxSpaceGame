package com.bamboo.boxspacegame.spirit

import android.graphics.*
import androidx.core.graphics.withRotation
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus

class Enemy : BaseSprite() {
    private var bmp: Bitmap? = null
    private val paint = Paint()

    init {
        this.distance = 3f
        this.HP = 10f
        bmp = AppGobal.bmpCache[AppGobal.BMP_ENEMY]
        if (bmp == null) {
            bmp = buildBmp()
        }
    }

    private fun buildBmp(): Bitmap {
        val bmp = Bitmap.createBitmap(
            AppGobal.unitSize.toInt(),
            AppGobal.unitSize.toInt(),
            Bitmap.Config.ARGB_8888
        )
        Canvas(bmp).apply {
            val paint = Paint()
            val path = Path()
            path.moveTo(AppGobal.unitSize / 2, 0f)
            path.lineTo(AppGobal.unitSize, AppGobal.unitSize - AppGobal.unitSize / 3)
            path.lineTo(
                AppGobal.unitSize - AppGobal.unitSize / 3,
                AppGobal.unitSize - AppGobal.unitSize / 3
            )
            path.lineTo(AppGobal.unitSize / 2, AppGobal.unitSize - 1)
            path.lineTo(AppGobal.unitSize / 3, AppGobal.unitSize - AppGobal.unitSize / 3)
            path.lineTo(0f, AppGobal.unitSize - AppGobal.unitSize / 3)
            path.close()
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.shader = RadialGradient(
                AppGobal.unitSize / 2f, 0f, AppGobal.unitSize,
                intArrayOf(Color.WHITE, Color.DKGRAY), null, Shader.TileMode.CLAMP
            )
            this.drawPath(path, paint)
            paint.style = Paint.Style.STROKE
            paint.shader = null
            paint.strokeWidth = 1f
            paint.color = Color.WHITE
            this.drawPath(path, paint)
            this.drawLine(
                AppGobal.unitSize / 2,
                0f,
                AppGobal.unitSize / 2,
                AppGobal.unitSize,
                paint
            )
        }
        AppGobal.bmpCache.put(AppGobal.BMP_ENEMY, bmp)
        return bmp
    }

    override fun move() {
        angle = setAngleByCoord(Player.x, Player.y)
        val pt = MathUtils.getCoordsByAngle(distance, angle.toDouble(), PointF(x, y))
        x = pt.x
        y = pt.y
    }

    override fun draw(canvas: Canvas) {
        bmp?.let {
            canvas.withRotation(
                angle + 90,
                x + AppGobal.unitSize / 2,
                y + AppGobal.unitSize / 2
            ) {
                drawMotion(this)
                drawBitmap(it, x, y, null)
            }
        }
    }

    private fun drawMotion(canvas: Canvas) {
        paint.color = Color.parseColor("#91C8EB")
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 1f
        val cz2 = System.currentTimeMillis() % 12
        paint.maskFilter = BlurMaskFilter(5f + cz2, BlurMaskFilter.Blur.OUTER)
        canvas.drawCircle(
            x + AppGobal.unitSize / 2, y + AppGobal.unitSize / 2,
            AppGobal.unitSize / 2.5f, paint
        )
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawCircle(
            x + AppGobal.unitSize / 2, y + AppGobal.unitSize / 2,
            AppGobal.unitSize / 2.5f, paint
        )
    }

    /**
     * 根据指定的目标位置设置敌人的角度
     */
    fun setAngleByCoord(targetX: Float, targetY: Float): Float {
        return MathUtils.getAngle(x, y, targetX, targetY).toFloat()
    }

    fun getRect() = RectF(x, y, x + AppGobal.unitSize, y + AppGobal.unitSize)

    fun hit() {
        if (this.HP <= 0) {
            this.free = true
            val cx = bmp?.width?.div(2) ?: 0
            val cy = bmp?.height?.div(2) ?: 0
            EffectManager.obtainBomb().play(x + cx, y + cy)
            StageManager.score++
            LiveEventBus.get(AppGobal.EVENT_SCORE).post(StageManager.score)
            LiveEventBus.get(AppGobal.EVENT_BOMB_SFX).post(true)
        } else {
            this.HP -= 2f
        }
    }
}