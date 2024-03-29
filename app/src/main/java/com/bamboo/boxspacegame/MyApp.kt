package com.bamboo.boxspacegame

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tencent.bugly.crashreport.CrashReport

class MyApp : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        CrashReport.initCrashReport(applicationContext, "135384028a", false)

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