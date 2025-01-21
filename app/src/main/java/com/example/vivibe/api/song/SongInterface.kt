package com.example.vivibe.api.song

import com.example.vivibe.model.ArtistAlbum
import com.example.vivibe.model.FullInfoAlbum
import com.example.vivibe.model.FullInfoArtist
import com.example.vivibe.model.FullInfoPlaylist
import com.example.vivibe.model.Genre
import com.example.vivibe.model.GenreSongs
import com.example.vivibe.model.NameAndSongs
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SongDetail
import com.example.vivibe.model.SpeedDialSong
import com.example.vivibe.model.SwipeSong
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

data class FullInfoAlbumResponse(
    val err: Int,
    val msg: String,
    val data: FullInfoAlbum
)

data class FullInfoPlaylistResponse(
    val err: Int,
    val msg: String,
    val data: FullInfoPlaylist
)


data class ArtistAlbumRequest(
    val artistIds: List<Int>
)

data class GenreSongsResponse(
    val err: Int,
    val msg: String,
    val data: List<GenreSongs>? = null
)

data class NameAndSongsResponse(
    val err: Int,
    val msg: String,
    val data: NameAndSongs? = null
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

data class SwipeSongsRequest(
    val genreIds: List<Int>
)

data class SwipeSongsResponse(
    val err: Int,
    val msg: String,
    val data: List<SwipeSong>? = null
)

data class LikedSongsResponse(
    val err: Int,
    val msg: String,
    val data: List<SongDetail>
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

    @GET("/api/v1/song/get-detail-album")
    suspend fun fetchDetailAlbum(@Query("albumId") albumId: Int): Response<FullInfoAlbumResponse>

    @GET("/api/v1/song/get-detail-playlist")
    suspend fun fetchDetailPlaylist(@Query("playlistId") playlistId: Int): Response<FullInfoPlaylistResponse>

    @GET("/api/v1/song/get-liked-songs")
    suspend fun getLikedSongs(@Query("userId") userId: String) : Response<LikedSongsResponse>

    @GET("/api/v1/song/get-detail-song")
    suspend fun fetchPlayingSong(@Query("songId") songId: Int): Response<PlayingSongResponse>

    @GET("/api/v1/song/get-downloaded-song")
    suspend fun fetchDownloadedSong(@Query("songId") songId: Int): Response<DownloadedSongResponse>

    @GET("/api/v1/song/get-name-and-songs")
    suspend fun fetchNameAndSongs(@Query("genreId") genreId: Int): Response<NameAndSongsResponse>

    @POST("/api/v1/song/get-swipe-songs")
    suspend fun fetchSwipeSongs(@Body swipeSongsRequest: SwipeSongsRequest): Response<SwipeSongsResponse>
}
