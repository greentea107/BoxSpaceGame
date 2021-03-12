package com.bamboo.boxspacegame.spirit

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * 子弹类
 */
class Bullet : BaseSprite() {
    private var target = AppGobal.TARGET_ENEMY // 子弹要攻击的目标enemy：敌人，player：玩家

    var damage: Float = 2f // 伤害值
        set(value) {
            field = if (value <= 0f) 2f else value
        }

    companion object {
        const val INTERVAL = 150L // 子弹的间隔
    }

    @Synchronized
    override fun move() {
        val pt = MathUtils.getCoordsByAngle(distance, angle.toDouble(), PointF(x, y))
        x = pt.x
        y = pt.y
        // 判断子弹的目标
        if (target == AppGobal.TARGET_ENEMY) {
            // 处理子弹的目标是敌人的情况
            StageManager.getListEnemy()
                ?.find {
                    !it.free && it.getRect().contains(x, y)
                }
                ?.let {
                    // 如果击中敌人则将子弹设为空闲并播放子弹特效
                    free = true
                    it.hit(this)
                    EffectManager.obtainBullet().play(x, y)
                    return
                }
        } else {
            // 处理子弹的目标是玩家的情况
            if (Player.getRect().contains(x, y)) {
                free = true
                Player.beHit(this)
                EffectManager.obtainBullet().play(x, y)
                return
            }
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
        val keyBmp = if (target == AppGobal.TARGET_PLAYER)
            AppGobal.BMP_BULLET_ENEMY else AppGobal.BMP_BULLET_PLAYER
        val bmp = AppGobal.bmpCache[keyBmp]
        canvas.drawBitmap(bmp, x - bmp.width / 2, y - bmp.height / 2, null)
    }

    /**
     * 由玩家发射的子弹
     * @param x 子弹的起始X轴
     * @param y 子弹的起始Y轴
     * @param angle 子弹移动的角度
     * @param damage 子弹的伤害值
     */
    fun sendTargetEnmey(x: Float, y: Float, angle: Float) {
        this.free = false
        this.x = x
        this.y = y
        this.angle = angle
        this.damage = 3f
        this.target = AppGobal.TARGET_ENEMY
        this.distance = 7f
        LiveEventBus.get(AppGobal.EVENT_BULLET_SFX).post(true)
    }

    /**
     * 由敌人发射的子弹
     * @param x 子弹的起始X轴
     * @param y 子弹的起始Y轴
     * @param angle 子弹移动的角度
     * @param damage 子弹的伤害值
     */
    fun sendTargetPlayer(x: Float, y: Float, angle: Float) {
        this.free = false
        this.x = x
        this.y = y
        this.angle = angle
        this.damage = 30f
        this.target = AppGobal.TARGET_PLAYER
        this.distance = 2f
        LiveEventBus.get(AppGobal.EVENT_BULLET_SFX).post(true)
    }
}