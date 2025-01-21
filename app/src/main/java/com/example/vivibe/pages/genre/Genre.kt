package com.example.vivibe.pages.genre

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.vivibe.R
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.router.ExploreRouter
import com.example.vivibe.router.SearchRouter
import kotlinx.coroutines.launch

class Genre {
    @Composable
    fun GenreScreen(
        viewModel: GenreViewModel,
        genreId: Int,
        navController: NavHostController,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onPlayMusicNavigate: (Int) -> Unit
    ) {
        val isLoading by viewModel.isLoading.collectAsState()
        var hasFetched by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(genreId) {
            if (!hasFetched) {
                viewModel.initialize(genreId)
                hasFetched = true
            }
        }

        if (isLoading) {
            LoadingIndicator()
            return
        }

        val songs by viewModel.songs.collectAsState()
        val genreName by viewModel.genreName.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 64.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationIcon(
                    iconRes = R.drawable.ic_back,
                    description = "Back"
                ) {
                    scope.launch {
                        navController.navigate(ExploreRouter.route)
                    }
                }

                Text(
                    text = genreName.replaceFirstChar { it.uppercaseChar() },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                NavigationIcon(
                    iconRes = R.drawable.ic_search,
                    description = "Search"
                ) {
                    scope.launch {
                        navController.navigate(SearchRouter.route)
                    }
                }
            }

            Text(
                text = "Songs",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TopSongs(
                songs = songs,
                viewModel = viewModel,
                onSongMoreClick = onSongMoreClick,
                onPlayMusicNavigate = onPlayMusicNavigate
            )
        }
    }

    @Composable
    private fun TopSongs(
        songs: List<QuickPicksSong>,
        viewModel: GenreViewModel,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onPlayMusicNavigate: (Int) -> Unit
    ) {
        val scope = rememberCoroutineScope()

        val itemsPerColumn = 4

        val pages = songs.chunked(itemsPerColumn)

        if (pages.isEmpty()) {
            return
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.Top
        ) {
            pages.forEach { songs ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 8.dp)
                ) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(4),
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .heightIn(max = 270.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(songs) { song ->
                            TopSongItem(
                                song = song,
                                onSongMoreClick = {
                                    scope.launch {
                                        onSongMoreClick(it)
                                    }
                                },
                                onClick = {
                                    scope.launch {
                                        onPlayMusicNavigate(song.id)
                                        viewModel.updatePlayHistory(song.id)
                                        viewModel.updateArtistPlayHistory(song.artist.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun TopSongItem(
        song: QuickPicksSong,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .heightIn(max = 60.dp)
                .padding(0.dp)
                .padding(end = 8.dp)
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${song.artist.name} - ${convertIntegerToString(song.views)} views",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            FloatingActionButton(
                onClick = { onSongMoreClick(song) },
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

    @Composable
    private fun NavigationIcon(
        iconRes: Int,
        description: String,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .myOwnClickable(onClick = onClick)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = description,
                tint = Color.White
            )
        }
    }


    @Composable
    private fun LoadingIndicator() {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
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

    @Composable
    private fun Modifier.myOwnClickable(
        enabled: Boolean = true,
        onClick: () -> Unit
    ): Modifier = this.clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}