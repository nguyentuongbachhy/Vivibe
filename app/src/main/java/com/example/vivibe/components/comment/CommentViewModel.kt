package com.example.vivibe.components.comment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.comment.CommentClient
import com.example.vivibe.model.CommentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel(
    context: Context,
    token: String
): ViewModel() {

    private val commentClient = CommentClient(context, token)

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    private val _replyingTo = MutableStateFlow<CommentItem?>(null)
    val replyingTo: StateFlow<CommentItem?> = _replyingTo.asStateFlow()

    private val _totalComments = MutableStateFlow(0)
    val totalComments: StateFlow<Int> = _totalComments.asStateFlow()

    fun initializeSongBranch(songId: Int) {
        viewModelScope.launch {
            try {
                commentClient.initializeSongBranch(songId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchTotalComments(songId: Int) {
        viewModelScope.launch {
            try {
                val total = commentClient.getTotalComment(songId)
                _totalComments.value = total
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchComments(songId: Int) {
        viewModelScope.launch {
            try {
                val fetchedComments = commentClient.getSongComments(songId)
                _comments.value = fetchedComments
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun addComment(songId: Int, userId: String, content: String, parentCommentId: Int? = null): CommentItem? {
        return try {
            val newComment = commentClient.addComment(songId, userId, content, parentCommentId)

            if (parentCommentId == null && newComment != null) {
                _comments.value = listOf(newComment) + _comments.value
            }
            _totalComments.value += 1
            newComment
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateComments(newComments: List<CommentItem>) {
        _comments.value = newComments
    }

    fun updateCommentCounters(commentId: Int?, increment: Boolean = true) {
        if (commentId != null) {
            _comments.value = _comments.value.map { comment ->
                if (comment.id == commentId) {
                    comment.copy(
                        countReplies = comment.countReplies + if (increment) 1 else -1
                    )
                } else comment
            }
        }
    }

    suspend fun deleteComment(songId: Int, commentId: Int, parentCommentId: Int? = null): Boolean {
        return try {
            val result = commentClient.deleteComment(songId, commentId)
            if (result) {
                _totalComments.value -= 1
                fetchComments(songId)
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCommentWithReplies(songId: Int, commentId: Int): List<CommentItem>? {
        return try {
            commentClient.getCommentWithReplies(songId, commentId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setReplyingTo(comment: CommentItem?) {
        _replyingTo.value = comment
    }
}