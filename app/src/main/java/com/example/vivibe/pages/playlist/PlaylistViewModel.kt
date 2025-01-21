package com.example.vivibe.pages.playlist

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.FullInfoAlbum
import com.example.vivibe.model.FullInfoPlaylist
import com.example.vivibe.model.SongDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer): ViewModel() {
    private val dbHelper = DatabaseHelper(appContext)
    private val songClient = MutableStateFlow<SongClient?>(null)
    private val genreClient = MutableStateFlow<GenreClient?>(null)

    private val _fullInfoPlaylist = MutableStateFlow<FullInfoPlaylist?>(null)
    val fullInfoPlaylist: StateFlow<FullInfoPlaylist?> get() = _fullInfoPlaylist

    private val _likedSongs = MutableStateFlow<List<SongDetail>>(emptyList())
    val likedSongs: StateFlow<List<SongDetail>> get() = _likedSongs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error




    init {
        viewModelScope.launch {
            userManager.userState.collect { user ->
                songClient.value = user?.token?.let { token ->
                    SongClient(appContext, token)
                }
                genreClient.value = user?.token?.let { token ->
                    GenreClient(appContext, token)
                }
            }
        }
    }

    fun fetchLikedSongs() {
        viewModelScope.launch {
            val userId = userManager.getId() ?: return@launch
            try {
                val response = songClient.value?.getLikedSongs(userId)
                if (response != null) {
                    _likedSongs.value = response
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PlaylistViewModel", "Error fetching liked songs", e)
            }
        }
    }

    fun fetchFullInfoPlaylist(playlistId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val client = songClient.value ?: run {
                    _error.value = "Not authenticated"
                    return@launch
                }

                val fetchedFullInfoArtist = client.fetchDetailPlaylist(playlistId)
                if(fetchedFullInfoArtist != null) {
                    _fullInfoPlaylist.value = fetchedFullInfoArtist
                } else {
                    _error.value = "Could not fetch artist info"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                println("Error fetching full info artist: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPlayAll(songIds: List<Int>) {
        viewModelScope.launch {
            val client = songClient.value ?: run {
                Log.d("AlbumViewModel", "Client not initialized")
                return@launch
            }
            val gClient = genreClient.value ?: run {
                Log.d("AlbumViewModel", "Genre client not initialized")
                return@launch
            }

            val fetchedPlayAll = client.fetchPlayAll(songIds)
            if (fetchedPlayAll.isNotEmpty()) {
                println("Play all fetched successfully")

                exoPlayer.updateSongs(fetchedPlayAll)

                val genreIds = gClient.fetchGenresSong(songIds[0])
                if (genreIds.isNotEmpty()) {
                    dbHelper.insertOrUpdateGenrePlayHistory(genreIds, userManager.getGoogleId() ?: "")
                }

            } else {
                println("No play all fetched")
            }
        }
    }

    fun updatePlayHistory(songId: Int) {
        viewModelScope.launch {
            try {
                val user = userManager.userState.value
                if (!user?.googleId.isNullOrBlank()) {
                    dbHelper.insertOrUpdateSongPlayHistory(songId, user!!.googleId!!)
                }
            } catch (e: Exception) {
                Log.e("AlbumViewModel", "Error updating play history", e)
            }
        }
    }

    fun updateArtistPlayHistory(artistId: Int) {
        viewModelScope.launch {
            try {
                val user = userManager.userState.value
                if (!user?.googleId.isNullOrBlank()) {
                    dbHelper.insertOrUpdateArtistPlayHistory(artistId, user!!.googleId!!)
                }
            } catch (e: Exception) {
                Log.e("AlbumViewModel", "Error updating artist play history", e)
            }
        }
    }
}