package com.example.vivibe.api.user

import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.PlaylistReview
import com.example.vivibe.model.SongDetail
import com.example.vivibe.model.SongHistory
import com.example.vivibe.pages.search.Search
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ToggleLikeRequest(
    val userId: String,
    val songId: String
)

data class ToggleFollowRequest(
    val userId: String,
    val artistId: String
)

data class LikeResponse(
    val err: Int,
    val msg: String,
    val liked: Boolean
)

data class FollowResponse(
    val err: Int,
    val msg: String,
    val followed: Boolean
)

data class UpgradeRequest(
    val userId: String
)

data class UpgradeResponse(
    val err: Int,
    val msg: String,
    val premium: Int
)

data class UpdateHistoryRequest(
    val userId: String,
    val songId: Int
)

data class UpdateHistoryResponse(
    val err: Int,
    val msg: String,
    val updated: Boolean
)

data class PlaylistsResponse(
    val err: Int,
    val msg: String,
    val playlists: List<PlaylistReview>? = null
)

data class LikedArtistsResponse(
    val err: Int,
    val msg: String,
    val artists: List<ArtistDetail>? = null
)

data class SearchResponse(
    val err: Int,
    val msg: String,
    val songs: List<SongDetail>? = null
)

data class HistoriesResponse(
    val err: Int,
    val msg: String,
    val histories: List<SongHistory>? = null
)


interface UserInterface {
    @POST("/api/v1/user/toggle-like")
    suspend fun toggleLike(
        @Body request: ToggleLikeRequest
    ): Response<LikeResponse>

    @GET("/api/v1/user/get-like-status")
    suspend fun getLikeStatus(
        @Query("userId") userId: String,
        @Query("songId") songId: String
    ): Response<LikeResponse>

    @POST("/api/v1/user/toggle-follow")
    suspend fun toggleFollow(
        @Body request: ToggleFollowRequest
    ): Response<FollowResponse>

    @GET("/api/v1/user/get-follow-status")
    suspend fun getFollowStatus(
        @Query("userId") userId: String,
        @Query("artistId") artistId: String
    ): Response<FollowResponse>

    @GET("/api/v1/user/get-playlists")
    suspend fun getPlaylists(
        @Query("userId") userId: String
    ) : Response<PlaylistsResponse>

    @GET("/api/v1/user/get-liked-artists")
    suspend fun getLikedArtists(
        @Query("userId") userId: String
    ): Response<LikedArtistsResponse>

    @GET("/api/v1/user/search")
    suspend fun search(@Query("keyword") keyword: String) : Response<SearchResponse>

    @GET("/api/v1/user/get-histories")
    suspend fun getHistories(@Query("userId") userId: String): Response<HistoriesResponse>

    @POST("/api/v1/user/upgrade-to-premium")
    suspend fun upgradeToPremium(@Body request: UpgradeRequest): Response<UpgradeResponse>

    @POST("/api/v1/user/update-history")
    suspend fun updateHistory(@Body request: UpdateHistoryRequest): Response<UpdateHistoryResponse>
}
