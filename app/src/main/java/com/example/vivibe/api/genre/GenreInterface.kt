package com.example.vivibe.api.genre

import com.example.vivibe.model.Genre
import retrofit2.Response
import retrofit2.http.GET


data class GenreResponse(
    val err: Int,
    val msg: String,
    val genres: List<Genre>? = null
)


interface GenreInterface {
    @GET("/api/v1/genre/get-genres")
    suspend fun fetchGenres(): Response<GenreResponse>
}