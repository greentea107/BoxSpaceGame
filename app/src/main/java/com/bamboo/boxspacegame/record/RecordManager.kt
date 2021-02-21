package com.bamboo.boxspacegame.record

import android.content.Context
import androidx.core.content.edit
import com.bamboo.boxspacegame.MyApp

object RecordManager {
    /**
     * 加载通关记录
     */
    fun loadStageRecord(): List<RecordBean> {
        val json = MyApp.context
            ?.getSharedPreferences("record", Context.MODE_PRIVATE)
            ?.getString("records", "")
        return if (json.isNullOrEmpty()) emptyList() else RecordBean.arrayBeanFromData(json)
    }

    fun getStageRecord(stageNo: Int): RecordBean? {
        val json = MyApp.context
            ?.getSharedPreferences("record", Context.MODE_PRIVATE)
            ?.getString("records", "")
        if (json.isNullOrEmpty()) return null
        val list = RecordBean.arrayBeanFromData(json)
        return list.find { it.stageNo == stageNo }
    }

    fun saveStageRecord(
        stageNo: Int,
        startMillis: Long,
        endMillis: Long,
        listRecord: MutableList<RecordBean>
    ) {
        val time = endMillis - startMillis
        val bean = RecordBean(stageNo, 0, time)
        if (getStageRecord(stageNo) == null || getStageRecord(stageNo)?.fastestTime ?: 0 < time) {
            bean.fastestTime = time
        }
        listRecord += bean
        MyApp.context
            ?.getSharedPreferences("record", Context.MODE_PRIVATE)
            ?.edit {
                this.putString("records", RecordBean.toJson(listRecord))
            }
    }

    fun clearStageRecord() {
        MyApp.context
            ?.getSharedPreferences("record", Context.MODE_PRIVATE)
            ?.edit { clear() }
    }
}