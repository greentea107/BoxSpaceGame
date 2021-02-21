package com.bamboo.boxspacegame.record

import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class RecordBean(var stageNo: Int, var fastestTime: Long, var time: Long) {
    companion object {
        fun arrayBeanFromData(str: String): List<RecordBean> {
            val listType: Type = object : TypeToken<ArrayList<RecordBean>>() {}.type
            return Gson().fromJson(str, listType)
        }

        fun toJson(list: List<RecordBean>): String {
            val listType: Type = object : TypeToken<ArrayList<RecordBean>>() {}.type
            return Gson().toJson(list, listType)
        }
    }
}