package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.model.TransferRecord
import com.example.model.TransferType
import org.json.JSONArray
import org.json.JSONObject

class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fastdrop_history", Context.MODE_PRIVATE)

    fun getRecords(): List<TransferRecord> {
        val raw = prefs.getString("records_json", null) ?: return emptyList()
        val list = mutableListOf<TransferRecord>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TransferRecord(
                        id = obj.optString("id"),
                        fileName = obj.optString("fileName"),
                        fileSize = obj.optLong("fileSize"),
                        type = TransferType.valueOf(obj.optString("type", TransferType.RECEIVE.name)),
                        timestamp = obj.optLong("timestamp"),
                        isSuccess = obj.optBoolean("isSuccess", true),
                        localFilePath = if (obj.has("localFilePath")) obj.optString("localFilePath") else null,
                        peerAddress = obj.optString("peerAddress", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun addRecord(record: TransferRecord) {
        val current = getRecords().toMutableList()
        current.add(0, record)
        val trimmed = if (current.size > 50) current.take(50) else current

        val arr = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("fileName", item.fileName)
            obj.put("fileSize", item.fileSize)
            obj.put("type", item.type.name)
            obj.put("timestamp", item.timestamp)
            obj.put("isSuccess", item.isSuccess)
            if (item.localFilePath != null) obj.put("localFilePath", item.localFilePath)
            obj.put("peerAddress", item.peerAddress)
            arr.put(obj)
        }
        prefs.edit().putString("records_json", arr.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("records_json").apply()
    }
}
