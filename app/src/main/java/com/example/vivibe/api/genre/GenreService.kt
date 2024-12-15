package com.example.vivibe.api.genre

import android.content.Context
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient

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
}