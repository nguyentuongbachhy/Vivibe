package com.example.vivibe.api.genre

import android.content.Context

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
}