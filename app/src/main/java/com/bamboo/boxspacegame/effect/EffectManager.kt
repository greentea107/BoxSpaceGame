package com.bamboo.boxspacegame.effect

import android.graphics.Canvas

/**
 * 特效管理类
 */
object EffectManager {
    private val listEffect = mutableListOf<BaseEffect>()

    @Synchronized
    private inline fun <reified T : BaseEffect> obtain(t: T): BaseEffect {
        val effect = listEffect.find {
            it.free && (it is T)
        }
        return effect ?: t.apply { listEffect += this }
    }

    fun obtainGrenade(): GrenadeEffect {
        return obtain(GrenadeEffect()) as GrenadeEffect
    }

    fun obtainBomb(): BombEffect {
        return obtain(BombEffect()) as BombEffect
    }

    fun obtainFlash(): FlashEffect {
        return obtain(FlashEffect()) as FlashEffect
    }

    fun obtainBullet(): BulletEffect {
        return obtain(BulletEffect()) as BulletEffect
    }

    fun draw(canvas: Canvas) {
        try {
            listEffect.filter { !it.free }.forEach { it.draw(canvas) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun release() {
        listEffect.clear()
    }
}