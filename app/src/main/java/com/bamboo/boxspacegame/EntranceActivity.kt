package com.bamboo.boxspacegame

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bamboo.boxspacegame.record.RecordBean
import com.bamboo.boxspacegame.record.RecordManager
import kotlinx.android.synthetic.main.activity_entrance.*
import kotlinx.android.synthetic.main.item_record.view.*

/**
 * 游戏入口界面
 */
class EntranceActivity : AppCompatActivity() {
    private val listRecord = mutableListOf<RecordBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance)
        initLogoAndVersion()
        initButtons()
        initRecyclerView()
        initOptionAudio()
    }

    /**
     * 用代码生成图标
     */
    private fun initLogoAndVersion() {
        ivLogo.post {
            val bmp = Bitmap.createBitmap(ivLogo.width, ivLogo.height, Bitmap.Config.ARGB_8888)
            Canvas(bmp).apply {
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = Color.WHITE
                paint.shader = RadialGradient(
                    bmp.width / 2f, 0f, bmp.height * 1f,
                    intArrayOf(Color.WHITE, Color.DKGRAY), null,
                    Shader.TileMode.CLAMP
                )
                val path = Path()
                path.moveTo(bmp.width / 2f, 0f)
                path.lineTo(bmp.width * 1f, bmp.height - (bmp.height / 3f))
                path.lineTo(bmp.width / 2f, bmp.height * 1f)
                path.lineTo(0f, bmp.height - (bmp.height / 3f))
                path.close()
                this.drawPath(path, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                paint.shader = null
                paint.color = Color.WHITE
                paint.strokeJoin = Paint.Join.ROUND
                this.drawPath(path, paint)
                this.drawLine(
                    bmp.width / 2f,
                    0f,
                    bmp.width / 2f,
                    bmp.height.toFloat(),
                    paint
                )
            }
            ivLogo.setImageBitmap(bmp)
        }
        tvVersion.text = "ver ${packageManager.getPackageInfo(packageName, 0).versionName}"
    }

    private fun initButtons() {
        ibtnExit.setOnClickListener { onBackPressed() }
        btnEntrance.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            overridePendingTransition(0, 0)
        }
        btnClearScore.setOnClickListener {
            RecordManager.clearStageRecord()
            listRecord.clear()
            rvData.adapter?.notifyDataSetChanged()
        }
    }

    /**
     * 初始化统计列表
     */
    private fun initRecyclerView() {
        rvData.setHasFixedSize(true)
        rvData.layoutManager = LinearLayoutManager(this)
        rvData.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val view =
                    LayoutInflater.from(MyApp.context).inflate(R.layout.item_record, null, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                listRecord[position].let {
                    val stageNo = it.stageNo
                    val fastestTime = it.fastestTime
                    val lastTime = it.time
                    holder.itemView.let {
                        it.tvStageNo.text = "第${stageNo}关 - "
                        it.tvFastestTime.text = " 最快用时（毫秒）：${fastestTime}"
                        it.tvLastTime.text = " 上次用时（毫秒）：${lastTime}"
                    }
                }
            }

            override fun getItemCount(): Int {
                return listRecord.size
            }
        }
    }

    private fun initOptionAudio() {
        switchBGM.setOnClickListener {
            val isPlaySFX = OptionHelper.isPlaySFX(this)
            OptionHelper.saveSoundOption(this, switchBGM.isChecked, isPlaySFX)
        }
        switchSFX.setOnClickListener {
            val isPlayBGM = OptionHelper.isPlayBGM(this)
            OptionHelper.saveSoundOption(this, isPlayBGM, switchSFX.isChecked)
        }
        switchFPS.setOnClickListener {
            OptionHelper.saveShowFPS(this, switchFPS.isChecked)
        }
        switchEnemyAttack.setOnClickListener {
            OptionHelper.saveEnableEnemyAttack(this, switchEnemyAttack.isChecked)
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                switchBGM.isChecked = OptionHelper.isPlayBGM(applicationContext)
                switchSFX.isChecked = OptionHelper.isPlaySFX(applicationContext)
                switchEnemyAttack.isChecked = OptionHelper.isEnableEnemyAttack(applicationContext)
                switchFPS.isChecked = OptionHelper.isShowFPS(applicationContext)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        RecordManager.loadStageRecord().let {
            listRecord.clear()
            listRecord.addAll(it.sortedBy { it.stageNo }.filter { it.time != 0L })
            rvData.adapter?.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}