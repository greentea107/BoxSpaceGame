package com.bamboo.boxspacegame.spirit

import android.graphics.Canvas

abstract class BaseSpirit {
    var x: Float = 0f
    var y: Float = 0f
    var HP: Float = 10f
    var angle: Float = 0f
    var free: Boolean = true
    var distance = 0f

    abstract fun draw(canvas: Canvas)
    abstract fun move()
}