package com.bamboo.boxspacegame

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.bamboo.boxspacegame.effect.BombEffect
import com.bamboo.boxspacegame.effect.BulletEffect
import com.bamboo.boxspacegame.effect.EffectManager
import com.bamboo.boxspacegame.effect.FlashEffect
import com.bamboo.boxspacegame.record.RecordManager
import com.bamboo.boxspacegame.spirit.BulletManager
import com.bamboo.boxspacegame.spirit.Player
import com.bamboo.boxspacegame.stage.StageManager
import com.bamboo.boxspacegame.view.CrossRocker
import com.bamboo.boxspacegame.view.MapBackground
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.*

class GameActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var soundPool: SoundPool
    private var mediaPlayer: MediaPlayer? = null
    private val mapSound = SparseIntArray()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        initCrossRocker()
        initSurfaceView()
        initEventBus()
        initMediaPlayer()
        initSoundPool()
        initButtons()
    }

    override fun onPause() {
        super.onPause()
        stopBGM()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppGobal.isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        soundPool.release()
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun onResume() {
        super.onResume()
        if (chkBGM.isChecked) playBGM()
    }

    private fun initEventBus() {
        LiveEventBus.get(AppGobal.EVENT_STAGE_NO, Int::class.java).observe(this) {
            tvStageNo.text = "$it"
        }
        LiveEventBus.get(AppGobal.EVENT_FASTEST_TIME, Boolean::class.java).observe(this) {
            val record = RecordManager.getStageRecord(StageManager.getCurrentStageNo())
            tvFastestTime.text = record?.fastestTime?.toString() ?: "0"
        }
        LiveEventBus.get(AppGobal.EVENT_CURRENT_TIME, Long::class.java).observe(this) {
            tvUseTime.text = "$it"
        }
    }

    private fun initButtons() {
        initBackButton()
        initPauseButton()
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
        chkBGM.isChecked = MyApp.isPlayBGM()
        chkBGM.text = if (chkBGM.isChecked) "音乐：开" else "音乐：关"
        chkBGM.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                stopBGM()
                chkBGM.text = "音乐：关"
            } else {
                playBGM()
                chkBGM.text = "音乐：开"
            }
            MyApp.saveSoundOption(isChecked, MyApp.isPlaySFX())
        }
    }

    private fun playBGM() {
        if (mediaPlayer == null) initMediaPlayer()
        mediaPlayer?.start()
    }

    private fun stopBGM() {
        mediaPlayer?.stop()
        mediaPlayer = null
    }

    private fun initSFXButton() {
        chkSFX.isChecked = MyApp.isPlaySFX()
        chkSFX.text = if (chkSFX.isChecked) "音效：开" else "音效：关"
        chkSFX.setOnCheckedChangeListener { _, isChecked ->
            chkSFX.text = if (isChecked) "音效：开" else "音效：关"
            MyApp.saveSoundOption(MyApp.isPlayBGM(), isChecked)
        }
    }

    /**
     * 初始化背景音乐
     */
    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm2)
        mediaPlayer?.isLooping = true
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
            if (chkSFX.text == "音效：开")
                soundPool.play(mapSound[1], 1f, 1f, 0, 0, 1f)
        }
        LiveEventBus.get(AppGobal.EVENT_BULLET_SFX, Boolean::class.java).observe(this) {
            if (chkSFX.text == "音效：开")
                soundPool.play(mapSound[2], 1f, 1f, 0, 0, 1f)
        }
        LiveEventBus.get(AppGobal.EVENT_FLASH_SFX, Boolean::class.java).observe(this) {
            if (chkSFX.text == "音效：开")
                soundPool.play(mapSound[3], 1f, 1f, 0, 0, 1f)
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
            it.setKeepScreenOn(true)
            it.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    launch(Dispatchers.Default) {
                        AppGobal.screenWidth = surfaceView.width
                        AppGobal.screenHeight = surfaceView.height
                        AppGobal.unitSize = AppGobal.screenWidth / 20f
                        MapBackground.init()
                        BulletEffect.init()
                        FlashEffect.init()
                        BombEffect.init()
                        BulletManager.init(this@GameActivity)
                        Player.initScope(this@GameActivity)
                        StageManager.init(this@GameActivity)
                        while (AppGobal.isRunning) {
                            val canvas = holder.lockCanvas()
                            MapBackground.draw(canvas)
                            StageManager.draw(canvas)
                            BulletManager.draw(canvas)
                            EffectManager.draw(canvas)
                            Player.draw(canvas)
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    AppGobal.isRunning = true
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {}
            })
        }
    }

}
