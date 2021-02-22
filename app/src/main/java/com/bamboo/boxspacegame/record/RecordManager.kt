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

    /**
     * 保存关卡记录到本地
     */
    fun saveStageRecord(
        stageNo: Int,
        startMillis: Long,
        endMillis: Long,
        listRecord: MutableList<RecordBean>
    ) {
        val time = endMillis - startMillis
        // 从集合获取关卡对象，如果对象为空则创建并加入集合
        val bean = listRecord.find { it.stageNo == stageNo }
            ?: RecordBean(stageNo, time, time).apply { listRecord += this }
        if (time < bean.fastestTime) {
            bean.fastestTime = time
        }
        // 序列化集合数据到本地文件
        MyApp.context?.getSharedPreferences("record", Context.MODE_PRIVATE)
            ?.edit {
                this.putString("records", RecordBean.toJson(listRecord))
            }
    }

    /**
     * 清空本地的关卡记录
     */
    fun clearStageRecord() {
        MyApp.context?.getSharedPreferences("record", Context.MODE_PRIVATE)
            ?.edit { clear() }
    }
}