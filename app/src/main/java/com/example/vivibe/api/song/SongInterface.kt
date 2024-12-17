package com.example.vivibe.api.song

import com.example.vivibe.PlaySong
import com.example.vivibe.QuickPicksSong
import com.example.vivibe.SpeedDialSong
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


data class SpeedDialResponse(
    val err: Int,
    val msg: String,
    val data: List<SpeedDialSong>? = null
)

data class QuickPicksRequest(
    val songIds: List<Int>
)

data class QuickPicksResponse(
    val err: Int,
    val msg: String,
    val data: List<QuickPicksSong>? = null
)

data class PlayingSongResponse(
    val err: Int,
    val msg: String,
    val data: PlaySong? = null
)

interface SongInterface {
    @GET("/api/v1/song/get-speed-dial")
    suspend fun fetchSpeedDialSongs(): Response<SpeedDialResponse>

    @POST("/api/v1/song/get-quick-picks")
    suspend fun fetchQuickPickSongs(@Body quickPicksRequest: QuickPicksRequest): Response<QuickPicksResponse>

    @GET("/api/v1/song/get-detail-song")
    suspend fun fetchPlayingSong(@Query("songId") songId: Int): Response<PlayingSongResponse>
}
