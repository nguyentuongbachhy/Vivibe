package com.example.vivibe.api.song

import android.content.Context
import com.example.vivibe.QuickPicksSong
import com.example.vivibe.SpeedDialSong

class SongClient(context: Context, token: String) {
    private val tag = "SongClient: "
    private val songService = SongService(context, token)

    suspend fun fetchSpeedDialSongs(): List<SpeedDialSong> {
        return try {
            val response = songService.fetchSpeedDialSongs()
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag Songs fetched successfully")
                } ?: emptyList()
            } else {
                println("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchQuickPickSongs(songIds: List<Int>): List<QuickPicksSong> {
        return try {
            val response = songService.fetchQuickPickSongs(songIds)
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag Songs fetched successfully")
                } ?: emptyList()
            } else {
                print("$tag Error: ${response?.msg}")
                emptyList()
            }
        }catch (e: Exception) {
            print("$tag Error: ${e.message}")
            emptyList()
        }

    }
}