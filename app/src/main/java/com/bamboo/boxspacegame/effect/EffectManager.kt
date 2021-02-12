package com.bamboo.boxspacegame.effect

import android.graphics.Canvas
import com.bamboo.boxspacegame.effect.BaseEffect
import com.bamboo.boxspacegame.effect.BombEffect
import com.bamboo.boxspacegame.effect.BulletEffect
import com.bamboo.boxspacegame.effect.FlashEffect
import java.lang.Exception
import java.util.concurrent.CopyOnWriteArrayList

object EffectManager {
    private val listEffect = CopyOnWriteArrayList<BaseEffect>()

    private inline fun <reified T : BaseEffect> obtain(t: T): BaseEffect {
        val effect = listEffect.find {
            it.free && (it is T)
        }
        return effect ?: t.apply { listEffect += this }
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
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun release() {
        listEffect.clear()
    }
}