package com.example.vivibe.pages.library

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.vivibe.MainActivity
import com.example.vivibe.R
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.DownloadedSong
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.PlaylistReview
import com.example.vivibe.model.User
import com.example.vivibe.router.AccountRouter
import com.example.vivibe.router.ArtistRouter
import com.example.vivibe.router.HistoryRouter
import com.example.vivibe.router.NotificationsRouter
import com.example.vivibe.router.PlaylistRouter
import com.example.vivibe.router.SearchRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class Library(private val context: Context) {
    @Composable
    fun LibraryScreen(
        navController: NavController,
        libraryViewModel: LibraryViewModel,
        exoPlayer: SharedExoPlayer,
    ) {
        val scope = rememberCoroutineScope()
        val user by libraryViewModel.user.collectAsState()
        val currentView by libraryViewModel.currentView.collectAsState()

        val onPlayMusicNavigate: (List<PlaySong>, Int) -> Unit = { songs, songId ->
            scope.launch {
                exoPlayer.updateSongs(songs, isOffline = true)
                exoPlayer.handlePlayOneInListSong(songId)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            TopBar(context, scope, navController, user, libraryViewModel, currentView)

            when (currentView) {
                "Library" -> {
                    LibraryComponentScreen(navController, libraryViewModel)
                }
                "Downloads" -> {
                    DownloadsComponentScreen(
                        viewModel = libraryViewModel,
                        onPlayMusicNavigate = onPlayMusicNavigate
                    )
                }
            }
        }
    }


    @Composable
    private fun LibraryComponentScreen(navController: NavController, viewModel: LibraryViewModel) {
        val isLoading by viewModel.isLoading.collectAsState()
        var hasFetched by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            if (!hasFetched) {
                viewModel.initializeLibrary()
                hasFetched = true
            }
        }

        if(isLoading) {
            LoadingIndicator()
            return
        }

        val playlists by viewModel.playlists.collectAsState()
        val likedArtists by viewModel.likedArtists.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            LikedMusicView() {
                scope.launch {
                    navController.navigate("${PlaylistRouter.route}/0")
                }
            }

            playlists.forEach {playlist ->
                PlaylistView(playlist) {
                    scope.launch {
                        navController.navigate("${PlaylistRouter.route}/${playlist.id}")
                    }
                }
            }

            likedArtists.forEach {likedArtist ->
                LikedArtistView(likedArtist) {
                    scope.launch {
                        navController.navigate("${ArtistRouter.route}/${likedArtist.id}")
                    }
                }
            }
        }

    }

    @Composable
    private fun DownloadsComponentScreen(
        viewModel: LibraryViewModel,
        onPlayMusicNavigate: (List<PlaySong>, Int) -> Unit
    ) {
        val downloadedSongs by viewModel.downloadedSongs.collectAsState()
        var hasFetched by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!hasFetched) {
                viewModel.getDownloadedSongs()
                hasFetched = true
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            DownloadsHeader(downloadedSongs.size)

            if (downloadedSongs.isEmpty()) {
                EmptyDownloadsView()
            } else {
                // Sort and Filter Options
                DownloadsSortFilterBar()

                // Songs List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(downloadedSongs) { song ->
                        DownloadedSongItem(
                            song = song,
                            onClick = {
                                // Convert downloaded songs to play songs
                                val playSongs = downloadedSongs.map { it.toPlaySong() }
                                onPlayMusicNavigate(playSongs, song.id)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DownloadsHeader(totalSongs: Int) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Downloads",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$totalSongs ${if(totalSongs == 1) "song" else "songs"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }

    @Composable
    private fun EmptyDownloadsView() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_download),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "No downloaded songs yet",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun DownloadsSortFilterBar() {
        var expanded by remember { mutableStateOf(false) }
        var selectedSort by remember { mutableStateOf("Date added") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expanded = true }
            ) {
                Text(
                    text = selectedSort,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = "Sort options",
                    tint = Color.White
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shuffle),
                    contentDescription = "Shuffle play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { /* Handle shuffle */ }
                )
                Icon(
                    painter = painterResource(R.drawable.ic_play_filled),
                    contentDescription = "Play all",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { /* Handle play all */ }
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Date added", "Title", "Artist", "Recently played").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedSort = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun DownloadedSongItem(
        song: DownloadedSong,
        onClick: () -> Unit
    ) {
        var showOptions by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                AsyncImage(
                    model = song.thumbnailPath,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                // Song Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artistName,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Options Button
                IconButton(
                    onClick = { showOptions = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
            }
        }

        if (showOptions) {
            DownloadedSongOptionsSheet(
                song = song,
                onDismiss = { showOptions = false }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DownloadedSongOptionsSheet(
        song: DownloadedSong,
        onDismiss: () -> Unit
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF202020),
            dragHandle = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Song info header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = song.thumbnailPath,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = song.artistName,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Options
                OptionsRow(
                    icon = R.drawable.ic_play_next,
                    text = "Play next",
                    onClick = { /* Handle play next */ }
                )
                OptionsRow(
                    icon = R.drawable.ic_add_to_queue,
                    text = "Add to queue",
                    onClick = { /* Handle add to queue */ }
                )
                OptionsRow(
                    icon = R.drawable.ic_go_to_artist,
                    text = "Go to artist",
                    onClick = { /* Handle go to artist */ }
                )
                OptionsRow(
                    icon = R.drawable.ic_share,
                    text = "Share",
                    onClick = { /* Handle share */ }
                )
                OptionsRow(
                    icon = R.drawable.ic_delete,
                    text = "Delete download",
                    onClick = { /* Handle delete */ }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    @Composable
    private fun OptionsRow(
        @DrawableRes icon: Int,
        text: String,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }



    private fun DownloadedSong.toPlaySong(): PlaySong {
        return PlaySong(
            id = id,
            title = title,
            thumbnailUrl = thumbnailPath,
            audio = audioPath, // This will be replaced in SharedExoPlayer
            artist = ArtistReview(artistId, artistName, ""),
            duration = duration,
            lyrics = lyrics,
            views = views,
            likes = likes,
            dominantColor = dominantColor
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TopBar(
        context: Context,
        scope: CoroutineScope,
        navController: NavController,
        user: User?,
        libraryViewModel: LibraryViewModel,
        currentView: String
    ) {
        var showViewMySheet by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showViewMySheet = true }
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentView,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_down),
                        contentDescription = "View options",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.width(150.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            navController.navigate(HistoryRouter.route)
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = "History",
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                IconButton(
                    onClick = {
                        navController.navigate(SearchRouter.route)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Search",
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                if (user == null) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val success = libraryViewModel.signIn(context)
                                if(success) {
                                    (context as? MainActivity)?.reloadActivity()
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google),
                            contentDescription = "Google Sign In",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            scope.launch {
                                navController.navigate(AccountRouter.route)
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = user.profilePictureUri),
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }

        if (showViewMySheet) {
            ModalBottomSheet(
                onDismissRequest = { showViewMySheet = false },
                containerColor = Color(0xFF202020),
                dragHandle = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "View my",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).clickable { showViewMySheet = false }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    ViewMyOption(
                        text = "Library",
                        isChecked = currentView == "Library",
                    ) {
                        libraryViewModel.updateCurrentView("Library")
                        showViewMySheet = false
                    }

                    ViewMyOption(
                        text = "Downloads",
                        isChecked = currentView == "Downloads"
                    ) {
                        libraryViewModel.updateCurrentView("Downloads")
                        showViewMySheet = false
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }


    @Composable
    private fun ViewMyOption(
        text: String,
        isChecked: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (isChecked) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    @Composable
    private fun LikedMusicView(onClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            Image(
                painter = painterResource(R.drawable.like),
                modifier = Modifier.size(56.dp),
                contentDescription = "Liked Music"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Liked Music",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pin),
                        contentDescription = "Pin",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "Auto playlist",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    private fun PlaylistView(playlist: PlaylistReview, onClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            PlaylistImage(playlist.thumbnails, imgWidth = 56)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = playlist.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = "Playlist - ${playlist.userName}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun LikedArtistView(artist: ArtistDetail, onClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            AsyncImage(
                model = artist.thumbnail,
                modifier = Modifier.size(56.dp),
                contentDescription = artist.name,
                contentScale = ContentScale.Crop
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = artist.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Artists - ${convertIntegerToString(artist.followers.toLong())} subscribers",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

            }
        }
    }

    @Composable
    private fun PlaylistImage(
        thumbnails: List<String>,
        imgWidth: Int
    ) {
        Box(
            modifier = Modifier
                .size(imgWidth.dp)
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
    private fun LoadingIndicator() {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}