package com.example.vivibe.pages
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vivibe.api.song.SongClient

class SearchViewModelFactory(private val songClient: SongClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(songClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}