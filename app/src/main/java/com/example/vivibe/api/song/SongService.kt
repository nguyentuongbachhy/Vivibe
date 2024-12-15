package com.example.vivibe.api.song

import android.content.Context
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient


class SongService(context: Context, token: String) {
    private val api: SongInterface

    init {
        val baseURL:String = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(SongInterface::class.java)
    }

    suspend fun fetchSpeedDialSongs(): SpeedDialResponse? {
        return try {
            val response = api.fetchSpeedDialSongs()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchQuickPickSongs(songIds: List<Int>) : QuickPicksResponse? {
        return try {
            val response = api.fetchQuickPickSongs(QuickPicksRequest(songIds))
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}