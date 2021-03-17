package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.record.RecordBean
import com.bamboo.boxspacegame.record.RecordManager
import com.bamboo.boxspacegame.spirit.BulletManager
import com.bamboo.boxspacegame.spirit.Enemy
import com.bamboo.boxspacegame.spirit.Enemy2
import com.bamboo.boxspacegame.spirit.Player
import com.bamboo.boxspacegame.utils.MathUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.*
import java.util.*

/**
 * 关卡管理
 */
object StageManager {
    // 游戏状态
    const val STATE_READY = 0 // 准备阶段
    const val STATE_PLAYING = 1 // 进行中
    const val STATE_MISSION_FAILED = 2 // 失败
    const val STATE_MISSION_COMPLETED = 3 // 过关
    private var currentStageNo = 1 // 当前的关卡数
    private var enemyCount = 5 // 初始的敌人数量
    private var enemyHP = 10f // 初始的敌人的HP量
    private var enableEnemyAttack = true // 是否允许敌人开火
    private var stageStartMillis = 0L // 记录关卡的起始时间
    private val listRecord = mutableListOf<RecordBean>() // 记录各关的通关时间
    private val listEnemy = mutableListOf<Enemy>() // 保存各关的敌人
    var gameStatus = STATE_READY // 游戏状态

    fun init(scope: CoroutineScope, isEnableEnemyAttack: Boolean) {
        currentStageNo = 1
        enemyCount = 5
        enemyHP = 10f
        enableEnemyAttack = isEnableEnemyAttack
        // 从本地读取各关的记录
        listRecord.clear()
        listRecord += RecordManager.loadStageRecord()
        listRecord.forEach { it.time = 0L } // 清除上次用时记录
        // 运行协程
        scope.launch(Dispatchers.Default) {
            setEnemyData(enemyCount, enemyHP)
            setPlayerLocation()
            while (AppGobal.isLooping) {
                if (AppGobal.pause) continue
                when (gameStatus) {
                    STATE_READY -> { // 准备阶段
                        onReady()
                    }
                    STATE_PLAYING -> { // 关卡进行中
                        onPlaying()
                    }
                    STATE_MISSION_COMPLETED -> { // 通关成功
                        onMissionComplete()
                    }
                    STATE_MISSION_FAILED -> { // 通关失败
                        onMissionFailed()
                    }
                }
                delay(50)
            }
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

    /**
     * 通关成功并开始下一关
     */
    private fun onMissionComplete() {
        // 保存关卡的记录
        RecordManager.saveStageRecord(
            currentStageNo,
            stageStartMillis, System.currentTimeMillis(),
            listRecord
        )
        listRecord[currentStageNo - 1].time = System.currentTimeMillis() - stageStartMillis
        stageStartMillis = System.currentTimeMillis()
        // 设置下一关的参数
        currentStageNo++
        enemyCount += 3 // 敌方数量增加
        setEnemyData(enemyCount, enemyHP)
        setPlayerLocation()
        // 刷新控件，显示关卡数
        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
    }

    /**
     * 通关失败并退回首页
     */
    private fun onMissionFailed() {
        RecordManager.saveStageRecord(
            currentStageNo,
            stageStartMillis, System.currentTimeMillis(),
            listRecord
        )
        currentStageNo = 1
        BulletManager.clearAll()
        LiveEventBus.get(AppGobal.EVENT_GAME_OVER).post(true)
    }

    private fun onPlaying() {
        actionMotion()
        val endMillis = System.currentTimeMillis() - stageStartMillis
        LiveEventBus.get(AppGobal.EVENT_CURRENT_TIME).post(endMillis)
    }

    private fun onReady() {
        stageStartMillis = System.currentTimeMillis()
        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
        LiveEventBus.get(AppGobal.EVENT_FASTEST_TIME).post(true)
    }

    fun draw(canvas: Canvas) {
        listEnemy.filter { !it.free }.forEach { it.draw(canvas) }
    }

    /**
     * 设置玩家的登场点和关卡中的敌人总数及敌人的HP值
     */
    @Synchronized
    fun setPlayerLocation() {
        gameStatus = STATE_READY
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
                gameStatus = STATE_PLAYING
            }
        }
    }

    private fun actionMotion() {
        addEnemy()
        // 遍历关卡中的全部敌方
        listEnemy.filter { !it.free }.forEach {
            it.move()
            // 判断敌人是否和玩家碰撞
            if (MathUtils.cross(it.getRect(), Player.getRect()) && Player.isShow) {
                Player.shotDown()
            }
            // 判断当前对象是否可以向玩家发射子弹
            if (it is Enemy2 && enableEnemyAttack) {
                it.sendBullet()
            }
        }
        // 判断敌方是否全部消灭
        if (listEnemy.count { it.free && it.HP <= 0 } == enemyCount) {
            gameStatus = STATE_MISSION_COMPLETED
        }
    }

    /**
     * 敌人登场
     */
    @Synchronized
    private fun addEnemy() {
        if ((System.currentTimeMillis() - stageStartMillis) < 1000) return
        if (listEnemy.size < enemyCount) {
            initEnemy(enemyHP, if (listEnemy.size % 5 == 0) 1 else 0)
            stageStartMillis = System.currentTimeMillis()
        }
    }

    @Synchronized
    fun setEnemyData(enemyCount: Int, enemyHP: Float) {
        this.enemyCount = enemyCount
        this.enemyHP = enemyHP
        gameStatus = STATE_READY
        listEnemy.clear()
    }

    fun getListEnemy() = listEnemy

    fun clearAllEnemy() {
        listEnemy.filter { !it.free }.forEach {
            it.free = true
            it.HP = 0f
            val cx = it.getRect().width().div(2)
            val cy = it.getRect().height().div(2)
            EffectManager.obtainBomb().play(it.x + cx, it.y + cy)
        }
    }

    fun getCurrentStageNo() = this.currentStageNo

}