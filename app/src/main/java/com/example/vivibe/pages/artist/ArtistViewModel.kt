package com.example.vivibe.pages.artist

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.FullInfoArtist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer): ViewModel() {
    private val dbHelper = DatabaseHelper(appContext)
    private val _songClient = MutableStateFlow<SongClient?>(null)

    private val _fullInfoArtist = MutableStateFlow<FullInfoArtist?>(null)
    val fullInfoArtist: StateFlow<FullInfoArtist?> get() = _fullInfoArtist

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            userManager.userState.collect { user ->
                _songClient.value = user?.token?.let { token ->
                    SongClient(appContext, token)
                }
            }
        }
    }

    fun fetchFullInfoArtist(artistId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val client = _songClient.value ?: run {
                    _error.value = "Not authenticated"
                    return@launch
                }

                val fetchedFullInfoArtist = client.fetchArtistAndAlbum(artistId)
                if(fetchedFullInfoArtist != null) {
                    _fullInfoArtist.value = fetchedFullInfoArtist
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
            val client = _songClient.value ?: run {
                Log.d("ArtistViewModel", "Client not initialized")
                return@launch
            }
            val fetchedPlayAll = client.fetchPlayAll(songIds)
            if (fetchedPlayAll.isNotEmpty()) {
                println("Play all fetched successfully")

                exoPlayer.updateSongs(fetchedPlayAll)
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
                Log.e("ArtistViewModel", "Error updating play history", e)
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
                Log.e("ArtistViewModel", "Error updating artist play history", e)
            }
        }
    }
}