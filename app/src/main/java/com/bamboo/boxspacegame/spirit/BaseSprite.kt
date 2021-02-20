package com.bamboo.boxspacegame.spirit

import android.graphics.Canvas

abstract class BaseSprite {
    var x: Float = 0f
    var y: Float = 0f
    var HP: Float = 10f // 血量
    var angle: Float = 0f // 移动的方向
    var distance = 0f // 移动的距离
    var free: Boolean = true // 是否空闲状态
    var isShow: Boolean = true
    abstract fun draw(canvas: Canvas)
    abstract fun move()
}