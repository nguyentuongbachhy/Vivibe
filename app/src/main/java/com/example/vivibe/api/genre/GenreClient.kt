package com.example.vivibe.api.genre

import android.content.Context
import android.util.Log

import com.example.vivibe.model.Genre

class GenreClient(context: Context, token: String?) {
    private val tag = "GenreClient: "
    private val genreService = token?.let { GenreService(context, it) }

    suspend fun fetchGenres(): List<Genre> {
        if(genreService == null) return emptyList()
        return try {
            val response = genreService.fetchGenres()
            if (response?.err == 0) {
                response.genres?.also {
                    println("$tag Genres fetched successfully")
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

    suspend fun fetchGenresSong(songId: Int): List<Int> {
        if(genreService == null) return emptyList()

        return try {
            val response = genreService.fetchGenresSong(songId)
            if(response?.err == 0) {
                Log.d(tag, "Genres fetched successfully: $response")
                response.genres?.map { it.id } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchGenreByIds(genreIds: List<Int>): List<Genre> {
        if(genreService == null) return emptyList()
        return try {
            val response = genreService.fetchGenreByIds(genreIds)
            if(response?.err == 0) {
                Log.d(tag, "Genres fetched successfully: $response")
                response.genres ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}