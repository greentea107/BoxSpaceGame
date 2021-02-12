package com.bamboo.boxspacegame.effect

import android.graphics.Canvas

abstract class BaseEffect {
    protected var x: Float = 0f
    protected var y: Float = 0f
    var free: Boolean = true
    abstract fun draw(canvas: Canvas);
}