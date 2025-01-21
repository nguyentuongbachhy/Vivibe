package com.example.vivibe.pages.artist

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vivibe.R
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SongDetail
import com.example.vivibe.router.SearchRouter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class Artist {
    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class)
    @Composable
    fun ArtistScreen(viewModel: ArtistViewModel, artistId: Int, exoPlayer: SharedExoPlayer, navController: NavController, onSongMoreClick: (QuickPicksSong) -> Unit, onPlayMusicNavigate: (Int) -> Unit) {
        LaunchedEffect(artistId) {
            viewModel.fetchFullInfoArtist(artistId)
            viewModel.fetchFollowStatus(artistId)
        }

        val fullInfoArtist by viewModel.fullInfoArtist.collectAsState()
        val isFollowed by viewModel.isFollowed.collectAsState()
        val isFollowLoading by viewModel.isFollowLoading.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()

        if(isLoading) {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            return
        }


        if(error != null) {
            Text(
                text = error ?: "Unknown error",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            return
        }

        fullInfoArtist?.let {

            val context = LocalContext.current
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current

            val screenHeight = with(density) {
                configuration.screenHeightDp.dp.toPx()
            }

            val swipeableArea = screenHeight - 400

            val motionScene = remember {
                context.resources.openRawResource(R.raw.artist_profile_motion_scene)
                    .readBytes()
                    .decodeToString()
            }

            val swipeableState = rememberSwipeableState(0)
            val anchors = mapOf(
                0f to 0,
                -swipeableArea to 1
            )

            val progress = min(abs(swipeableState.offset.value / swipeableArea), 1f)

            var expanded by remember { mutableStateOf(false) }
            val scrollState = rememberScrollState()
            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return when {
                            available.y < 0 && swipeableState.currentValue == 0 -> {
                                Offset(0f, swipeableState.performDrag(available.y))
                            }

                            available.y > 0 && scrollState.value == 0 -> {
                                Offset(0f, swipeableState.performDrag(available.y))
                            }

                            else -> Offset.Zero
                        }
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return when {
                            available.y > 0 && scrollState.value == 0 -> {
                                Offset(0f, swipeableState.performDrag(available.y))
                            }

                            else -> Offset.Zero
                        }
                    }

                    override suspend fun onPostFling(
                        consumed: Velocity,
                        available: Velocity
                    ): Velocity {
                        when {
                            available.y < 0 && swipeableState.currentValue == 0 -> {
                                swipeableState.animateTo(1)
                            }

                            available.y > 0 && scrollState.value == 0 -> {
                                swipeableState.animateTo(0)
                            }
                        }
                        return available
                    }
                }
            }

            val currentSongId = exoPlayer.currentSongId.collectAsState()
            val scope = rememberCoroutineScope()

            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)) {
                MotionLayout(
                    motionScene = MotionScene(content = motionScene),
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                ) {

                    val duration = it.songs.sumOf { item -> item.duration }

                    AsyncImage(
                        model = it.artist.thumbnail,
                        contentDescription = it.artist.name,
                        modifier = Modifier
                            .layoutId("poster")
                            .alpha((1f - progress).pow(5))
                            .height(
                                with(density) {
                                    (screenHeight / 2).toDp()
                                }
                            ),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .layoutId("overlay")
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF101010).copy(0.9f),
                                        Color(0xFF101010),
                                        Color(0xFF101010)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .layoutId("navigationBar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    navController.popBackStack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    navController.navigate(SearchRouter.route)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(
                        text = it.artist.name,
                        fontSize = (40f - 16f * progress).sp,
                        modifier = Modifier.layoutId("title"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.layoutId("followButton"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ) {
                        Box(modifier = Modifier.height(40.dp).width(108.dp).layoutId("followButton").background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp)).clickable {
                            scope.launch {
                                viewModel.handleFollow(artistId)
                            }
                        }) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                if(isFollowLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        painter = painterResource(if(isFollowed) R.drawable.ic_follow_filled else R.drawable.ic_follow_outline),
                                        contentDescription = "Follow_Unfollow",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Text(
                                        text = if(isFollowed) "Followed" else "Follow",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${convertIntegerToString(it.artist.followers.toLong())} followers",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = it.artist.description,
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .layoutId("description")
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layoutId("content")
                            .swipeable(
                                state = swipeableState,
                                anchors = anchors,
                                thresholds = { _, _ -> FractionalThreshold(0.2f) },
                                velocityThreshold = 300.dp,
                                orientation = Orientation.Vertical
                            )
                            .nestedScroll(nestedScrollConnection)
                            .verticalScroll(scrollState)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
                        ) {
                            it.songs.forEach { song ->
                                AlbumSongItem(
                                    currentSongId = currentSongId,
                                    artist = it.artist,
                                    song = song,
                                    onSongMoreClick = onSongMoreClick,
                                    onClick = { songId ->
                                        onPlayMusicNavigate(songId)
                                        viewModel.updatePlayHistory(songId)
                                        viewModel.updateArtistPlayHistory(artistId)
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                Text(
                                    text = "${it.songs.size} songs - ${convertSecond(duration)}",
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color.White, CircleShape)
                                        .clickable {
                                            val songIds = it.songs.map { song -> song.id }
                                            viewModel.fetchPlayAll(songIds)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_play_outline),
                                        contentDescription = "Play",
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(160.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF101010))
                            .layoutId("player")
                    )

                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertSecond(second: Int): String {
        val hours = second / 3600
        val minutes = (second % 3600)  / 60
        if(hours > 0)
            return String.format("%02d hours %02d minutes", hours, minutes)
        return String.format("%02d minutes", minutes)
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun AlbumSongItem(
        currentSongId: State<Int>,
        artist: ArtistDetail,
        song: SongDetail,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onClick: (Int) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 64.dp)
                .padding(0.dp)
                .background(
                    if (song.id == currentSongId.value) Color.DarkGray else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(end = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClick(song.id)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${artist.name} - ${convertSecondToMS(song.duration)} - ${convertIntegerToString(song.views)} views",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            FloatingActionButton(
                onClick = {
                    val quickPicksSong = QuickPicksSong(
                        id = song.id,
                        artist = ArtistReview(id = artist.id, name = artist.name, thumbnail = ""),
                        thumbnailUrl = song.thumbnailUrl,
                        title = song.title,
                        views = song.views
                    )
                    onSongMoreClick(quickPicksSong)
                },
                modifier = Modifier.size(24.dp),
                containerColor = Color.Transparent,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertSecondToMS(second: Int): String {
        val minutes = second / 60
        val seconds = second % 60
        return String.format("%02d:%02d", minutes, seconds)
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
}