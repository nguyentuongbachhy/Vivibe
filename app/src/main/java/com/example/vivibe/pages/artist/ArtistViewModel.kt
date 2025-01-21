package com.example.vivibe.pages.artist

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.FullInfoArtist
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer): ViewModel() {
    private val dbHelper = DatabaseHelper(appContext)
    private val songClient = MutableStateFlow<SongClient?>(null)
    private val genreClient = MutableStateFlow<GenreClient?>(null)
    private val userClient = MutableStateFlow<UserClient?>(null)
    private val TAG = "ArtistViewModel"

    private val _isFollowLoading = MutableStateFlow(false)
    val isFollowLoading: StateFlow<Boolean> get() = _isFollowLoading
    private val _fullInfoArtist = MutableStateFlow<FullInfoArtist?>(null)
    val fullInfoArtist: StateFlow<FullInfoArtist?> get() = _fullInfoArtist

    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> get() = _isFollowed

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
                userClient.value = user?.token?.let { token ->
                    UserClient(appContext, token)
                }
            }
        }
    }

    fun fetchFollowStatus(artistId: Int) {
        val userId = userManager.getId() ?: return

        viewModelScope.launch {
            try {
                _isFollowLoading.value = true
                val newFollowState = userClient.value?.getFollowStatus(userId, artistId) ?: false
                _isFollowed.value = newFollowState
            } catch (e: Exception) {
                Log.e(TAG, "Error handling like", e)
                _isFollowed.value = false
            } finally {
                _isFollowLoading.value = false
            }
        }
    }

    fun handleFollow(artistId: Int) {
        val userId = userManager.getId() ?: return

        viewModelScope.launch {
            try {
                _isFollowLoading.value = true
                val newFollowState = userClient.value?.toggleFollow(userId, artistId) ?: false
                _isFollowed.value = newFollowState
            } catch (e: Exception) {
                Log.e(TAG, "Error handling like", e)
                _isFollowed.value = false
            } finally {
                _isFollowLoading.value = false
            }
        }
    }

    fun fetchFullInfoArtist(artistId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val client = songClient.value ?: run {
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
            val client = songClient.value ?: run {
                Log.d("ArtistViewModel", "Client not initialized")
                return@launch
            }
            val gClient = genreClient.value ?: run {
                Log.d("ArtistViewModel", "Genre client not initialized")
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
                val userId = userManager.getId() ?: return@launch
                val updated = userClient.value?.updateHistory(userId, songId)
                if(updated == true) {
                    Log.d("ArtistViewModel", "Play history updated successfully")
                } else {
                    Log.e("ArtistViewModel", "Failed to update play history")
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