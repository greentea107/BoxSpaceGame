package com.bamboo.boxspacegame

import android.content.Intent
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

class EntranceActivity : AppCompatActivity() {
    private val listRecord = mutableListOf<RecordBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrance)
        initButtons()
        initRecyclerView()
        initOptionAudio()
    }

    private fun initButtons() {
        ibtnExit.setOnClickListener { onBackPressed() }
        btnEntrance.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
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
            listRecord.addAll(it)
            rvData.adapter?.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}