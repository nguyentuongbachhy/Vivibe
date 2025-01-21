package com.example.vivibe.pages.common

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.common.CommonClient
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.AlbumReview
import com.example.vivibe.model.ArtistDetail
import com.example.vivibe.model.ArtistReview
import com.example.vivibe.model.Genre
import com.example.vivibe.model.QuickPicksSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CommonViewModel(context: Context, private val userManager: UserManager): ViewModel() {
    private val userClient = UserClient(context, userManager.getToken().orEmpty())
    private val genreClient = GenreClient(context,  userManager.getToken().orEmpty())
    private val commonClient = CommonClient(context, userManager.getToken().orEmpty())
    private val dbHelper = DatabaseHelper(context)

    private val _latestAlbum = MutableStateFlow<AlbumReview?>(null)
    val latestAlbum: MutableStateFlow<AlbumReview?> = _latestAlbum

    private val _albums = MutableStateFlow<List<AlbumReview>>(emptyList())
    val albums: MutableStateFlow<List<AlbumReview>> = _albums

    private val _artists = MutableStateFlow<List<ArtistReview>>(emptyList())
    val artists: MutableStateFlow<List<ArtistReview>> = _artists

    private val _topSongs = MutableStateFlow<List<QuickPicksSong>>(emptyList())
    val topSongs: MutableStateFlow<List<QuickPicksSong>> = _topSongs

    private val _topArtists = MutableStateFlow<List<ArtistDetail>>(emptyList())
    val topArtists: MutableStateFlow<List<ArtistDetail>> = _topArtists

    private val _tagForYou = MutableStateFlow<List<Genre>>(emptyList())
    val tagForYou: MutableStateFlow<List<Genre>> = _tagForYou

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: MutableStateFlow<List<Genre>> = _genres

    private val _isLoading = MutableStateFlow(false)
    val isLoading: MutableStateFlow<Boolean> = _isLoading

    fun initializeNewRelease() {
        viewModelScope.launch {
            _isLoading.value = true
            reset()
            fetchLatestAlbum()
            fetchAlbumsAndSongs()
            _isLoading.value = false
        }
    }

    fun initializeCharts() {
        viewModelScope.launch {
            _isLoading.value = true
            reset()
            fetchTopSongs()
            fetchTopArtists()
            _isLoading.value = false
        }
    }

    fun initializeMoodsGenres() {
        viewModelScope.launch {
            _isLoading.value = true
            reset()
            fetchTagForYou()
            fetchGenres()
            _isLoading.value = false
        }
    }

    private suspend fun fetchLatestAlbum() {
        try {
            val album = commonClient.getLatestAlbum()
            if(album != null) {
                _latestAlbum.value = album
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchAlbumsAndSongs() {
       try {
           val response = commonClient.getAlbumsAndSongs()
           if(response != null) {
               _albums.value = response.albums
               _artists.value = response.artists
           }
       } catch (e: Exception) {
           e.printStackTrace()
       }
    }

    private suspend fun fetchTopSongs() {
        try {
            val response = commonClient.fetchTopSongs()
            if(response.isNotEmpty()) {
                _topSongs.value = response
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchTopArtists() {
        try {
            val response = commonClient.fetchTopArtists()
            if(response.isNotEmpty()) {
                _topArtists.value = response
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchTagForYou() {
        try {
            val genreIds = dbHelper.getTopGenres(userManager.getGoogleId().orEmpty(), limit = 6)
            val response = genreClient.fetchGenreByIds(genreIds)
            if(response.isNotEmpty()) {
                _tagForYou.value = response
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchGenres() {
        try {
            val response = genreClient.fetchGenres()
            if(response.isNotEmpty()) {
                _genres.value = response
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePlayHistory(songId: Int) {
        viewModelScope.launch {
            try {
                val userId = userManager.getId() ?: return@launch
                val updated = userClient.updateHistory(userId, songId)
                if(updated) {
                    Log.d("CommonViewModel", "Play history updated successfully")
                } else {
                    Log.e("CommonViewModel", "Failed to update play history")
                }
            } catch (e: Exception) {
                Log.e("ArtistViewModel", "Error updating play history", e)
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

    private fun reset() {
        _latestAlbum.value = null
        _albums.value = emptyList()
        _artists.value = emptyList()
        _topSongs.value = emptyList()
        _topArtists.value = emptyList()
        _tagForYou.value = emptyList()
        _genres.value = emptyList()
    }
}