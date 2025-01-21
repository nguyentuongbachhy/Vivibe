package com.example.vivibe.api.comment

import com.example.vivibe.model.CommentItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class APIResponse(
    val err: Int,
    val msg: String,
    val data: CommentItem? = null
)

data class CommentResponse(
    val err: Int,
    val msg: String,
    val data: List<CommentItem>? = null
)

data class TotalCommentResponse(
    val err: Int,
    val msg: String,
    val data: Int = 0
)

data class AddCommentRequest(
    val songId: Int,
    val userId: String,
    val content: String,
    val parentCommentId: Int? = null
)

interface CommentInterface {
    @GET("/api/v1/comment/initialize-song-branch")
    suspend fun initializeSongBranch(
        @Query("songId") songId: Int
    ): Response<APIResponse>


    @POST("/api/v1/comment/add-comment")
    suspend fun addComment(
        @Body request: AddCommentRequest
    ): Response<APIResponse>

    @DELETE("/api/v1/comment/delete-comment/{songId}/{commentId}")
    suspend fun deleteComment(
        @Path("songId") songId: Int,
        @Path("commentId") commentId: Int
    ): Response<APIResponse>

    @GET("/api/v1/comment/get-comments-song/{songId}")
    suspend fun getSongComments(
        @Path("songId") songId: Int
    ): Response<CommentResponse>

    @GET("/api/v1/comment/get-replies/{songId}/{commentId}/replies")
    suspend fun getCommentWithReplies(
        @Path("songId") songId: Int,
        @Path("commentId") commentId: Int
    ): Response<CommentResponse>

    @GET("/api/v1/comment/get-total-comment")
    suspend fun getTotalComment(
        @Query("songId") songId: Int
    ): Response<TotalCommentResponse>
}

