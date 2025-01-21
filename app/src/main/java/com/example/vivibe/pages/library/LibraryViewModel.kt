package com.example.vivibe.pages.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.DownloadedSong
import com.example.vivibe.model.PlaylistReview
import com.example.vivibe.model.SongDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(appContext: Context, private val userManager: UserManager) : ViewModel() {
    private val userClient = UserClient(appContext, userManager.getToken())
    private val googleAuthClient = GoogleSignInClient(appContext)
    private val dbHelper = DatabaseHelper(appContext)
    val user = userManager.userState

    private val _currentView = MutableStateFlow("Library")
    val currentView: StateFlow<String> = _currentView

    private val _playlists = MutableStateFlow<List<PlaylistReview>>(emptyList())
    val playlists: StateFlow<List<PlaylistReview>> = _playlists

    private val _likedArtists = MutableStateFlow<List<ArtistDetail>>(emptyList())
    val likedArtists: StateFlow<List<ArtistDetail>> = _likedArtists

    private val _downloadedSongs = MutableStateFlow<List<DownloadedSong>>(emptyList())
    val downloadedSongs: StateFlow<List<DownloadedSong>> = _downloadedSongs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun updateCurrentView(newView: String) {
        _currentView.value = newView
    }

    fun initializeLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchPlaylists()
            fetchLikedArtists()
            _isLoading.value = false
        }
    }

    private suspend fun fetchPlaylists() {
        viewModelScope.launch {
            try {
                val userId = userManager.getId() ?: return@launch
                val playlists = userClient.getPlaylists(userId)
                _playlists.value = playlists
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchLikedArtists() {
        viewModelScope.launch {
            try {
                val userId = userManager.getId() ?: return@launch
                val artists = userClient.getLikedArtists(userId)
                _likedArtists.value = artists
            } catch (e: Exception) {
                e.printStackTrace()
            }            }

    }

    suspend fun getDownloadedSongs() {
        viewModelScope.launch {
            try {
                val response = dbHelper.getAllDownloadedSongs(userManager.getGoogleId().orEmpty())
                _downloadedSongs.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun signIn(activityContext: Context): Boolean {
        val success = googleAuthClient.signIn(activityContext)
        if (success) {
            userManager.resetTokenExpired()
        }
        return success
    }

    suspend fun signOut(): Boolean {
        return googleAuthClient.signOut()
    }
}