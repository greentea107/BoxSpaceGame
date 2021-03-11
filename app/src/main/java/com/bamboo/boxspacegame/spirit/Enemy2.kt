package com.bamboo.boxspacegame.spirit

import android.graphics.*
import androidx.core.graphics.withRotation
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import java.util.*

class Enemy2 : Enemy() {
    private var bmpEnemy: Bitmap? = null
    private val paint = Paint()
    private var lastAttackTime = 0L // 上次开火的时间
    private var attackDelay = 1500L // 开火的时间间隔

    init {
        this.distance = 5f
        this.HP = 10f
        bmpEnemy = AppGobal.bmpCache[AppGobal.BMP_ENEMY_2]
        if (bmpEnemy == null) buildBmp()
    }

    /**
     * 绘制敌人的图像到Bitmap并缓存
     */
    private fun buildBmp() {
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
                AppGobal.unitSize / 2f,
                0f,
                AppGobal.unitSize,
                intArrayOf(
                    Color.parseColor("#D15C53"),
                    Color.parseColor("#3C2827")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            this.drawPath(path, paint)
            paint.style = Paint.Style.STROKE
            paint.shader = RadialGradient(
                AppGobal.unitSize / 2f,
                0f,
                AppGobal.unitSize,
                intArrayOf(
                    Color.rgb(255, 235, 225),
                    Color.rgb(136, 55, 78)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            paint.strokeWidth = 1f
            this.drawLine(
                AppGobal.unitSize / 2,
                0f,
                AppGobal.unitSize / 2,
                AppGobal.unitSize,
                paint
            )
            paint.shader = null
            paint.color = Color.rgb(255, 180, 180)
            this.drawPath(path, paint)
        }
        AppGobal.bmpCache.put(AppGobal.BMP_ENEMY_2, bmp)
        this.bmpEnemy = bmp
    }

    /**
     * 计算移动后的坐标
     */
    @Synchronized
    override fun move() {
        angle = setAngleByCoord(Player.x, Player.y)
        val pt = MathUtils.getCoordsByAngle(distance, angle.toDouble(), PointF(x, y))
        x = pt.x
        y = pt.y
    }

    /**
     * 绘制敌人的图像到屏幕
     */
    override fun draw(canvas: Canvas) {
        bmpEnemy?.let {
            canvas.withRotation(
                angle + 90,
                x + AppGobal.unitSize / 2,
                y + AppGobal.unitSize / 2
            ) {
                if (isShow) {
                    drawMotion(this)
                    drawBitmap(it, x, y, null)
                }
            }
        }
    }

    /**
     * 绘制圆形光晕
     */
    private fun drawMotion(canvas: Canvas) {
        paint.color = Color.rgb(25, 130, 255)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 1f
        paint.maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.OUTER)
        canvas.drawCircle(
            x + AppGobal.unitSize / 2, y + AppGobal.unitSize / 2,
            AppGobal.unitSize / 2.5f, paint
        )
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        canvas.drawCircle(
            x + AppGobal.unitSize / 2, y + AppGobal.unitSize / 2,
            AppGobal.unitSize / 2.5f, paint
        )
    }

    /**
     * 向玩家的位置发射子弹
     */
    fun sendBullet() {
        // 根据当前时间和射击延时判断是否可以发射子弹
        if (System.currentTimeMillis() - lastAttackTime >= attackDelay) {
            val ex = if (bmpEnemy != null) bmpEnemy!!.width / 2 + x else x
            val ey = if (bmpEnemy != null) bmpEnemy!!.height / 2 + y else y
            val px = Player.size.width / 2 + Player.x
            val py = Player.size.height / 2 + Player.y
            val angle = MathUtils.getAngle(ex, ey, px, py).toFloat()
            BulletManager.sendTargetPlayer(ex, ey, angle)
            lastAttackTime = System.currentTimeMillis()
        }
    }
}