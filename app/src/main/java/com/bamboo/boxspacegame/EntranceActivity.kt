package com.bamboo.boxspacegame

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Size
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
import com.bamboo.boxspacegame.spirit.Player
import com.bamboo.boxspacegame.utils.LogEx
import kotlinx.android.synthetic.main.activity_entrance.*
import kotlinx.android.synthetic.main.item_record.view.*

class EntranceActivity : AppCompatActivity() {
    private val listRecord = mutableListOf<RecordBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance)
        initLogo()
        initButtons()
        initRecyclerView()
        initOptionAudio()
    }

    /**
     * 用代码生成图标
     */
    private fun initLogo() {
        ivLogo.post {
            val bmp = Bitmap.createBitmap(ivLogo.width, ivLogo.height, Bitmap.Config.ARGB_8888)
            Canvas(bmp).apply {
                val paint = Paint()
                paint.color = Color.WHITE
                paint.shader = RadialGradient(
                    bmp.width / 2f, 0f, bmp.height.toFloat(),
                    intArrayOf(Color.WHITE, Color.DKGRAY), null,
                    Shader.TileMode.CLAMP
                )
                val path = Path()
                path.moveTo(bmp.width / 2f, 0f)
                path.lineTo(bmp.width.toFloat(), (bmp.height - (bmp.height / 3)).toFloat())
                path.lineTo(bmp.width / 2f, bmp.height.toFloat())
                path.lineTo(0f, (bmp.height - (bmp.height / 3)).toFloat())
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
                    holder.itemView.tvRecord.text =
                        "第${stageNo}关 最快用时：${fastestTime} 上次用时：${lastTime}"
                }
            }

            override fun getItemCount(): Int {
                return listRecord.size
            }
        }
    }

    private fun initOptionAudio() {
        switchBGM.setOnClickListener {
            MyApp.saveSoundOption(switchBGM.isChecked, MyApp.isPlaySFX())
        }
        switchSFX.setOnClickListener {
            MyApp.saveSoundOption(MyApp.isPlayBGM(), switchSFX.isChecked)
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                switchBGM.isChecked = MyApp.isPlayBGM()
                switchSFX.isChecked = MyApp.isPlaySFX()
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