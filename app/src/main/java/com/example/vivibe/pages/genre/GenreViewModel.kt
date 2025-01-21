package com.example.vivibe.pages.genre

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.Genre
import com.example.vivibe.model.GenreSongs
import com.example.vivibe.model.QuickPicksSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GenreViewModel(appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer): ViewModel() {
    private val songClient = SongClient(appContext, userManager.getToken().orEmpty())
    private val dbHelper = DatabaseHelper(appContext)

    private val _genreName = MutableStateFlow("")
    val genreName: MutableStateFlow<String> = _genreName

    private val _songs = MutableStateFlow<List<QuickPicksSong>>(emptyList())
    val songs: MutableStateFlow<List<QuickPicksSong>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: MutableStateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: MutableStateFlow<String?> = _error

    fun initialize(genreId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            fetchGenreSongs(genreId)
            _isLoading.value = false

        }
    }

    private suspend fun fetchGenreSongs(genreId: Int) {
        try {
            val response = songClient.fetchNameAndSongs(genreId)
            if(response != null) {
                _genreName.value = response.name
                _songs.value = response.songs
            } else {
                _error.value = "Could not fetch genre songs"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePlayHistory(songId: Int) {
        viewModelScope.launch {
            try {
                val googleId = userManager.getGoogleId()
                if(!googleId.isNullOrBlank()) {
                    dbHelper.insertOrUpdateSongPlayHistory(songId, googleId)
                }
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Error updating artist play history", e)
            }
        }
    }

    fun updateArtistPlayHistory(artistId: Int) {
        viewModelScope.launch {
            try {
                val googleId = userManager.getGoogleId()
                if(!googleId.isNullOrBlank()) {
                    dbHelper.insertOrUpdateArtistPlayHistory(artistId, googleId)
                }
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Error updating artist play history", e)
            }
        }
    }
}