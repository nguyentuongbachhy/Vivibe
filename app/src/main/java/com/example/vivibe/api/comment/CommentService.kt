package com.example.vivibe.api.comment

import android.content.Context
import com.example.vivibe.R
import com.example.vivibe.api.RetrofitClient

class CommentService(context: Context, token: String) {
    private val api: CommentInterface

    init {
        val baseURL = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(CommentInterface::class.java)
    }

    suspend fun initializeSongBranch(songId: Int): APIResponse? {
        return try {
            val response = api.initializeSongBranch(songId)
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

    suspend fun addComment(songId: Int, userId: String, content: String, parentCommentId: Int? = null): APIResponse? {
        return try {
            val request = AddCommentRequest(songId, userId, content, parentCommentId)
            val response = api.addComment(request)
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

    suspend fun deleteComment(songId: Int, commentId: Int): APIResponse? {
        return try {
            val response = api.deleteComment(songId, commentId)
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

    suspend fun getSongComments(songId: Int): CommentResponse? {
        return try {
            val response = api.getSongComments(songId)
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

    suspend fun getCommentWithReplies(songId: Int, commentId: Int): CommentResponse? {
        return try {
            val response = api.getCommentWithReplies(songId, commentId)
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

    suspend fun getTotalComment(songId: Int): TotalCommentResponse? {
        return try {
            val response = api.getTotalComment(songId)
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