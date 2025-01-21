package com.example.vivibe.api.comment

import android.content.Context
import com.example.vivibe.model.CommentItem
import com.example.vivibe.model.User

class CommentClient(context: Context, token: String) {
    private val tag = "CommentClient: "
    private val commentService = CommentService(context, token)

    suspend fun initializeSongBranch(songId: Int): Boolean {
        return try {
            val response = commentService.initializeSongBranch(songId)
            if (response?.err == 0) {
                println("$tag Song branch initialized successfully")
                true
            } else {
                println("$tag Error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            false
        }
    }

    suspend fun addComment(songId: Int, userId: String, content: String, parentCommentId: Int? = null): CommentItem? {
        return try {
            val response = commentService.addComment(songId, userId, content, parentCommentId)
            if (response?.err == 0) {
                response.data?.also {
                    println("$tag Comment added successfully")
                }
            } else {
                println("$tag Error: ${response?.msg}")
                null
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            null
        }
    }

    suspend fun deleteComment(songId: Int, commentId: Int): Boolean {
        return try {
            val response = commentService.deleteComment(songId, commentId)
            if (response?.err == 0) {
                println("$tag Comment deleted successfully")
                true
            } else {
                println("$tag Error: ${response?.msg}")
                false
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            false
        }
    }

    suspend fun getSongComments(songId: Int): List<CommentItem> {
        return try {
            val response = commentService.getSongComments(songId)
            if (response?.err == 0) {
                response.data?.map { comment ->
                    CommentItem(
                        id = comment.id,
                        songId = comment.songId,
                        content = comment.content,
                        depth = comment.depth,
                        likes = comment.likes,
                        createdAt = comment.createdAt,
                        countReplies = comment.countReplies,
                        user = User(
                            id = comment.user.id,
                            name = comment.user.name,
                            profilePictureUri = comment.user.profilePictureUri
                        )
                    )
                } ?: emptyList()
            } else {
                println("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getCommentWithReplies(songId: Int, commentId: Int): List<CommentItem> {
        return try {
            val response = commentService.getCommentWithReplies(songId, commentId)
            if (response?.err == 0) {
                response.data?.map { comment ->
                    CommentItem(
                        id = comment.id,
                        songId = comment.songId,
                        content = comment.content,
                        depth = comment.depth,
                        likes = comment.likes,
                        createdAt = comment.createdAt,
                        countReplies = comment.countReplies,
                        user = User(
                            id = comment.user.id,
                            name = comment.user.name,
                            profilePictureUri = comment.user.profilePictureUri
                        )
                    )
                } ?: emptyList()
            } else {
                println("$tag Error: ${response?.msg}")
                emptyList()
            }
        } catch (e: Exception) {
            println("$tag Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getTotalComment(songId: Int): Int {
        return try {
            val response = commentService.getTotalComment(songId)
            if (response?.err == 0) {
                response.data
            } else {
                0
            }
        }catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}