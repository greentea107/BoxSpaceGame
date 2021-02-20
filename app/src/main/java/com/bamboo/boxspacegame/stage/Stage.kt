package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.spirit.Enemy
import com.bamboo.boxspacegame.spirit.Enemy2
import com.bamboo.boxspacegame.spirit.Player
import com.bamboo.boxspacegame.utils.MathUtils
import kotlinx.coroutines.delay
import java.util.*

/**
 * 游戏关卡
 */
class Stage {
    private var enemyCount: Int = 0
    private var enemyHP: Float = 8f
    private val listEnemy = mutableListOf<Enemy>()
    private var gameStatus = READY

    /**
     * 关卡状态
     */
    companion object Status {
        const val READY = 0 // 准备阶段
        const val PLAYING = 1 // 进行中
        const val MISSION_FAILED = 2 // 失败
        const val MISSION_COMPLETED = 3 // 过关
    }

    fun actionMotion() {
        addEnemy()
        // 遍历关卡中的全部敌方
        listEnemy.filter { !it.free }.forEach {
            it.move()
            // 判断敌人是否和玩家碰撞
            if (MathUtils.cross(it.getRect(), Player.getRect()) && Player.isShow) {
                Player.isShow = false
                val center = AppGobal.unitSize / 2
                EffectManager.obtainBomb().play(Player.x + center, Player.y + center) {
                    gameStatus = MISSION_FAILED
                }
            }
        }
        // 判断敌方是否全部消灭
        if (listEnemy.count { it.free && it.HP <= 0 } == listEnemy.size) {
            gameStatus = MISSION_COMPLETED
        }
    }

    @Synchronized
    fun setEnemyData(enemyCount: Int, enemyHP: Float) {
        this.enemyCount = enemyCount
        this.enemyHP = enemyHP
        gameStatus = READY
        listEnemy.clear()
    }

    /**
     * 设置玩家的登场点和关卡中的敌人总数及敌人的HP值
     */
    @Synchronized
    fun setPlayerLocation() {
        gameStatus = READY
        // 设置玩家登场
        Player.isShow = false
        EffectManager.obtainFlash().play(Player.x, Player.y, true) {
            val cx = AppGobal.screenWidth / 2f
            val cy = AppGobal.screenHeight / 2f
            EffectManager.obtainFlash().play(cx, cy) {
                Player.let {
                    // 设置玩家的位置
                    it.isShow = true
                    it.x = cx
                    it.y = cy
                }
                gameStatus = PLAYING
            }
        }
    }

    private var startMillis = System.currentTimeMillis()

    /**
     * 敌人登场
     */
    @Synchronized
    private fun addEnemy() {
        if((System.currentTimeMillis()-startMillis)<1000) return
        if (listEnemy.size < enemyCount) {
            initEnemy(enemyHP, if (listEnemy.size % 5 == 0) 1 else 0)
            startMillis = System.currentTimeMillis()
        }
    }

    /**
     * 初始化敌人的位置和血量
     */
    private fun initEnemy(HP: Float, type: Int = 0) {
        val step = AppGobal.unitSize.toInt() * 2
        var x = Random().nextInt(AppGobal.screenWidth - step) + AppGobal.unitSize
        var y = Random().nextInt(AppGobal.screenHeight - step) + AppGobal.unitSize
        when (Random().nextInt(4)) {
            0 -> y = AppGobal.unitSize
            1 -> y = AppGobal.screenHeight - AppGobal.unitSize * 2
            2 -> x = AppGobal.unitSize
            3 -> x = AppGobal.screenWidth - AppGobal.unitSize * 2
        }
        // 判断敌人的出场点是否在地图的透视区
        if (x <= AppGobal.unitSize) x = AppGobal.unitSize
        if (x >= AppGobal.screenWidth - AppGobal.unitSize - 1)
            x = AppGobal.screenWidth - AppGobal.unitSize - 1
        if (y < AppGobal.unitSize) y = AppGobal.unitSize
        if (y >= AppGobal.screenHeight - AppGobal.unitSize - 1)
            y = AppGobal.screenHeight - AppGobal.unitSize - 1
        // 设置敌人的位置并保存
        val enemy = when (type) {
            0 -> Enemy()
            1 -> Enemy2()
            else -> Enemy()
        }.apply {
            this.x = x
            this.y = y
            this.HP = HP
            this.free = false
            this.isShow = false
            this.angle = setAngleByCoord(Player.x, Player.y)
        }
        listEnemy.add(enemy)
        // 播放入场动画
        EffectManager.obtainFlash().play(x, y, true) {
            enemy.let {
                it.isShow = true
                it.x = x
                it.y = y
            }
        }
    }

    fun drawAllEnemy(canvas: Canvas) {
        listEnemy.filter { !it.free }.forEach { it.draw(canvas) }
    }

    fun getListEnemy() = listEnemy

    fun getStatus() = gameStatus

    fun reset() {
        gameStatus = MISSION_FAILED
    }
}