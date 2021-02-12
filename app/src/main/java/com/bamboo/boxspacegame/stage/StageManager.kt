package com.bamboo.boxspacegame.stage

import android.graphics.Canvas
import com.bamboo.boxspacegame.AppGobal
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.spirit.BulletManager
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.coroutines.*

object StageManager {
    private var stage: Stage? = null
    private var stageNo = 1
    private var enemyCount = 5
    private var enemyHP = 10f
    var score = 0

    fun init(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            stage = Stage()
            stage?.setPlayerAndEnemy(enemyCount, enemyHP)
            while (true) {
                if (AppGobal.pause) continue
                when (stage?.getStatus()) {
                    Stage.READY -> {
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(stageNo)
                        LiveEventBus.get(AppGobal.EVENT_SCORE).post(score)
                    }
                    Stage.PLAYING -> { // 关卡进行中
                        stage?.actionMotion()
                    }
                    Stage.MISSION_COMPLETED -> { // 通关成功
                        EffectManager.release()
                        BulletManager.release()
                        stageNo++
                        enemyCount++
                        enemyHP += 10f
                        stage?.setPlayerAndEnemy(enemyCount, enemyHP)
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(stageNo)
                        LiveEventBus.get(AppGobal.EVENT_SCORE).post(score)
                    }
                    Stage.MISSION_FAILED -> { // 通关失败
                        EffectManager.release()
                        BulletManager.release()
                        stage?.setPlayerAndEnemy(enemyCount, enemyHP)
                        LiveEventBus.get(AppGobal.EVENT_STAGE_NO).post(stageNo)
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

    fun reset() {
        stageNo = 1
        enemyCount = 5
        enemyHP = 10f
        score = 0
        stage?.reset()
    }

    fun getListEnemy() = stage?.getListEnemy()
}