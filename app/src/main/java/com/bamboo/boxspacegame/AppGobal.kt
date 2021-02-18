package com.bamboo.boxspacegame

import android.graphics.Bitmap
import android.util.LruCache

object AppGobal {
    const val BMP_ENEMY = "enemy"
    const val BMP_PLAYER = "player"
    const val BMP_FLASH = "flash"
    const val BMP_BOMB = "bomb"
    const val EVENT_STAGE_NO = "stageNo"
    const val EVENT_SCORE = "score"
    const val EVENT_BOMB_SFX = "bomb_sfx" // 爆炸音效
    const val EVENT_BULLET_SFX = "bullet_sfx" // 子弹音效
    const val EVENT_FLASH_SFX = "flash_sfx" // 瞬移音效
    var screenWidth = 0
    var screenHeight = 0
    var unitSize = 0f
    var pause = false
    val bmpCache = LruCache<String, Bitmap>(120)
}