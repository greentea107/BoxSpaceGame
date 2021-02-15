package com.bamboo.boxspacegame.effect

import android.graphics.Canvas

abstract class BaseEffect {
    // 动画播放的中心坐标
    protected var x: Float = 0f
    protected var y: Float = 0f
    var free: Boolean = true

    abstract fun draw(canvas: Canvas)
}