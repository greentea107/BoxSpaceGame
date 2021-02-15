package com.bamboo.boxspacegame.spirit

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus

class Bullet : BaseSprite() {
    private var size: Float = 0f

    companion object {
        const val INTERVAL = 150L // 子弹的间隔
    }

    init {
        this.distance = 7f
        this.size = Player.size.width / 2
        val bmp = AppGobal.bmpCache["bullet"]
        if (bmp == null) buildBitmap()
    }

    private fun buildBitmap() {
        val bmp = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                size / 2, size / 2, size / 4, intArrayOf(
                    Color.WHITE,
                    Color.parseColor("#FFEFA2"),
                    Color.parseColor("#56FFFFFF")
                ),
                floatArrayOf(0.1f, 0.5f, 0.8f),
                Shader.TileMode.CLAMP
            )
            this.drawCircle(size / 2, size / 2, size / 2, paint)
        }
        AppGobal.bmpCache.put("bullet", bmp)
    }

    override fun move() {
        val pt = MathUtils.getCoordsByAngle(distance, angle.toDouble(), PointF(x, y))
        x = pt.x
        y = pt.y
        // 判断子弹是否击中敌人
        StageManager.getListEnemy()
            ?.find {
                !it.free && it.getRect().contains(x, y)
            }
            ?.let {
                // 如果击中敌人则将子弹设为空闲并播放子弹特效
                free = true
                EffectManager.obtainBullet().play(x, y)
                it.hit()
                return
            }
        // 判断子弹是否越界，越界就释放
        if (x <= AppGobal.unitSize / 2) {
            x = AppGobal.unitSize / 2
            free = true
            EffectManager.obtainBullet().play(x, y)
        }
        if (x >= AppGobal.screenWidth - AppGobal.unitSize / 2) {
            x = AppGobal.screenWidth - 1f - AppGobal.unitSize / 2
            free = true
            EffectManager.obtainBullet().play(x, y)
        }
        if (y <= AppGobal.unitSize / 2) {
            y = AppGobal.unitSize / 2
            free = true
            EffectManager.obtainBullet().play(x, y)
        }
        if (y >= AppGobal.screenHeight - AppGobal.unitSize / 2) {
            y = AppGobal.screenHeight - 1 - AppGobal.unitSize / 2
            free = true
            EffectManager.obtainBullet().play(x, y)
        }
    }

    override fun draw(canvas: Canvas) {
        val bmp = AppGobal.bmpCache["bullet"]
        canvas.drawBitmap(bmp, x - size / 2, y - size / 2, null)
    }

    fun send(x: Float, y: Float, angle: Float) {
        this.free = false
        this.x = x
        this.y = y
        this.angle = angle
        LiveEventBus.get(AppGobal.EVENT_BULLET_SFX).post(true)
    }
}