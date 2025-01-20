package com.example.vivibe.pages

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vivibe.R
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.model.QuickPicksSong
import kotlinx.coroutines.launch

class Search {

    @Composable
    fun SearchScreen(
        searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(SongClient(LocalContext.current, "your_token_here"))),
        onBackPressed: () -> Unit
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val searchResults by searchViewModel.searchResults.collectAsState()
        val recentSearches = remember { mutableStateOf(listOf<String>()) }

        var query by remember { mutableStateOf("") }
        var isResultScreen by remember { mutableStateOf(false) }

        BackHandler {
            if (isResultScreen) {
                isResultScreen = false
            } else {
                onBackPressed()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    coroutineScope.launch {
                        searchViewModel.searchSongs(it)
                        // Tự động lưu vào lịch sử khi nhấn Enter
                        if (it.isNotEmpty() && !recentSearches.value.contains(it)) {
                            recentSearches.value = (listOf(it) + recentSearches.value).take(5) // Lưu tối đa 5 từ
                        }
                        isResultScreen = true
                    }
                },
                onBackPressed = {
                    if (isResultScreen) isResultScreen = false else onBackPressed()
                }
            )

            if (isResultScreen) {
                SearchResultScreen(query = query, searchViewModel = searchViewModel)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (query.isNotEmpty() && searchResults.isNotEmpty()) {
                        items(searchResults.filter { it.title.contains(query, ignoreCase = true) }) { suggestion ->
                            SuggestionItem(suggestion.title) {
                                query = suggestion.title
                                coroutineScope.launch {
                                    searchViewModel.searchSongs(suggestion.title)
                                    if (!recentSearches.value.contains(suggestion.title)) {
                                        recentSearches.value =
                                            (listOf(suggestion.title) + recentSearches.value).take(5)
                                    }
                                }
                                isResultScreen = true
                            }
                        }
                    }

                    items(searchResults.take(5)) { song ->
                        SongItem(song = song, context = context)
                    }

                    if (recentSearches.value.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Searched",
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(recentSearches.value) { recentQuery ->
                            RecentSearchItem(recentQuery) {
                                query = recentQuery
                                coroutineScope.launch {
                                    searchViewModel.searchSongs(recentQuery)
                                }
                                isResultScreen = true
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchBar(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearch: (String) -> Unit,
        onBackPressed: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onBackPressed() },
                tint = Color.Gray
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("Search songs, artists ...", color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                    innerTextField()
                }
            )
        }
    }

    @Composable
    private fun SearchResultScreen(
        query: String,
        searchViewModel: SearchViewModel
    ) {
        val context = LocalContext.current
        val searchResults by searchViewModel.searchResults.collectAsState()

        LaunchedEffect(query) {
            searchViewModel.searchSongs(query)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
                .padding(8.dp)
        ) {
            Text(
                text = "Search Results for \"$query\"",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (searchResults.isEmpty()) {
                    item {
                        Text(
                            text = "No results found.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(searchResults) { song ->
                        SongItem(song = song, context = context)
                    }
                }
            }
        }
    }

    @Composable
    private fun SuggestionItem(title: String, onClick: () -> Unit) {
        Text(
            text = title,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(8.dp)
        )
    }

    @Composable
    private fun SongItem(song: QuickPicksSong, context: Context) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    Toast.makeText(context, "Playing ${song.title}", Toast.LENGTH_SHORT).show()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = "Song Thumbnail",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF292929)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = song.artist.name,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    private fun RecentSearchItem(query: String, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_history),
                contentDescription = "History Icon",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                tint = Color.Gray
            )
            Text(
                text = query,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
