package com.example.vivibe.pages.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.example.vivibe.R
import com.example.vivibe.model.AlbumReview
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.Genre
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.router.AlbumRouter
import com.example.vivibe.router.ArtistRouter
import com.example.vivibe.router.ExploreRouter
import com.example.vivibe.router.GenreRouter
import com.example.vivibe.router.SearchRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Common {
    @Composable
    fun CommonScreen(
        screenId: Int,
        commonViewModel: CommonViewModel,
        navController: NavController,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onPlayMusicNavigate: (Int) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 64.dp)
        ) {
            when (screenId) {
                0 -> NewReleaseScreen(viewModel = commonViewModel, navController = navController)
                1 -> ChartsScreen(viewModel = commonViewModel, navController = navController, onSongMoreClick = onSongMoreClick, onPlayMusicNavigate = onPlayMusicNavigate)
                2 -> MoodsAndGenresScreen(viewModel = commonViewModel, navController = navController)
            }
        }
    }

    @Composable
    private fun NewReleaseScreen(
        viewModel: CommonViewModel,
        navController: NavController
    ) {
        val isLoading by viewModel.isLoading.collectAsState()
        var hasFetched by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            if (!hasFetched) {
                viewModel.initializeNewRelease()
                hasFetched = true
            }
        }

        if (isLoading) {
            LoadingIndicator()
            return
        }

        val latestAlbum by viewModel.latestAlbum.collectAsState()
        val albums by viewModel.albums.collectAsState()
        val artists by viewModel.artists.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TopBar(navController, scope)
            HeaderSection("New releases")
            LatestReleaseSection(latestAlbum)
            ContentSection(albums, artists, navController, scope)
        }
    }

    @Composable
    private fun ChartsScreen(
        viewModel: CommonViewModel,
        navController: NavController,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onPlayMusicNavigate: (Int) -> Unit,
    ) {
        val isLoading by viewModel.isLoading.collectAsState()
        var hasFetched by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        if(isLoading) {
            LoadingIndicator()
            return
        }

        LaunchedEffect(Unit) {
            if (!hasFetched) {
                viewModel.initializeCharts()
                hasFetched = true
            }
        }

        val topSongs by viewModel.topSongs.collectAsState()
        val topArtists by viewModel.topArtists.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            TopBar(navController, scope)
            HeaderSection("Charts")
            TopSongs(topSongs, viewModel, onSongMoreClick, onPlayMusicNavigate)
            TopArtists(topArtists, navController, scope)
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun MoodsAndGenresScreen(viewModel: CommonViewModel, navController: NavController) {
        val isLoading by viewModel.isLoading.collectAsState()
        var hasFetched by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        if(isLoading) {
            LoadingIndicator()
            return
        }

        LaunchedEffect(Unit) {
            if (!hasFetched) {
                viewModel.initializeMoodsGenres()
                hasFetched = true
            }
        }

        val tagForYou by viewModel.tagForYou.collectAsState()
        val genres by viewModel.genres.collectAsState()

        val colors = listOf(
            Color(0xFFFF6B6B),  // Coral Red
            Color(0xFF4ECDC4),  // Turquoise
            Color(0xFFFFBE0B),  // Yellow
            Color(0xFF95E1D3),  // Mint
            Color(0xFFF86624),  // Orange
            Color(0xFF8338EC),  // Purple
            Color(0xFF48BFE3),  // Sky Blue
            Color(0xFFFF006E),  // Pink
            Color(0xFF72B01D),  // Lime Green
            Color(0xFFE76F51),  // Burnt Sienna
            Color(0xFF3A86FF),  // Blue
            Color(0xFFFF7B9C),  // Light Pink
            Color(0xFF34A0A4),  // Teal
            Color(0xFFFB5607),  // Deep Orange
            Color(0xFF9B5DE5),  // Lavender
            Color(0xFF00BBF9),  // Light Blue
            Color(0xFFFF9F1C),  // Golden
            Color(0xFF7CB518)   // Green
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            item {
                TopBar(navController, scope)
            }

            item {
                HeaderSection("Moods & genres")
            }

            item {
                Text(
                    text = "Tag for you",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 2
                ) {
                    tagForYou.forEachIndexed { index, genre ->
                        GenreButton(
                            genre = genre,
                            color = colors[index % colors.size],
                            modifier = Modifier.weight(1f),
                            scope = scope,
                            navController = navController
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Genres",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 2
                ) {
                    genres.forEachIndexed { index, genre ->
                        GenreButton(
                            genre = genre,
                            color = colors[index % colors.size],
                            modifier = Modifier.weight(1f),
                            scope = scope,
                            navController = navController
                        )
                    }
                }
            }
        }
    }


    @Composable
    private fun TopArtists(topArtists: List<ArtistDetail>, navController: NavController, scope: CoroutineScope) {

        val itemsPerColumn = 4

        val pages = topArtists.chunked(itemsPerColumn)

        if(pages.isEmpty()) {
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(288.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Top artists",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.Top
            ) {
                pages.forEachIndexed { pageIndex, artists ->
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
                            itemsIndexed(artists) {index, artist ->
                                TopArtistItem(
                                    id = (pageIndex * itemsPerColumn) + index + 1,
                                    artist = artist,
                                    navController = navController,
                                    scope = scope
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopSongs(topSongs: List<QuickPicksSong>, viewModel: CommonViewModel, onSongMoreClick: (QuickPicksSong) -> Unit, onPlayMusicNavigate: (Int) -> Unit) {
        val scope = rememberCoroutineScope()

        val itemsPerColumn = 4

        val pages = topSongs.chunked(itemsPerColumn)

        if(pages.isEmpty()) {
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(288.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Top songs",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.Top
            ) {
                pages.forEachIndexed { pageIndex, songs ->
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
                            itemsIndexed(songs) {index, song ->
                                TopSongItem(
                                    id = (pageIndex * itemsPerColumn) + index + 1,
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

    @Composable
    private fun LoadingIndicator() {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    @Composable
    private fun TopBar(
        navController: NavController,
        scope: CoroutineScope
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

            NavigationIcon(
                iconRes = R.drawable.ic_search,
                description = "Search"
            ) {
                scope.launch {
                    navController.navigate(SearchRouter.route)
                }
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
    private fun HeaderSection(text: String) {
        Text(
            text = text,
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            modifier = Modifier.width(160.dp)
        )
    }

    @Composable
    private fun LatestReleaseSection(latestAlbum: AlbumReview?) {
        latestAlbum?.let { album ->
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (album.thumbnails.isNotEmpty()) {
                    AlbumImage(album.thumbnails, 120)
                }

                Text(
                    text = "RELEASED",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = album.names.joinToString(", "),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    private fun ContentSection(
        albums: List<AlbumReview>,
        artists: List<ArtistReview>,
        navController: NavController,
        scope: CoroutineScope
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Albums & singles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(albums) { album ->
                    AlbumItem(
                        album = album,
                        onNavigation = {
                            scope.launch {
                                navController.navigate("${AlbumRouter.route}/${album.id}")
                            }
                        }
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(artists) { artist ->
                    ArtistItem(
                        artist = artist,
                        onNavigation = {
                            scope.launch {
                                navController.navigate("${ArtistRouter.route}/${artist.id}")
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun AlbumItem(
        album: AlbumReview,
        onNavigation: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .height(150.dp)
                .myOwnClickable(onClick = onNavigation),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlbumImage(album.thumbnails, 100)

            Text(
                text = album.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Album - ${album.names.joinToString(" & ")}",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun ArtistItem(
        artist: ArtistReview,
        onNavigation: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .height(150.dp)
                .myOwnClickable(onClick = onNavigation),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = artist.thumbnail,
                contentDescription = artist.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Text(
                text = artist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Single",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun AlbumImage(
        thumbnails: List<String>,
        imgWidth: Int
    ) {
        Box(
            modifier = Modifier
                .size(imgWidth.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            thumbnails.take(4).forEachIndexed { index, thumbnail ->
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    modifier = Modifier
                        .size((imgWidth / 2).dp)
                        .align(
                            when (index) {
                                0 -> Alignment.TopStart
                                1 -> Alignment.TopEnd
                                2 -> Alignment.BottomStart
                                else -> Alignment.BottomEnd
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun TopSongItem(
        id: Int,
        song: QuickPicksSong,
        onSongMoreClick: (QuickPicksSong) -> Unit,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
            .widthIn(max = 270.dp)
            .heightIn(max = 56.dp)
            .padding(0.dp)
            .padding(end = 8.dp)
            .clickable {
                onClick()
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ){
            Text(
                text = id.toString(),
                color = Color.Gray,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(),
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
                        color = Color.Gray,
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
    }

    @Composable
    private fun TopArtistItem(id: Int, artist: ArtistDetail, navController: NavController, scope: CoroutineScope) {
        Row(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .heightIn(max = 56.dp)
                .padding(0.dp)
                .padding(end = 8.dp)
                .clickable {
                    scope.launch {
                        navController.navigate("${ArtistRouter.route}/${artist.id}")
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            Text(
                text = id.toString(),
                color = Color.Gray,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = artist.thumbnail,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
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
                        text = artist.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${convertIntegerToString(artist.followers.toLong())} subscribers",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    @Composable
    fun GenreButton(genre: Genre, color: Color, modifier: Modifier, scope: CoroutineScope, navController: NavController) {
        Row(modifier = modifier.width(110.dp).height(35.dp)) {
            Box(modifier = Modifier.fillMaxHeight().width(10.dp).background(color, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))

            Button(
                onClick = {
                    scope.launch {
                        navController.navigate("${GenreRouter.route}/${genre.id}")
                    }
                },
                modifier = Modifier
                    .height(35.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C))
            ) {
                Text(
                    text = genre.name ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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