package com.bamboo.boxspacegame.spirit

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

object BulletManager {
    private val listBullet = CopyOnWriteArrayList<Bullet>()

    fun init(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (true) {
                if (AppGobal.pause) continue
                listBullet.filter { !it.free }.forEach { it.move() }
                delay(3)
            }
        }
    }

    fun obtain(): Bullet {
        val bullet = listBullet.find { it.free }
        return if (bullet == null) {
            val b = Bullet()
            listBullet.add(b)
            b
        } else bullet
    }

    fun draw(canvas: Canvas) {
        listBullet.filter { !it.free }.forEach { it.draw(canvas) }
    }

    fun release() {
        listBullet.clear()
    }
}