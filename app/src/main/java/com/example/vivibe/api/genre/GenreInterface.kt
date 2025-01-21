package com.example.vivibe.api.genre

import com.example.vivibe.model.Genre
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class TagForYouRequest(
    val genreIds: List<Int>
)

data class GenreResponse(
    val err: Int,
    val msg: String,
    val genres: List<Genre>? = null
)

data class PlaylistReviewResponse(
    val err: Int,
    val msg: String,

)

interface GenreInterface {
    @GET("/api/v1/genre/get-genres")
    suspend fun fetchGenres(): Response<GenreResponse>

    @GET("/api/v1/genre/get-genres-song")
    suspend fun fetchGenresSong(
        @Query("songId") songId: Int
    ): Response<GenreResponse>

    @POST("/api/v1/genre/get-genre-by-ids")
    suspend fun fetchGenreByIds(@Body genreIds: TagForYouRequest) : Response<GenreResponse>
}