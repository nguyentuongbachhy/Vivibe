package com.example.vivibe.api.common


import com.example.vivibe.model.AlbumReview
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.QuickPicksSong
import retrofit2.Response
import retrofit2.http.GET

data class LatestReleaseResponse(
    val err: Int,
    val msg: String,
    val data: AlbumReview
)

data class AlbumsSongsResponse(
    val err: Int,
    val msg: String,
    val albums: List<AlbumReview>,
    val artists: List<ArtistReview>
)

data class TopSongsResponse(
    val err: Int,
    val msg: String,
    val data: List<QuickPicksSong>? = null
)

data class TopArtistsResponse(
    val err: Int,
    val msg: String,
    val data: List<ArtistDetail>? = null
)

interface CommonInterface {
    @GET("/api/v1/new-release/get-latest-album")
    suspend fun getLatestAlbum() : Response<LatestReleaseResponse>

    @GET("/api/v1/new-release/get-albums-artists")
    suspend fun getAlbumsAndSongs(): Response<AlbumsSongsResponse>

    @GET("/api/v1/song/get-top-songs")
    suspend fun fetchTopSongs(): Response<TopSongsResponse>

    @GET("/api/v1/song/get-top-artists")
    suspend fun fetchTopArtists(): Response<TopArtistsResponse>
}