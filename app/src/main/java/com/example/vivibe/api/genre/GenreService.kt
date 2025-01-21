package com.example.vivibe.api.genre

import android.content.Context
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient
import com.example.vivibe.model.Genre

class GenreService(context: Context, token:String) {
    private val api: GenreInterface

    init {
        val baseURL:String = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(GenreInterface::class.java)
    }

    suspend fun fetchGenres(): GenreResponse? {
        return try {
            val response = api.fetchGenres()
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

    suspend fun fetchGenresSong(songId: Int): GenreResponse? {
        return try {
            val response = api.fetchGenresSong(songId)
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

    suspend fun fetchGenreByIds(genreIds: List<Int>): GenreResponse? {
        return try {
            val response = api.fetchGenreByIds(TagForYouRequest(genreIds))
            if(response.isSuccessful) {
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