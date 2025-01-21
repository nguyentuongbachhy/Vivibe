package com.example.vivibe.pages.search

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vivibe.MainViewModel
import com.example.vivibe.R
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SongDetail
import kotlinx.coroutines.launch

class Search {
    @Composable
    fun SearchScreen(
        navController: NavController,
        viewModel: SearchViewModel,
        mainViewModel: MainViewModel
    ) {
        val searchResults by viewModel.searchResults.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val recentSearches by viewModel.recentSearches.collectAsState()

        var query by remember { mutableStateOf("") }
        var isResultScreen by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
        ) {
            SearchBar(
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery
                    if (newQuery.isEmpty()) {
                        isResultScreen = false
                    }
                },
                onSearch = {
                    if (it.isNotEmpty()) {
                        scope.launch {
                            viewModel.searchSongs(it)
                            viewModel.addToRecentSearches(it)
                            isResultScreen = true
                        }
                    }
                },
                onClearQuery = {
                    query = ""
                    isResultScreen = false
                },
                onBackPressed = {
                    if (isResultScreen) {
                        isResultScreen = false
                        query = ""
                    } else {
                        navController.navigateUp()
                    }
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> LoadingScreen()
                    isResultScreen -> SearchResultScreen(
                        query = query,
                        searchResults = searchResults,
                        onSongClick = { songId ->
                            scope.launch {
                                mainViewModel.fetchPlaySong(songId)
                            }
                        },
                        onMoreClick = { song ->
                            mainViewModel.showBottomSheet(song)
                        }
                    )
                    else -> SearchSuggestionsScreen(
                        query = query,
                        recentSearches = recentSearches,
                        suggestions = searchResults,
                        onSuggestionClick = { suggestion ->
                            query = suggestion
                            scope.launch {
                                viewModel.searchSongs(suggestion)
                                viewModel.addToRecentSearches(suggestion)
                                isResultScreen = true
                            }
                        },
                        onRecentSearchClick = { recentQuery ->
                            query = recentQuery
                            scope.launch {
                                viewModel.searchSongs(recentQuery)
                                isResultScreen = true
                            }
                        },
                        onClearRecentSearches = {
                            viewModel.clearRecentSearches()
                        },
                        onSongClick = { songId ->
                            scope.launch {
                                mainViewModel.fetchPlaySong(songId)
                            }
                        },
                        onMoreClick = { song ->
                            mainViewModel.showBottomSheet(song)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun SearchResultScreen(
        query: String,
        searchResults: List<SongDetail>,
        onSongClick: (Int) -> Unit,
        onMoreClick: (QuickPicksSong) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Results for \"$query\"",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = "${searchResults.size} ${if (searchResults.size == 1) "result" else "results"} found",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (searchResults.isEmpty()) {
                item {
                    EmptySearchResult()
                }
            } else {
                items(
                    items = searchResults,
                    key = { it.id }
                ) { song ->
                    SearchResultItem(
                        song = song,
                        onClick = { onSongClick(song.id) },
                        onMoreClick = {
                            val qSong = song.artist?.let {
                                QuickPicksSong(
                                    id = song.id,
                                    title = song.title,
                                    thumbnailUrl = song.thumbnailUrl,
                                    views = song.views,
                                    artist = it
                                )
                            }

                            if (qSong != null) {
                                onMoreClick(qSong)
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptySearchResult() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "No results found",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Try different keywords or check the spelling",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun SearchResultItem(
        song: SongDetail,
        onClick: () -> Unit,
        onMoreClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
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
                // Thumbnail
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                // Song Info
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.artist?.name ?: "",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "•",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Text(
                            text = formatViews(song.views),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                // More Options
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

    @Composable
    private fun SearchBar(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearch: (String) -> Unit,
        onClearQuery: () -> Unit,
        onBackPressed: () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .statusBarsPadding(), // Thêm padding cho status bar
            color = Color(0xFF202020)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(Color(0xFF303030), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            autoCorrect = true // Thêm auto correct
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSearch(query) }
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (query.isEmpty()) {
                                    Text(
                                        "",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Clear",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchSuggestionsScreen(
        query: String,
        recentSearches: List<String>,
        suggestions: List<SongDetail>,
        onSuggestionClick: (String) -> Unit,
        onRecentSearchClick: (String) -> Unit,
        onClearRecentSearches: () -> Unit,
        onSongClick: (Int) -> Unit,
        onMoreClick: (QuickPicksSong) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (query.isNotEmpty()) {
                items(suggestions.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.artist?.name?.contains(query, ignoreCase = true) == true
                }) { song ->
                    SuggestionItem(
                        title = song.title,
                        artist = song.artist?.name ?: "",
                        onClick = { onSuggestionClick(song.title) }
                    )
                }
            } else if (recentSearches.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent searches",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = onClearRecentSearches) {
                            Text("Clear all", color = Color.Gray)
                        }
                    }
                }

                items(recentSearches) { query ->
                    RecentSearchItem(
                        query = query,
                        onClick = { onRecentSearchClick(query) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Top results",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(suggestions.take(5)) { song ->
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song.id) },
                        onMoreClick = {
                            val qSong = song.artist?.let {
                                QuickPicksSong(
                                    id = song.id,
                                    title = song.title,
                                    thumbnailUrl = song.thumbnailUrl,
                                    views = song.views,
                                    artist = it
                                )
                            }

                            if(qSong != null) {
                                onMoreClick(qSong)
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun SongItem(
        song: SongDetail,
        onClick: () -> Unit,
        onMoreClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp),
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist?.name ?: "",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
        }
    }

    @Composable
    private fun SuggestionItem(
        title: String,
        artist: String,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = artist,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    private fun RecentSearchItem(
        query: String,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = query,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )

                Text(
                    text = "Searching...",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }

    private fun formatViews(views: Long): String {
        return when {
            views < 1000 -> views.toString()
            views < 1_000_000 -> String.format("%.1fK", views / 1000.0)
            else -> String.format("%.1fM", views / 1_000_000.0)
        }
    }
}