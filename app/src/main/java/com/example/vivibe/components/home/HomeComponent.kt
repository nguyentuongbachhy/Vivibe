package com.example.vivibe.components.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vivibe.model.Genre
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.R
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.model.ArtistAlbum
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.GenreSong
import com.example.vivibe.model.GenreSongs
import com.example.vivibe.model.SongDetail
import com.example.vivibe.model.SongReview
import com.example.vivibe.model.User
import com.example.vivibe.router.ArtistRouter
import kotlinx.coroutines.launch

class HomeComponent(private val viewModel: HomeComponentViewModel, private val exoPlayer: SharedExoPlayer) {
    private val currentSongId = exoPlayer.currentSongId
    private val isPlaying = exoPlayer.isPlaying

    @Composable
    fun GenresScreen(selectedGenre: Genre, updateSelectedGenre: (Genre) -> Unit) {
        val componentState by viewModel.state.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            componentState.genreList.forEach { genre ->
                GenreItem(genre, selectedGenre = selectedGenre, updateSelectedGenre)
            }
        }
    }

    @Composable
    private fun GenreItem(genre: Genre, selectedGenre: Genre, updateSelectedGenre: (Genre) -> Unit) {
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .padding(start = 0.dp, end = 8.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF202020),
                    shape = RoundedCornerShape(8.dp)
                )
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedGenre == genre) Color.White else Color(0xFF1A191C))
                .clickable {
                    scope.launch {
                        if (selectedGenre == genre) {
                            updateSelectedGenre(Genre.ALL)
                        } else {
                            updateSelectedGenre(genre)
                            viewModel.fetchSongsByGenre(genre.id)
                        }
                    }
                }
                .padding(12.dp)
        ) {
            Text(
                text = genre.name?.replaceFirstChar { it.uppercaseChar() } ?: "",
                color = if(selectedGenre == genre) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
                ,

            )
        }
    }



    @Composable
    fun QuickPicksScreen(onSongMoreClick: (QuickPicksSong) -> Unit, onPlayMusicNavigate: (Int) -> Unit) {
        val componentState by viewModel.state.collectAsState()
        val scope = rememberCoroutineScope()

        val quickPicksSong = componentState.quickPicks

        val itemsPerColumn = 4

        val pages = quickPicksSong.chunked(itemsPerColumn)

        if(pages.isEmpty()) {
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quick Picks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .padding(start = 0.dp, end = 8.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color(0xFF1A191C))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF202020),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .shadow(6.dp, RoundedCornerShape(8.dp))
                        .clickable {}
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Play all",
                        modifier = Modifier.clickable {
                            scope.launch {
                                val songIds = quickPicksSong.map { it.id }
                                viewModel.fetchPlayAll(songIds)
                            }
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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
                                .heightIn(max = 250.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(songs) { song ->
                                QuickPickSongItem(
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

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun QuickPickSongItem(
        song: QuickPicksSong,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onClick: (Int) -> Unit
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .heightIn(max = 56.dp)
                .padding(0.dp)
                .background(
                    if (song.id == currentSongId.value) Color.DarkGray else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(end = 8.dp)
                .clickable {
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
                    text = "${song.artist.name} - ${convertIntegerToString(song.views)} views",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            FloatingActionButton(
                onClick = {onSongMoreClick(song)},
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
    fun SpeedDialScreen(user: User?, onPlayMusicNavigate: (Int) -> Unit) {
        val componentState by viewModel.state.collectAsState()

        val speedDialSongs = componentState.speedDial
        val itemsPerPage = 9
        val pages = speedDialSongs.chunked(itemsPerPage)
        val pagerState = rememberPagerState(initialPage = 0) { pages.size }
        val scope = rememberCoroutineScope()

        UserInfo(user)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) { pageIndex ->
            val currentPageItems = pages[pageIndex]
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 0.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top
            ) {
                items(currentPageItems) {song ->
                    SpeedDialSongItem(
                        songId = song.id,
                        thumbnail = song.thumbnailUrl,
                        title = song.title,
                        onClick = {
                            scope.launch {
                                onPlayMusicNavigate(song.id)
                                viewModel.updatePlayHistory(song.id)
                                viewModel.updateArtistPlayHistory(song.artistId)
                            }
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) {pageIndex ->
                val isSelected = pagerState.currentPage == pageIndex
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.DarkGray)
                        .padding(8.dp)
                )
            }
        }
    }

    @Composable
    fun UserInfo(user: User?) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(user != null) {
                AsyncImage(
                    model = user.profilePictureUri,
                    contentDescription = user.name,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Speed dial",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = "Speed dial",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    private fun SpeedDialSongItem(
        songId: Int,
        thumbnail: String,
        title: String,
        onClick: (Int) -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(
                    if (songId == currentSongId.value) Color.White else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
                .padding(4.dp)
                .clickable {
                    onClick(songId)
                }
        ) {

            AsyncImage(
                model = thumbnail,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = 100f
                        )
                    )
            )

            Text(
                text = title.replaceFirstChar { it.uppercaseChar() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun ForgottenFavoritesScreen(onPlayMusicNavigate: (Int) -> Unit) {
        val componentState by viewModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val forgottenFavorites = componentState.forgottenFavorites
        if(forgottenFavorites.isEmpty()) return

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Forgotten favorites",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                forgottenFavorites.forEach {song ->
                    ForgottenFavoriteItem(
                        song = song,
                        onPlayMusicNavigate = {
                            scope.launch {
                                onPlayMusicNavigate(song.id)
                                viewModel.updatePlayHistory(song.id)
                                viewModel.updateArtistPlayHistory(song.artist.id)
                            }
                        })
                }
            }
        }
    }
    
    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    private fun ForgottenFavoriteItem(song: QuickPicksSong, onPlayMusicNavigate: (Int) -> Unit) {
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .clickable {
                    onPlayMusicNavigate(song.id)
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
        ) {
            Box(modifier = Modifier
                .height(136.dp)
                .fillMaxWidth()) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                FloatingActionButton(
                    onClick = {
                        if(song.id != currentSongId.value) {
                            onPlayMusicNavigate(song.id)
                            viewModel.updatePlayHistory(song.id)
                            viewModel.updateArtistPlayHistory(song.artist.id)
                        } else {
                            if(isPlaying.value) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        }
                    },
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                ) {
                    Box(modifier = Modifier
                        .size(24.dp)
                        .background(Color.Black, CircleShape))
                    if(song.id != currentSongId.value) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_filled),
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        if(isPlaying.value) {
                            Icon(
                                painter = painterResource(R.drawable.ic_pause_filled),
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_play_filled),
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${song.artist.name} - ${convertIntegerToString(song.views)} views",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    fun NewReleasesScreen(onPlayMusicNavigate: (Int) -> Unit) {
        val componentState by viewModel.state.collectAsState()
        val newRelease = componentState.newReleases
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        ) {
            Text(
                text = "New Releases",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                newRelease.forEach {song ->
                    NewReleaseItem(
                        song = song,
                        onPlayMusicNavigate = {
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

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    private fun NewReleaseItem(song: QuickPicksSong, onPlayMusicNavigate: (Int) -> Unit) {
        Column(
            modifier = Modifier
                .width(140.dp)
                .fillMaxHeight()
                .clickable {
                    onPlayMusicNavigate(song.id)
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
        ) {
            Box(modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${song.artist.name} - ${convertIntegerToString(song.views)} views",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    fun ArtistAlbumsScreen(onPlayMusicNavigate: (Int) -> Unit, navController: NavController) {
        val componentState by viewModel.state.collectAsState()
        val albums = componentState.albums
        if(albums.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 1564.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                items(albums) {album ->
                    ArtistAlbumScreen(artistAlbum = album, navController= navController, onPlayMusicNavigate = onPlayMusicNavigate)
                }
            }
        }
    }

    @Composable
    fun ArtistAlbumScreen(
        artistAlbum: ArtistAlbum,
        onPlayMusicNavigate: (Int) -> Unit,
        navController: NavController
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        ) {
            ArtistAlbumInfo(
                artist = artistAlbum.artist,
                navController = navController
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalAlignment = Alignment.Top
            ) {
                artistAlbum.songs.forEach {song ->
                    SongItemInAlbum(
                        artistName = artistAlbum.artist.name,
                        song = song,
                        onPlayMusicNavigate = {
                            onPlayMusicNavigate(song.id)
                            viewModel.updatePlayHistory(song.id)
                            viewModel.updateArtistPlayHistory(artistAlbum.artist.id)
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun ArtistAlbumInfo(
        artist: ArtistReview,
        navController: NavController
    ) {
        val scope = rememberCoroutineScope()
        Row(
            modifier = Modifier.fillMaxWidth().padding(end=16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = artist.thumbnail,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = artist.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable {
                        scope.launch {
                            navController.navigate("${ArtistRouter.route}/${artist.id}")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "More",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    private fun SongItemInAlbum(artistName: String, song: SongReview, onPlayMusicNavigate: (Int) -> Unit) {
        Column(
            modifier = Modifier
                .width(160.dp)
                .fillMaxHeight()
                .clickable {
                    onPlayMusicNavigate(song.id)
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
        ) {
            Box(modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = artistName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    fun GenreSongsScreen(mainGenreName: String, onPlayMusicNavigate: (Int) -> Unit) {
        val componentState by viewModel.state.collectAsState()
        val genreSongs = componentState.genreSongs
        val scope = rememberCoroutineScope()
        if(genreSongs.isEmpty()) return

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 1564.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            items(genreSongs) {genreSong ->
                GenreSongItemScreen(
                    mainGenreName = mainGenreName,
                    genreSong = genreSong,
                    onPlayMusicNavigate = {
                        scope.launch {
                            onPlayMusicNavigate(it)
                        }
                    }
                )
            }
        }

    }

    @Composable
    private fun GenreSongItemScreen(
        mainGenreName: String,
        genreSong: GenreSongs,
        onPlayMusicNavigate: (Int) -> Unit
    ) {
        if(genreSong.songs.size <= 1) return
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        ) {
            Text(
                text = "${mainGenreName.replaceFirstChar { it.uppercaseChar() }} - ${genreSong.genre.name?.replaceFirstChar { it.uppercaseChar() }}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                genreSong.songs.forEach {song ->
                    GenreSongItem(
                        song = song,
                        onPlayMusicNavigate = {
                            onPlayMusicNavigate(song.id)
                            viewModel.updatePlayHistory(song.id)
                            viewModel.updateArtistPlayHistory(song.artist.id)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun GenreSongItem(song: GenreSong, onPlayMusicNavigate: (Int) -> Unit) {
        Column(
            modifier = Modifier
                .width(160.dp)
                .fillMaxHeight()
                .clickable {
                    onPlayMusicNavigate(song.id)
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
        ) {
            Box(modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${song.artist.name} - ${convertSecondToMS(song.duration)} - ${convertIntegerToString(song.views)} views",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun convertSecondToMS(second: Int): String {
        val minutes = second / 60
        val seconds = second % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

