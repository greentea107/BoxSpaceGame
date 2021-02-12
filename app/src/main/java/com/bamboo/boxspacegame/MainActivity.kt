package com.bamboo.boxspacegame

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.bamboo.boxspacegame.effect.BulletEffect
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.effect.FlashEffect
import com.bamboo.boxspacegame.spirit.BulletManager
import com.bamboo.boxspacegame.spirit.Player
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.view.CrossRocker
import com.bamboo.boxspacegame.view.MapBackground
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var soundPool: SoundPool
    private val mapSound = SparseIntArray()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initButtons()
        initCrossRocker()
        initSurfaceView()
        initEventBus()
        initMediaPlayer()
        initSoundPool()
    }

    private fun initEventBus() {
        LiveEventBus.get(AppGobal.EVENT_STAGE_NO, Int::class.java).observe(this) {
            tvStageNo.text = "$it"
        }
        LiveEventBus.get(AppGobal.EVENT_SCORE, Int::class.java).observe(this) {
            tvScore.text = "$it"
        }
    }

    private fun initButtons() {
        initBackButton()
        initPauseButton()
        initResetButton()
        initFireButton()
        initJumpButton()
        initBGMButton()
        initSFXButton()
    }

    private fun initBackButton() {
        ibtnBack.setOnClickListener { onBackPressed() }
    }

    private fun initPauseButton() {
        btnPause.setOnClickListener {
            if (btnPause.text == "暂停") {
                AppGobal.pause = true
                btnPause.text = "继续"
            } else {
                AppGobal.pause = false
                btnPause.text = "暂停"
            }
        }
    }

    private fun initResetButton() {
        btnReset.setOnClickListener {
            AppGobal.pause = false
            btnPause.text = "暂停"
            StageManager.reset()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFireButton() {
        btnFire.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    Player.lockAngle()
                    Player.sendBullet(true)
                    btnFire.setBackgroundResource(R.drawable.shape_pressed_true)
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    Player.unlockAngle()
                    Player.sendBullet(false)
                    btnFire.setBackgroundResource(R.drawable.shape_pressed_false)
                }
            }
            true
        }
    }

    private fun initJumpButton() {
        btnJump.setOnClickListener { Player.jump() }
    }

    private fun initBGMButton() {
        btnBGM.setOnClickListener {
            if (btnBGM.text == "音乐：开") {
                mediaPlayer.stop()
                btnBGM.text = "音乐：关"
            } else {
                mediaPlayer.prepare()
                mediaPlayer.start()
                btnBGM.text = "音乐：开"
            }
        }
    }

    private fun initSFXButton() {
        btnSFX.setOnClickListener {
            btnSFX.text = if (btnSFX.text == "音效：开") "音效：关" else "音效：开"
        }
    }

    /**
     * 初始化背景音乐
     */
    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm)
        mediaPlayer.isLooping = true
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                mediaPlayer.start()
            }
        })
    }

    /**
     * 初始化游戏音效
     * 游戏中音效的播放是通过事件总线现实，在此方法里订阅事件，在需要播放的时候发送事件
     */
    private fun initSoundPool() {
        soundPool = SoundPool.Builder().setMaxStreams(256).build()
        mapSound.put(1, soundPool.load(this, R.raw.bomb_sfx, 1))
        mapSound.put(2, soundPool.load(this, R.raw.bullet_sfx, 1))
        mapSound.put(3, soundPool.load(this, R.raw.flash_sfx, 1))
        LiveEventBus.get(AppGobal.EVENT_BOMB_SFX, Boolean::class.java).observe(this) {
            if (btnSFX.text == "音效：开")
                soundPool.play(mapSound[1], 1f, 1f, 0, 0, 1f)
        }
        LiveEventBus.get(AppGobal.EVENT_BULLET_SFX, Boolean::class.java).observe(this) {
            if (btnSFX.text == "音效：开")
                soundPool.play(mapSound[2], 1f, 1f, 0, 0, 1f)
        }
        LiveEventBus.get(AppGobal.EVENT_FLASH_SFX, Boolean::class.java).observe(this) {
            if (btnSFX.text == "音效：开")
                soundPool.play(mapSound[3], 1f, 1f, 0, 0, 1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /**
     * 全屏
     */
    private fun hideStatusBar() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            decorView.systemUiVisibility = uiOptions
        }
    }

    /**
     * 初始化游戏十字键
     */
    private fun initCrossRocker() {
        rocker.setActionListener { _, direction, evt ->
            val angle = when (direction) {
                CrossRocker.TOP -> 270f
                CrossRocker.TOP_RIGHT -> 315f
                CrossRocker.RIGHT -> 0f
                CrossRocker.BOTTOM_RIGHT -> 45f
                CrossRocker.BOTTOM -> 90f
                CrossRocker.BOTTOM_LEFT -> 135f
                CrossRocker.LEFT -> 180f
                CrossRocker.TOP_LEFT -> 225f
                else -> 0f
            }
            Player.angle = angle
            when (evt.actionMasked) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    Player.actionMove()
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    Player.actionRelease()
                }
            }
        }
    }

    private fun initSurfaceView() {
        surfaceView.holder.let {
            it.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    launch(Dispatchers.IO) {
                        BulletManager.init(this@MainActivity)
                        Player.init(this@MainActivity)
                        StageManager.init(this@MainActivity)
                        while (true) {
                            val canvas = holder.lockCanvas()
                            MapBackground.draw(canvas)
                            BulletManager.draw(canvas)
                            EffectManager.draw(canvas)
                            StageManager.draw(canvas)
                            Player.draw(canvas)
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    AppGobal.screenWidth = surfaceView.width
                    AppGobal.screenHeight = surfaceView.height
                    AppGobal.unitSize = AppGobal.screenWidth / 20f
                    MapBackground.init()
                    BulletEffect.init()
                    FlashEffect.init()
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {}
            })
        }
    }
}
