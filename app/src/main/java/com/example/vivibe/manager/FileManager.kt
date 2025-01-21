package com.example.vivibe.manager

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class FileManager(private val context: Context) {
    fun saveToJson(userId: String, searches: List<String>) {
        try {
            val fileName = "${userId}_preferences.json"
            val data = JSONObject().put("recentSearches", JSONArray(searches))

            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { stream ->
                stream.write(data.toString().toByteArray())
            }
            Log.d("FileManager", "Successfully saved searches for user: $userId")
        } catch (e: Exception) {
            Log.e("FileManager", "Error saving searches", e)
        }
    }

    fun loadFromJson(userId: String): List<String> {
        try {
            val fileName = "${userId}_preferences.json"
            context.openFileInput(fileName).use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonString)
                val jsonArray = jsonObject.getJSONArray("recentSearches")
                return List(jsonArray.length()) { jsonArray.getString(it) }
            }
        } catch (e: Exception) {
            Log.e("FileManager", "Error loading searches", e)
            return emptyList()
        }
    }

    fun clearJson(userId: String) {
        try {
            val fileName = "${userId}_preferences.json"
            context.deleteFile(fileName)
            Log.d("FileManager", "Successfully cleared searches for user: $userId")
        } catch (e: Exception) {
            Log.e("FileManager", "Error clearing searches", e)
        }
    }
}