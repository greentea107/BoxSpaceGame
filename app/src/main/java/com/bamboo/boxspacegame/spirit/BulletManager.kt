package com.bamboo.boxspacegame.spirit

import android.graphics.*
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.BmpCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 子弹管理类，以单例模式运行
 * 管理类内部有一个集合，用于保存所有的玩家和敌人发射的子弹
 * 如果子弹击中目标或越界后就将该对象设为空闲，以便于下回使用而不再频繁的创建对象
 */
object BulletManager {
    private val listBullet = mutableListOf<Bullet>()

    fun init(scope: CoroutineScope) {
        buildBitmapPlayer()
        buildBitmapEnemy()
        scope.launch(Dispatchers.Default) {
            while (AppGobal.isLooping) {
                if (AppGobal.pause) continue
                listBullet.filter { !it.free }.forEach { it.move() }
                delay(3)
            }
        }
    }

    /**
     * 绘制玩家使用的位图
     */
    private fun buildBitmapPlayer() {
        val size = Player.size.width.toInt() / 2
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                size / 2f, size / 2f, size / 4f,
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFEFA2"),
                    Color.parseColor("#56FFFFFF")
                ),
                floatArrayOf(0.1f, 0.5f, 0.8f),
                Shader.TileMode.CLAMP
            )
            this.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        }
        BmpCache.put(BmpCache.BMP_BULLET_PLAYER, bmp)
    }

    /**
     * 绘制敌方使用的子弹位图
     */
    private fun buildBitmapEnemy() {
        val size = Player.size.width.toInt() / 2
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                size / 2f, size / 2f, size / 4f,
                intArrayOf(
                    Color.parseColor("#3282F5"),
                    Color.parseColor("#A2EFFF"),
                    Color.parseColor("#5633CAFF")
                ),
                floatArrayOf(0.1f, 0.5f, 0.8f),
                Shader.TileMode.CLAMP
            )
            this.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        }
        BmpCache.put(BmpCache.BMP_BULLET_ENEMY, bmp)
    }

    /**
     * 从集合中找出一个空闲的对象，如果没有空闲的就创建一个并返回
     */
    private fun obtain(): Bullet {
        val bullet = listBullet.find { it.free }
        return if (bullet == null) {
            val b = Bullet()
            listBullet.add(b)
            b
        } else bullet
    }

    fun sendTargetEnemy(x: Float, y: Float, angle: Float) {
        obtain().sendTargetEnmey(x, y, angle)
    }

    fun sendTargetPlayer(x: Float, y: Float, angle: Float) {
        obtain().sendTargetPlayer(x, y, angle)
    }

    fun draw(canvas: Canvas) {
        listBullet.filter { !it.free }.forEach { it.draw(canvas) }
    }

    @Synchronized
    fun clearAll() {
        listBullet.clear()
    }
}