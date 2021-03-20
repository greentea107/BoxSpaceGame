package com.bamboo.boxspacegame

import android.graphics.Bitmap
import androidx.collection.LruCache

/**
 * Bitmap缓存工具类
 */
object BmpCache {
    private val mapCache = LruCache<String, Bitmap>(120)
    fun get(key: String) = mapCache[key]
    fun put(key: String, bmp: Bitmap) {
        mapCache.put(key, bmp)
    }

    const val BMP_PLAYER = "player"
    const val BMP_ENEMY = "enemy1"
    const val BMP_ENEMY_2 = "enemy2"
    const val BMP_BACKGROUND = "background"
    const val BMP_FLASH = "flash"
    const val BMP_BOMB = "bomb"
    const val BMP_BULLET_PLAYER = "bulletPlayer" // 玩家发射的子弹
    const val BMP_BULLET_ENEMY = "bulletEnemy" // 敌人发射的子弹
    const val BMP_GRENADE = "grenade"
}