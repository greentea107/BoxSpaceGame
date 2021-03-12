package com.bamboo.boxspacegame

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.edit
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pgyer.pgyersdk.PgyerSDKManager
import com.pgyer.pgyersdk.pgyerenum.FeatureEnum
import com.tencent.bugly.crashreport.CrashReport

class MyApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        /**
         * 保存音频选项
         */
        fun saveSoundOption(isPlayBGM: Boolean, isPlaySFX: Boolean) {
            context?.getSharedPreferences("audio", Context.MODE_PRIVATE)
                ?.edit {
                    this.putBoolean("bgm", isPlayBGM)
                    this.putBoolean("sfx", isPlaySFX)
                }
        }

        fun isPlayBGM() = context?.getSharedPreferences("audio", Context.MODE_PRIVATE)
            ?.getBoolean("bgm", true)
            ?: true

        fun isPlaySFX() = context?.getSharedPreferences("audio", Context.MODE_PRIVATE)
            ?.getBoolean("sfx", true)
            ?: true

        /**
         * 是否允许敌人开火
         */
        fun saveEnableEnemyAttack(isEnable: Boolean) {
            context?.getSharedPreferences("enemy_attack", Context.MODE_PRIVATE)
                ?.edit { this.putBoolean("enable_attack", isEnable) }
        }

        fun isEnableEnemyAttack() =
            context?.getSharedPreferences("enemy_attack", Context.MODE_PRIVATE)
                ?.getBoolean("enable_attack", false)
                ?: true
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        CrashReport.initCrashReport(applicationContext, "135384028a", false)

        PgyerSDKManager.InitSdk()
            .setContext(context)
            .enable(FeatureEnum.CHECK_UPDATE)  //添加检查新版本
            .build()

        LiveEventBus.config().enableLogger(false).lifecycleObserverAlwaysActive(false)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                // 屏幕常亮
                activity.window.addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
                // 全屏，隐藏状态栏和导航栏
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    val v = activity.window.decorView
                    v.systemUiVisibility = View.GONE
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val decorView = activity.window.decorView
                    val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                    decorView.systemUiVisibility = uiOptions
                }
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

        })
    }
}