package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.spirit.BulletManager
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.*

/**
 * 关卡管理
 */
object StageManager {
    private var stage: Stage? = null
    private var currentStageNo = 1 // 当前的关卡数
    private var enemyCount = 5 // 初始的敌人数量
    private var enemyHP = 10f // 初始的敌人的HP量
    var score = 0

    fun init(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            stage = Stage()
            stage?.setPlayerAndEnemy(enemyCount, enemyHP)
            while (true) {
                if (AppGobal.pause) continue
                when (stage?.getStatus()) {
                    Stage.READY -> {
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
                        LiveEventBus.get(AppGobal.EVENT_SCORE).post(score)
                    }
                    Stage.PLAYING -> { // 关卡进行中
                        stage?.actionMotion()
                    }
                    Stage.MISSION_COMPLETED -> { // 通关成功
                        EffectManager.release()
                        BulletManager.release()
                        BulletManager.damage += 2f
                        currentStageNo++
                        enemyCount++
                        enemyHP += 5f
                        stage?.setPlayerAndEnemy(enemyCount, enemyHP)
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
                        LiveEventBus.get(AppGobal.EVENT_SCORE).post(score)
                    }
                    Stage.MISSION_FAILED -> { // 通关失败
                        EffectManager.release()
                        BulletManager.release()
                        stage?.setPlayerAndEnemy(enemyCount, enemyHP)
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(currentStageNo)
                        LiveEventBus.get(AppGobal.EVENT_SCORE).post(score)
                    }
                }
                delay(50)
            }
        }
    }

    fun draw(canvas: Canvas) {
        stage?.drawAllEnemy(canvas)
    }

    /**
     * 重新开始游戏
     */
    fun reset() {
        currentStageNo = 1
        enemyCount = 5
        enemyHP = 10f
        score = 0
        stage?.reset()
    }

    fun getListEnemy() = stage?.getListEnemy()
}