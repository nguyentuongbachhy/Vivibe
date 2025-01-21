package com.example.vivibe.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vivibe.MainViewModel
import com.example.vivibe.R
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SongDetail
import com.example.vivibe.model.SongHistory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("INTEGER_OVERFLOW")
class History {
    @Composable
    fun HistoryScreen(
        navController: NavController,
        mainViewModel: MainViewModel,
        exoPlayer: SharedExoPlayer,
    ) {
        val scope = rememberCoroutineScope()
        var histories by remember { mutableStateOf<List<SongHistory>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        val userManager = UserManager.getInstance(LocalContext.current)
        val userClient = UserClient(LocalContext.current, userManager.getToken())

        // Fetch histories
        LaunchedEffect(Unit) {
            try {
                val userId = userManager.getId() ?: return@LaunchedEffect
                histories = userClient.getHistories(userId)
            } catch (e: Exception) {
                Log.e("History", "Error fetching histories", e)
            } finally {
                isLoading = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
        ) {
            // Top Bar
            TopBar(navController)

            if (isLoading) {
                LoadingScreen()
            } else if (histories.isEmpty()) {
                EmptyHistoryScreen()
            } else {
                HistoryContent(
                    histories = histories,
                    onSongClick = { song ->
                        scope.launch {
                            mainViewModel.fetchPlaySong(song.id)
                        }
                    },
                    onMoreClick = { song ->
                        mainViewModel.showBottomSheet(song.toQuickPicksSong()!!)
                    }
                )
            }
        }
    }

    @Composable
    private fun TopBar(navController: NavController) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF202020))
                    .clickable { navController.navigateUp() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier.size(36.dp)
            )
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }

    @Composable
    private fun EmptyHistoryScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "No listening history",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Songs you listen to will appear here",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    private fun HistoryContent(
        histories: List<SongHistory>,
        onSongClick: (SongHistory) -> Unit,
        onMoreClick: (SongHistory) -> Unit
    ) {
        val timeBasedHistories = histories.groupBy { song ->
            getTimeCategory(song.lastPlayedAt)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            timeBasedHistories.forEach { (timeCategory, songs) ->
                item {
                    Text(
                        text = timeCategory,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(songs) { song ->
                    HistoryItem(
                        song = song,
                        onSongClick = { onSongClick(song) },
                        onMoreClick = { onMoreClick(song) }
                    )
                }
            }
        }
    }

    @Composable
    private fun HistoryItem(
        song: SongHistory,
        onSongClick: () -> Unit,
        onMoreClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSongClick),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF202020)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = song.artist?.name ?: "",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = getRelativeTime(song.lastPlayedAt),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End
                )

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
            }
        }
    }

    private fun getTimeCategory(timestamp: Date): String {
        val now = Date()
        val diff = now.time - timestamp.time
        return when {
            diff < 24 * 60 * 60 * 1000 -> "Today"
            diff < 7 * 24 * 60 * 60 * 1000 -> "This week"
            diff < 30 * 24 * 60 * 60 * 1000 -> "This month"
            else -> "Older"
        }
    }

    private fun getRelativeTime(timestamp: Date): String {
        val now = Date()
        val diff = now.time - timestamp.time
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(timestamp)
        }
    }

    private fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun SongHistory.toQuickPicksSong() = artist?.let {
        QuickPicksSong(
            id = id,
            title = title,
            thumbnailUrl = thumbnailUrl,
            views = views,
            artist = it
        )
    }
}