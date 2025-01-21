package com.example.vivibe.components.comment

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.vivibe.R
import com.example.vivibe.model.CommentItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    songId: Int,
    userId: String,
    onDismissRequest: () -> Unit,
    viewModel: CommentViewModel
) {
    val comments by viewModel.comments.collectAsState()
    val replyingTo by viewModel.replyingTo.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var currentReplyCallback by remember { mutableStateOf<((CommentItem) -> Unit)?>(null) }

    LaunchedEffect(songId) {
        viewModel.initializeSongBranch(songId)
        viewModel.fetchComments(songId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF202020),
        dragHandle = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Comments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(modifier = Modifier.size(24.dp).clip(CircleShape).clickable { onDismissRequest() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(comments) { comment ->
                    CommentViewItem(
                        userId = userId,
                        comment = comment,
                        onReply = { targetComment, addReplyCallback ->
                            viewModel.setReplyingTo(targetComment)
                            currentReplyCallback = addReplyCallback
                        },
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Input Area
            Column {
                if (replyingTo != null) {
                    ReplyingToBar(
                        comment = replyingTo!!,
                        onCancelReply = {
                            viewModel.setReplyingTo(null)
                            currentReplyCallback = null
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material.TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF303030), RoundedCornerShape(24.dp)),
                        placeholder = {
                            Text(
                                "Add a comment...",
                                color = Color.Gray
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.White,
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 5
                    )

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.viewModelScope.launch {
                                    val newComment = viewModel.addComment(
                                        songId = songId,
                                        userId = userId,
                                        content = commentText,
                                        parentCommentId = replyingTo?.id
                                    )

                                    if (newComment != null) {
                                        if (replyingTo == null) {
                                            // Nếu là root comment, cập nhật toàn bộ danh sách
                                            viewModel.updateComments(listOf(newComment) + comments)
                                        } else {
                                            // Nếu là reply, gọi callback để cập nhật local state
                                            currentReplyCallback?.invoke(newComment)
                                        }

                                        commentText = ""
                                        viewModel.setReplyingTo(null)
                                        currentReplyCallback = null
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "Send",
                            tint = if (commentText.isBlank()) Color.Gray else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CommentViewItem(
    userId: String,
    comment: CommentItem,
    onReply: (CommentItem, (CommentItem) -> Unit) -> Unit,
    parentCommentId: Int? = null,
    viewModel: CommentViewModel,
    modifier: Modifier = Modifier
) {
    var showReplies by remember { mutableStateOf(false) }
    var replies by remember { mutableStateOf<List<CommentItem>>(emptyList()) }
    var isLoadingReplies by remember { mutableStateOf(false) }
    var hasLoadedReplies by remember { mutableStateOf(false) }
    var localCommentCount by remember { mutableStateOf(comment.countReplies) }

    val addReplyToList = { newComment: CommentItem ->
        replies = listOf(newComment) + replies
        localCommentCount++  // Tăng counter local
        viewModel.updateCommentCounters(comment.id, true)  // Tăng counter trong ViewModel
    }

    // Function để xóa reply khỏi danh sách
    val removeReplyFromList = { commentId: Int ->
        replies = replies.filter { it.id != commentId }
        localCommentCount--  // Giảm counter local
        viewModel.updateCommentCounters(comment.id, false)  // Giảm counter trong ViewModel
    }

    // Handle click vào replies
    val handleRepliesClick = {
        if (!hasLoadedReplies) {
            isLoadingReplies = true
            viewModel.viewModelScope.launch {
                viewModel.getCommentWithReplies(comment.songId, comment.id)?.let {
                    replies = it.filter { reply -> reply.id != comment.id }
                    localCommentCount = replies.size
                    hasLoadedReplies = true
                }
                isLoadingReplies = false
            }
        }
        showReplies = !showReplies
    }

    val handleReply = { targetComment: CommentItem ->
        onReply(targetComment, if (targetComment.id == comment.id) addReplyToList else replies.find { it.id == targetComment.id }?.let { _ ->
            // Nếu reply tới một reply khác trong danh sách
            { newReply: CommentItem ->
                replies = listOf(newReply) + replies
            }
        } ?: {})
    }

    // Handle delete comment
    val handleDelete = { commentToDelete: CommentItem ->
        viewModel.viewModelScope.launch {
            val success = viewModel.deleteComment(
                songId = commentToDelete.songId,
                commentId = commentToDelete.id,
                parentCommentId = if (commentToDelete.id == comment.id) null else comment.id
            )
            if (success) {
                if (commentToDelete.id == comment.id) {
                    // Không cần làm gì vì comment sẽ được xóa khỏi UI bởi parent
                } else {
                    // Xóa khỏi danh sách replies
                    removeReplyFromList(commentToDelete.id)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = ((comment.depth - 2) * 16).dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = comment.user.profilePictureUri,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${comment.user.name} - " + formatTimestamp(comment.createdAt),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = comment.content,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 36.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
            ) {
                Box(
                    modifier = Modifier.size(14.dp).clickable {  }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_like_outline),
                        contentDescription = "Like",
                        tint = Color.White
                    )
                }
                if(comment.likes > 0) {
                    Text(
                        text = convertIntegerToString(comment.likes.toLong()),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier.size(14.dp).clickable { handleReply(comment) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_reply),
                    contentDescription = "Reply",
                    tint = Color.White
                )
            }

            if(userId == comment.user.id) {
                Box(
                    modifier = Modifier.size(14.dp).clickable { handleDelete(comment) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }

        if(localCommentCount > 0 || replies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .clickable(enabled = !isLoadingReplies) { handleRepliesClick() }
                    .padding(start = 36.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${convertIntegerToString(localCommentCount.toLong())} replies",
                    color = Color.Cyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                )

                if (isLoadingReplies) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = Color.Cyan,
                        strokeWidth = 2.dp
                    )
                }
            }

            if (showReplies) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    replies.forEach { reply ->
                        CommentViewItem(
                            userId = userId,
                            comment = reply,
                            onReply = onReply,
                            parentCommentId = comment.id,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ReplyingToBar(
    comment: CommentItem,
    onCancelReply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF303030))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Replying to ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = comment.user.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onCancelReply) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Cancel Reply",
                tint = Color.White
            )
        }
    }
}


private fun formatTimestamp(timestamp: Date): String {
    val now = Calendar.getInstance()
    val commentTime = Calendar.getInstance().apply { time = timestamp }

    return when {
        now.get(Calendar.YEAR) != commentTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(timestamp)
        }
        now.get(Calendar.DAY_OF_YEAR) - commentTime.get(Calendar.DAY_OF_YEAR) > 7 -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(timestamp)
        }
        now.get(Calendar.DAY_OF_YEAR) != commentTime.get(Calendar.DAY_OF_YEAR) -> {
            "${now.get(Calendar.DAY_OF_YEAR) - commentTime.get(Calendar.DAY_OF_YEAR)}d ago"
        }
        now.get(Calendar.HOUR_OF_DAY) != commentTime.get(Calendar.HOUR_OF_DAY) -> {
            "${now.get(Calendar.HOUR_OF_DAY) - commentTime.get(Calendar.HOUR_OF_DAY)}h ago"
        }
        now.get(Calendar.MINUTE) != commentTime.get(Calendar.MINUTE) -> {
            "${now.get(Calendar.MINUTE) - commentTime.get(Calendar.MINUTE)}m ago"
        }
        else -> "Just now"
    }
}

@SuppressLint("DefaultLocale")
private fun convertIntegerToString(number: Long): String {
    if (number < 1000) return number.toString()

    val suffixes = arrayOf("", "K", "M", "B")
    var value = number.toDouble()
    var index = 0

    while (value >= 1000 && index < suffixes.size - 1) {
        value /= 1000
        index++
    }

    return if (value >= 10) {
        "${value.toInt()}${suffixes[index]}"
    } else {
        String.format("%.1f%s", value, suffixes[index])
    }
}