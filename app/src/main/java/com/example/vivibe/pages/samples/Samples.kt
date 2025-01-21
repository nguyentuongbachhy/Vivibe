package com.example.vivibe.pages.samples

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vivibe.R
import com.example.vivibe.components.comment.CommentViewModel
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SwipeSong
import com.example.vivibe.router.ArtistRouter
import com.example.vivibe.router.HomeRouter
import com.example.vivibe.router.SearchRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Samples {
    @Composable
    fun SamplesScreen(
        viewModel: SampleViewModel,
        commentViewModel: CommentViewModel,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        switchToMainPlayer: (Int) -> Unit,
        navController: NavController,
        onOpenComment: (Int) -> Unit
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        viewModel.onScreenPause()
                        viewModel.resetPlayer()
                    }
                    Lifecycle.Event.ON_RESUME -> viewModel.onScreenResume()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                viewModel.resetPlayer()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.fetchSwipeSongs()
        }

        val songs by viewModel.listSong.collectAsState()
        val currentSongId by viewModel.currentSongId.collectAsState()
        val isPlaying by viewModel.isPlaying.collectAsState()
        val currentIndex by viewModel.currentIndex.collectAsState()
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
                .background(Color(0xFF101010))
        ) {
            if(songs.isNotEmpty()) {
                SongsPager(
                    commentViewModel = commentViewModel,
                    songs = songs,
                    currentIndex = currentIndex,
                    currentSongId = currentSongId,
                    isPlaying = isPlaying,
                    viewModel = viewModel,
                    onSongMoreClick = onSongMoreClick,
                    switchToMainPlayer = switchToMainPlayer,
                    onOpenComment = onOpenComment,
                    navController = navController
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF101010), // Màu gốc ở trên
                                Color(0xFF101010).copy(alpha = 0.8f), // Giảm độ trong suốt
                                Color.Transparent // Trong suốt ở dưới
                            )
                        )
                    )
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(32.dp).clickable {
                        scope.launch {
                            navController.navigate(HomeRouter.route) {
                                popUpTo(HomeRouter.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "For You",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.size(32.dp).clickable {
                        scope.launch {
                            navController.navigate(SearchRouter.route)
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SongsPager(
        commentViewModel: CommentViewModel,
        songs: List<SwipeSong>,
        currentIndex: Int,
        currentSongId: Int,
        isPlaying: Boolean,
        viewModel: SampleViewModel,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        switchToMainPlayer: (Int) -> Unit,
        onOpenComment: (Int) -> Unit,
        navController: NavController
    ) {
        val pagerState = rememberPagerState(
            initialPage = currentIndex,
            pageCount = { songs.size }
        )

        val commentCount by remember { mutableIntStateOf(0) }

        LaunchedEffect(currentIndex) {
            if (pagerState.currentPage != currentIndex) {
                commentViewModel.fetchTotalComments(songs[currentIndex].id)
                pagerState.animateScrollToPage(currentIndex)
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            viewModel.onPageChanged(pagerState.currentPage)
        }

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            SongCard(
                song = songs[page],
                isCurrentSong = songs[page].id == currentSongId,
                isPlaying = isPlaying,
                onMoreClick = {
                    onSongMoreClick(
                        QuickPicksSong(
                            id = songs[page].id,
                            title = songs[page].title,
                            artist = songs[page].artist,
                            thumbnailUrl = songs[page].thumbnailUrl,
                            views = songs[page].views
                        )
                    )
                },
                onPlayClick = { viewModel.playPause() },
                onLongPress = { switchToMainPlayer(songs[page].id) },
                onOpenComment = onOpenComment,
                commentCount = commentCount,
                navController = navController
            )
        }
    }

    @Composable
    private fun SongCard(
        song: SwipeSong,
        isCurrentSong: Boolean,
        isPlaying: Boolean,
        onMoreClick: () -> Unit,
        onPlayClick: () -> Unit,
        onLongPress: () -> Unit,
        onOpenComment: (Int) -> Unit,
        commentCount: Int,
        navController: NavController,
        scope: CoroutineScope = rememberCoroutineScope()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Content
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        scope.launch {
                            navController.navigate("${ArtistRouter.route}/${song.artist.id}")
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {

                AsyncImage(
                    model = song.artist.thumbnail,
                    contentDescription = song.artist.name,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = song.artist.name,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Right side controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isCurrentSong && isPlaying) R.drawable.ic_pause_filled
                            else R.drawable.ic_play_filled
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(0.3f), CircleShape)
                        .clickable {
                            onOpenComment(song.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = if(commentCount > 0) convertToShortString(commentCount) else "",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }


                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onLongPress,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_out_full_screen),
                        contentDescription = "Full screen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun convertToShortString(number: Int): String {
        return when {
            number < 1000 -> number.toString()
            number < 1000000 -> String.format("%.1fK", number / 1000f)
            else -> String.format("%.1fM", number / 1000000f)
        }
    }
}