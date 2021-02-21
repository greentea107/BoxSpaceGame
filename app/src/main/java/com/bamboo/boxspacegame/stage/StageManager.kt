package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.record.RecordBean
import com.bamboo.boxspacegame.record.RecordManager
import com.bamboo.boxspacegame.spirit.BulletManager
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
        scope.launch(Dispatchers.Default) {
            stage = Stage().apply {
                this.setEnemyData(enemyCount, enemyHP)
                this.setPlayerLocation()
            }
            while (AppGobal.isRunning) {
                if (AppGobal.pause) continue
                when (stage?.getStatus()) {
                    Stage.READY -> {
                        startMillis = System.currentTimeMillis()
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
                        LiveEventBus.get(AppGobal.EVENT_FASTEST_TIME).post(true)
                    }
                    Stage.PLAYING -> { // 关卡进行中
                        stage?.actionMotion()
                        LiveEventBus.get(AppGobal.EVENT_CURRENT_TIME)
                            .post(System.currentTimeMillis() - startMillis)
                    }
                    Stage.MISSION_COMPLETED -> { // 通关成功
                        RecordManager.saveStageRecord(
                            currentStageNo,
                            startMillis, System.currentTimeMillis(),
                            listRecord
                        )
                        BulletManager.damage += 2f // 子弹威力升级
                        currentStageNo++
                        enemyCount++
                        enemyHP += 5f
                        stage?.let {
                            it.setEnemyData(enemyCount, enemyHP)
                            it.setPlayerLocation()
                        }
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
                    }
                    Stage.MISSION_FAILED -> { // 通关失败
                        stage?.let {
                            it.setEnemyData(enemyCount, enemyHP)
                            it.setPlayerLocation()
                        }
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
                    }
                }
                delay(50)
            }
        }
    }

    fun draw(canvas: Canvas) {
        stage?.drawAllEnemy(canvas)
    }

    fun getListEnemy() = stage?.getListEnemy()

    fun getCurrentStageNo() = this.currentStageNo
}