package com.bamboo.boxspacegame

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bamboo.boxspacegame.effect.*
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
        initViews()
    }

    override fun onPause() {
        super.onPause()
        stopBGM()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppGobal.isLooping = false
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
        if (OptionHelper.isPlayBGM(applicationContext)) playBGM()
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
            progressBarPower.progress = Player.power
            btnBomb.isEnabled = Player.power == AppGobal.POWER_MAX
            btnJump.isEnabled = Player.power >= 50
        }
        LiveEventBus.get(AppGobal.EVENT_GAME_OVER, Boolean::class.java).observe(this) {
            onBackPressed()
        }
    }

    private fun initViews() {
        initBackButton()
        initPauseButton()
        initFireButton()
        initBombButton()
        initJumpButton()
        progressBarPower.max = AppGobal.POWER_MAX
        progressBarPower.progress = 0
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
                    btnFire.setTextColor(ContextCompat.getColor(this, R.color.text_btn_color))
                    btnFire.setBackgroundResource(R.drawable.shape_pressed_true)
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    Player.unlockAngle()
                    Player.sendBullet(false)
                    btnFire.setTextColor(ContextCompat.getColor(this, R.color.text_btn_color))
                    btnFire.setBackgroundResource(R.drawable.shape_pressed_false)
                }
            }
            true
        }
    }

    private fun initBombButton() {
        btnBomb.setOnClickListener {
            Player.sendBomb()
        }
    }

    private fun initJumpButton() {
        btnJump.setOnClickListener { Player.jump() }
    }

    private fun playBGM() {
        if (mediaPlayer == null) initMediaPlayer()
        mediaPlayer?.start()
    }

    private fun stopBGM() {
        mediaPlayer?.stop()
        mediaPlayer = null
    }

    /**
     * 初始化背景音乐
     */
    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm3)
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
        LiveEventBus.get(AppGobal.EVENT_BOMB_SFX, Boolean::class.java)
            .observe(this) {
                if (OptionHelper.isPlaySFX(applicationContext))
                    soundPool.play(mapSound[1], 1f, 1f, 0, 0, 1f)
            }
        LiveEventBus.get(AppGobal.EVENT_BULLET_SFX, Boolean::class.java)
            .observe(this) {
                if (OptionHelper.isPlaySFX(applicationContext))
                    soundPool.play(mapSound[2], 1f, 1f, 0, 0, 1f)
            }
        LiveEventBus.get(AppGobal.EVENT_FLASH_SFX, Boolean::class.java)
            .observe(this) {
                if (OptionHelper.isPlaySFX(applicationContext))
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
        AppGobal.screenHeight = resources.displayMetrics.heightPixels
        AppGobal.screenWidth = AppGobal.screenHeight / 20 * 28
        AppGobal.unitSize = AppGobal.screenWidth / 20f
        // 调整SurfaceView的大小
        surfaceView.layoutParams = LinearLayout.LayoutParams(
            AppGobal.screenWidth,
            AppGobal.screenHeight
        )
        surfaceView.holder.let {
            it.setKeepScreenOn(true) // 保存屏幕常亮
            it.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    launch(Dispatchers.Default) {
                        while (AppGobal.isLooping) {
                            val canvas = holder.lockCanvas()
                            // 记录帧开始的时间
                            val startMillis = System.currentTimeMillis()
                            drawFrame(canvas)
                            // 是否需要显示FPS
                            if (OptionHelper.isShowFPS(applicationContext))
                                drawFPS(canvas, startMillis, System.currentTimeMillis())
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    AppGobal.isLooping = true
                    val canvas = holder.lockCanvas()
                    canvas?.let {
                        drawTitleString(it, "游戏加载中...")
                        holder.unlockCanvasAndPost(it)
                    }

                    MapBackground.init()
                    BulletEffect.init()
                    FlashEffect.init()
                    BombEffect.init()
                    GrenadeEffect.init()
                    BulletManager.init(this@GameActivity)
                    Player.init(this@GameActivity)
                    StageManager.init(
                        this@GameActivity,
                        OptionHelper.isEnableEnemyAttack(applicationContext)
                    )
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    AppGobal.isLooping = false
                }
            })
        }
    }

    private fun drawFrame(canvas: Canvas) {
        MapBackground.draw(canvas)
        StageManager.draw(canvas)
        BulletManager.draw(canvas)
        EffectManager.draw(canvas)
        Player.draw(canvas)
    }

    private fun dp2px(dp: Float): Float {
        return MyApp.context?.let {
            dp * it.resources.displayMetrics.density + 0.5f
        } ?: 0f
    }

    /**
     * 绘制主标题和子标题，屏幕居中
     */
    private fun drawTitleString(canvas: Canvas, subTitle: String) {
        // 绘制主标题文字，屏幕居中偏上
        val title = "Space Battle"
        val rectTitle = Rect()
        val paintTitle = Paint().apply {
            this.style = Paint.Style.FILL_AND_STROKE
            this.color = Color.WHITE
            this.textSize = dp2px(22f)
            this.getTextBounds(title, 0, title.length - 1, rectTitle)
        }
        val titleX = (AppGobal.screenWidth - rectTitle.width()) / 2f
        val titleY = AppGobal.screenHeight / 2f - dp2px(20f)
        canvas.drawText(title, titleX, titleY, paintTitle)
        // 绘制分隔线
        canvas.drawLine(
            AppGobal.screenWidth / 4f,
            AppGobal.screenHeight / 2f,
            AppGobal.screenWidth - (AppGobal.screenWidth / 4f),
            AppGobal.screenHeight / 2f,
            paintTitle
        )
        // 绘制子标题文字，屏幕居中偏下
        val rectSubTitle = Rect()
        val paintSubTitle = Paint().apply {
            this.style = Paint.Style.FILL_AND_STROKE
            this.color = Color.WHITE
            this.textSize = dp2px(16f)
            this.getTextBounds(subTitle, 0, subTitle.length - 1, rectSubTitle)
        }
        val subTitleX = (AppGobal.screenWidth - rectSubTitle.width()) / 2f
        val subTitleY = AppGobal.screenHeight / 2f + dp2px(30f)
        canvas.drawText(subTitle, subTitleX, subTitleY, paintSubTitle)
    }

    private val paintFPS = Paint()
    private fun drawFPS(canvas: Canvas, startMillis: Long, endMillis: Long) {
        paintFPS.let {
            it.color = Color.WHITE
            it.style = Paint.Style.FILL
            it.textSize = dp2px(14f)
        }
        val fps = 1000 / (endMillis - startMillis)
        canvas.drawText("FPS:$fps", 10f, dp2px(20f), paintFPS)
    }
}
