package com.example.vivibe.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.model.QuickPicksSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val songClient: SongClient) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<QuickPicksSong>>(emptyList())
    val searchResults: StateFlow<List<QuickPicksSong>> = _searchResults

    fun searchSongs(query: String) {
        viewModelScope.launch {
            Log.d("SearchViewModel", "Searching for: $query")
            val results = songClient.searchSongAndArtist(query)
            Log.d("SearchViewModel", "Results: ${results.size}")
            _searchResults.value = results
        }
    }
}