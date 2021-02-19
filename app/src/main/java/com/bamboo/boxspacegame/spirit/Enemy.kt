package com.bamboo.boxspacegame.spirit

import android.graphics.*
import androidx.core.graphics.withRotation
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import java.util.*

open class Enemy : BaseSprite() {
    private var bmpEnemy: Bitmap? = null
    private val paint = Paint()
    protected var score = 1

    init {
        this.distance = 3f
        this.HP = 10f
        bmpEnemy = AppGobal.bmpCache[AppGobal.BMP_ENEMY]
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
                    Color.rgb(78, 136, 55),
                    Color.rgb(44, 72, 49)
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
                    Color.rgb(235, 245, 235),
                    Color.rgb(78, 136, 55)
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
            paint.color = Color.rgb(220,255,200)
            this.drawPath(path, paint)
        }
        AppGobal.bmpCache.put(AppGobal.BMP_ENEMY, bmp)
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
                drawMotion(this)
                drawBitmap(it, x, y, null)
            }
        }
    }

    /**
     * 绘制圆形光晕
     */
    private fun drawMotion(canvas: Canvas) {
        paint.color = Color.parseColor("#4F78F1")
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
     * 根据指定的目标位置设置敌人的角度
     */
    fun setAngleByCoord(targetX: Float, targetY: Float): Float {
        return MathUtils.getAngle(x, y, targetX, targetY).toFloat()
    }

    fun getRect() = RectF(x, y, x + AppGobal.unitSize, y + AppGobal.unitSize)

    /**
     * 处理敌人被子弹击中的流程
     */
    fun hit(bullet: Bullet) {
        if (this.HP <= 0) {
            // 当敌人的血量清零的处理步骤
            // 将当前类设为空闲，以便于下次使用
            // 根据当前坐标播放爆炸效果
            // 通过事件总线更新分数和播放音效
            this.free = true
            val cx = bmpEnemy?.width?.div(2) ?: 0
            val cy = bmpEnemy?.height?.div(2) ?: 0
            EffectManager.obtainBomb().play(x + cx, y + cy)
            LiveEventBus.get(AppGobal.EVENT_SCORE).post(score)
            LiveEventBus.get(AppGobal.EVENT_BOMB_SFX).post(true)
        } else {
            this.HP -= bullet.damage
        }
        // 敌机被击中时再随机的往某个角度发射子弹，在视觉上作出子弹反弹的效果
        val bulletAngle = angle + Random().nextInt(360).toFloat()
        BulletManager.send(bullet.x, bullet.y, bulletAngle)
    }
}