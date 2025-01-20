package com.example.vivibe.api.song

import com.example.vivibe.model.ArtistAlbum
import com.example.vivibe.model.FullInfoArtist
import com.example.vivibe.model.GenreSongs
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SpeedDialSong
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class LikeRequest(val isLiked: Boolean)

data class APIResponse(
    val err: Int,
    val msg: String
)

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

data class ArtistAlbumResponse(
    val err: Int,
    val msg: String,
    val data: List<ArtistAlbum>
)

data class FullInfoArtistResponse(
    val err: Int,
    val msg: String,
    val data: FullInfoArtist
)

data class ArtistAlbumRequest(
    val artistIds: List<Int>
)

data class GenreSongsResponse(
    val err: Int,
    val msg: String,
    val data: List<GenreSongs>? = null
)

data class PlayingSongResponse(
    val err: Int,
    val msg: String,
    val data: List<PlaySong>? = null
)

data class DownloadedSongResponse(
    val err: Int,
    val msg: String,
    val data: PlaySong?
)

interface SongInterface {
    @GET("/api/v1/song/get-speed-dial")
    suspend fun fetchSpeedDialSongs(): Response<SpeedDialResponse>

    @POST("/api/v1/song/get-quick-picks")
    suspend fun fetchQuickPickSongs(@Body quickPicksRequest: QuickPicksRequest): Response<QuickPicksResponse>

    @GET("/api/v1/song/get-new-releases")
    suspend fun fetchNewRelease(): Response<QuickPicksResponse>

    @POST("/api/v1/song/get-albums")
    suspend fun fetchAlbums(@Body artistAlbumRequest: ArtistAlbumRequest): Response<ArtistAlbumResponse>

    @POST("/api/v1/song/get-play-all")
    suspend fun fetchPlayAll(@Body quickPicksRequest: QuickPicksRequest): Response<PlayingSongResponse>

    @GET("/api/v1/song/get-artist-and-album")
    suspend fun fetchArtistAndAlbum(@Query("artistId") artistId: Int): Response<FullInfoArtistResponse>

    @GET("/api/v1/song/get-songs-by-genre")
    suspend fun fetchSongsByGenre(@Query("genreId") genreId: Int): Response<GenreSongsResponse>

    @PUT("/api/v1/song/update-likes/{id}")
    suspend fun updateLikes(
        @Path("id") songId: String,
        @Body request: LikeRequest
    ): Response<APIResponse>

    @GET("/api/v1/song/get-detail-song")
    suspend fun fetchPlayingSong(@Query("songId") songId: Int): Response<PlayingSongResponse>

    @GET("/api/v1/song/get-downloaded-song")
    suspend fun fetchDownloadedSong(@Query("songId") songId: Int): Response<DownloadedSongResponse>
    @GET("/api/v1/song/search")
    suspend fun searchSongAndArtist(@Query("query") query: String): Response<QuickPicksResponse>
}
