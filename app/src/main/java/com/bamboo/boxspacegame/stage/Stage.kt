package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.spirit.Enemy
import com.bamboo.boxspacegame.spirit.Player
import com.bamboo.boxspacegame.utils.LogEx
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * 游戏关卡
 */
class Stage {
    private var enemyCount: Int = 0
    private var enemyHp: Float = 10f
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
        // 遍历关卡中的全部敌方
        listEnemy.filter { !it.free }.forEach {
            it.move()
            // 判断敌人是否和玩家碰撞
            if (MathUtils.cross(it.getRect(), Player.getRect())) {
                val center = AppGobal.unitSize / 2
                EffectManager.obtainBomb().play(Player.x + center, Player.y + center) {
                    gameStatus = MISSION_FAILED
                    // 通过事件机制播放音效
                    LiveEventBus.get(AppGobal.EVENT_BOMB_SFX).post(true)
                }
            }
        }
        // 判断敌方是否全部消灭
        if (listEnemy.count { it.free && it.HP <= 0 } == listEnemy.size) {
            gameStatus = MISSION_COMPLETED
        }
    }

    /**
     * 设置玩家的登场点和关卡中的敌人总数及敌人的HP值
     */
    @Synchronized
    suspend fun setPlayerAndEnemy(enemyCount: Int, enemyHP: Float) {
        this.enemyCount = enemyCount
        this.enemyHp = enemyHP
        // 设置玩家登场
        Player.isShow = false
        listEnemy.clear()
        EffectManager.obtainFlash().play(Player.x, Player.y, true) {
            Player.let {
                // 设置玩家的位置
                it.isShow = true
                it.x = AppGobal.screenWidth / 2f
                it.y = AppGobal.screenHeight / 2f
            }
        }
        // 敌人登场
        withContext(Dispatchers.IO) {
            delay(2000) // 延时两秒后加载敌人
            gameStatus = READY
            repeat(enemyCount) {
                initEnemy(enemyHP)
                delay(500)
                if (it == (enemyCount - 1)) gameStatus = PLAYING
            }
        }
    }

    /**
     * 初始化敌人的位置和血量
     */
    private fun initEnemy(HP: Float) {
        var x = Random(System.currentTimeMillis()).nextInt(20) * AppGobal.unitSize
        val stepY = AppGobal.screenHeight / AppGobal.unitSize.toInt()
        var y = Random(System.currentTimeMillis()).nextInt(stepY) * AppGobal.unitSize
        when (Random(System.currentTimeMillis()).nextInt(4)) {
            0 -> y = AppGobal.unitSize
            1 -> y = AppGobal.screenHeight - AppGobal.unitSize * 2
            2 -> x = AppGobal.unitSize
            3 -> x = AppGobal.screenWidth - AppGobal.unitSize * 2
        }
        // 判断敌人的出场点是否在地图的透视区
        if (x <= AppGobal.unitSize) x = AppGobal.unitSize
        if (x >= AppGobal.screenWidth - AppGobal.unitSize)
            x = AppGobal.screenWidth - 1 - AppGobal.unitSize
        if (y < AppGobal.unitSize) y = AppGobal.unitSize
        if (y >= AppGobal.screenHeight - AppGobal.unitSize)
            y = AppGobal.screenHeight - 1 - AppGobal.unitSize
        // 播放入场动画
        EffectManager.obtainFlash().play(x, y, true) {
            // 设置敌人的位置并保存
            val enemy = Enemy().apply {
                this.x = x
                this.y = y
                this.HP = HP
                this.free = false
                this.angle = setAngleByCoord(Player.x, Player.y)
            }
            listEnemy.add(enemy)
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