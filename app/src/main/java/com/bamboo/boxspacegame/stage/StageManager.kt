package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.record.RecordBean
import com.bamboo.boxspacegame.record.RecordManager
import com.bamboo.boxspacegame.spirit.BulletManager
import com.bamboo.boxspacegame.utils.LogEx
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 关卡管理
 */
object StageManager {
    private var stage: Stage? = null
    private var currentStageNo = 1 // 当前的关卡数
    private var enemyCount = 5 // 初始的敌人数量
    private var enemyHP = 10f // 初始的敌人的HP量
    private var startMillis = 0L
    private val listRecord = mutableListOf<RecordBean>()

    fun init(scope: CoroutineScope) {
        currentStageNo = 1
        enemyCount = 5
        enemyHP = 10f
        // 从本地读取各关的记录
        listRecord.clear()
        listRecord += RecordManager.loadStageRecord()
        listRecord.forEach { it.time = 0L } // 清除上次用时记录
        // 运行协程
        scope.launch(Dispatchers.Default) {
            stage = Stage().apply {
                this.setEnemyData(enemyCount, enemyHP)
                this.setPlayerLocation()
            }
            while (AppGobal.isRunning) {
                if (AppGobal.pause) continue
                when (stage?.getStatus()) {
                    Stage.READY -> { // 准备阶段
                        onReady()
                    }
                    Stage.PLAYING -> { // 关卡进行中
                        onPlaying()
                    }
                    Stage.MISSION_COMPLETED -> { // 通关成功
                        onMissionComplete()
                    }
                    Stage.MISSION_FAILED -> { // 通关失败
                        onMissionFailed()
                    }
                }
                delay(50)
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
            startMillis, System.currentTimeMillis(),
            listRecord
        )
        listRecord[currentStageNo - 1].time = System.currentTimeMillis() - startMillis
        startMillis = System.currentTimeMillis()
        // 设置下一关的参数
        currentStageNo++
        enemyCount += 3 // 敌方数量增加
        stage?.let {
            it.setEnemyData(enemyCount, enemyHP)
            it.setPlayerLocation()
        }
        // 刷新控件，显示关卡数
        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
    }

    /**
     * 通关失败并退回首页
     */
    private fun onMissionFailed() {
        RecordManager.saveStageRecord(
            currentStageNo,
            startMillis, System.currentTimeMillis(),
            listRecord
        )
        currentStageNo = 1
        LiveEventBus.get(AppGobal.EVENT_GAME_OVER).post(true)
    }

    private fun onPlaying() {
        stage?.actionMotion()
        LiveEventBus.get(AppGobal.EVENT_CURRENT_TIME)
            .post(System.currentTimeMillis() - startMillis)
    }

    private fun onReady() {
        startMillis = System.currentTimeMillis()
        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
        LiveEventBus.get(AppGobal.EVENT_FASTEST_TIME).post(true)
    }

    fun draw(canvas: Canvas) {
        stage?.drawAllEnemy(canvas)
    }

    fun getListEnemy() = stage?.getListEnemy()

    fun getCurrentStageNo() = this.currentStageNo

    fun clearAllEnemy() {
        stage?.clearEnemy()
    }
}