package com.bamboo.boxspacegame

import android.content.Context
import androidx.core.content.edit

object OptionHelper {
    private const val FILE = "option"

    /**
     * 保存音频选项
     */
    fun saveSoundOption(context: Context, isPlayBGM: Boolean, isPlaySFX: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit {
                this.putBoolean("bgm", isPlayBGM)
                this.putBoolean("sfx", isPlaySFX)
            }
    }

    fun isPlayBGM(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean("bgm", true)

    fun isPlaySFX(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean("sfx", true)

    /**
     * 是否允许敌人开火
     */
    fun saveEnableEnemyAttack(context: Context, isEnable: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit { this.putBoolean("enable_enemy_attack", isEnable) }
    }

    fun isEnableEnemyAttack(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean("enable_enemy_attack", false)

    fun saveShowFPS(context: Context, isShowFPS: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit { this.putBoolean("show_fps", isShowFPS) }
    }

    fun isShowFPS(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean("show_fps", false)
}