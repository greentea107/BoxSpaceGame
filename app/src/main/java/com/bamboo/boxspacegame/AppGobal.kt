package com.bamboo.boxspacegame

object AppGobal {
    const val EVENT_STAGE_NO = "stageNo"
    const val EVENT_FASTEST_TIME = "fastest_time"
    const val EVENT_CURRENT_TIME = "current_time"
    const val EVENT_GAME_OVER = "game_over"
    const val EVENT_BOMB_SFX = "bomb_sfx" // 爆炸音效
    const val EVENT_BULLET_SFX = "bullet_sfx" // 子弹音效
    const val EVENT_FLASH_SFX = "flash_sfx" // 瞬移音效
    const val POWER_MAX = 300//能量的最大值
    const val TARGET_PLAYER = "player"
    const val TARGET_ENEMY = "enemy"
    var screenWidth = 0
    var screenHeight = 0
    var unitSize = 0f // 基准单位，屏幕上的玩家、敌人、爆炸等物体的大小以此为基准
    var isLooping = true
    var pause = false

}