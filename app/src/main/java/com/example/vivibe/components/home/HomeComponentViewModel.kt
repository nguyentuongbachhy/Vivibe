package com.example.vivibe.components.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.model.Genre
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SpeedDialSong
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.ArtistAlbum
import com.example.vivibe.model.GenreSongs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeComponentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val songClient: SongClient,
    private val genreClient: GenreClient,
    private val dbHelper: DatabaseHelper,
    private val userManager: UserManager,
    private val exoPlayer: SharedExoPlayer
): ViewModel() {
    data class HomeComponentState(
        val speedDial: List<SpeedDialSong> = emptyList(),
        val quickPicks: List<QuickPicksSong> = emptyList(),
        val forgottenFavorites: List<QuickPicksSong> = emptyList(),
        val newReleases: List<QuickPicksSong> = emptyList(),
        val albums: List<ArtistAlbum> = emptyList(),
        val genreList: List<Genre> = emptyList(),
        val genreSongs: List<GenreSongs> = emptyList()
    )

    private val _state = MutableStateFlow(HomeComponentState())
    val state = _state.asStateFlow()

    private var isInitialized = false


    init {
        println("HomeComponentViewModel initialized")
        viewModelScope.launch {
            println("Starting initial data load")
            reloadPage()
        }
    }

    private fun loadStuff() {
        if (isInitialized) return
        viewModelScope.launch {
            reloadPage()
            isInitialized = true
        }
    }

    fun reloadPage() {
        viewModelScope.launch {
            fetchGenreList()
            fetchQuickPicks()
            fetchSpeedDial()
            fetchForgottenFavorites()
            fetchNewReleases()
            fetchArtistAlbums()
        }
    }

    private fun fetchGenreList() {
        viewModelScope.launch {
            try {
                val fetchedGenres = genreClient.fetchGenres()
                if(fetchedGenres.isNotEmpty()) {
                    _state.value = _state.value.copy(genreList = fetchedGenres)
                }
            } catch(e: Exception) {
                println("Error fetching genres: ${e.message}")
            }
        }
    }

    private fun fetchSpeedDial() {
        viewModelScope.launch {
            try {
                val fetchedSongs = songClient.fetchSpeedDialSongs()
                if (fetchedSongs.isNotEmpty()) {
                    _state.value = _state.value.copy(speedDial = fetchedSongs)
                }
            } catch (e: Exception) {
                println("Error fetching speed dial: ${e.message}")
            }
        }
    }

    private fun fetchQuickPicks() {
        val googleId = userManager.getGoogleId()
        viewModelScope.launch {
            if (googleId.isNullOrBlank()) {
                _state.value = _state.value.copy(quickPicks = emptyList())
                return@launch
            }
            try {
                val songIds = dbHelper.getTopSongs(googleId, limit = 20)
                val fetchedSongs = songClient.fetchSongs(songIds)
                if (fetchedSongs.isNotEmpty()) {
                    _state.value = _state.value.copy(quickPicks = fetchedSongs)
                }
            } catch (e: Exception) {
                println("Error fetching quick picks: ${e.message}")
            }
        }
    }

    private fun fetchForgottenFavorites() {
        val googleId = userManager.getGoogleId()
        viewModelScope.launch {
            if (googleId.isNullOrBlank()) {
                _state.value = _state.value.copy(forgottenFavorites = emptyList())
                return@launch
            }
            try {
                val songIds = dbHelper.getForgottenFavorites(googleId)
                val fetchedSongs = songClient.fetchSongs(songIds)
                if (fetchedSongs.isNotEmpty()) {
                    _state.value = _state.value.copy(forgottenFavorites = fetchedSongs)
                }
            } catch (e: Exception) {
                println("Error fetching forgotten favorites: ${e.message}")
            }
        }
    }

    private fun fetchNewReleases() {
        viewModelScope.launch {
            try {
                println("Starting to fetch new releases")
                val fetchedSongs = songClient.fetchNewRelease()
                println("Fetched new releases: ${fetchedSongs.size}")

                if (fetchedSongs.isNotEmpty()) {
                    val oldState = _state.value
                    println("Current newReleases size: ${oldState.newReleases.size}")

                    _state.value = oldState.copy(newReleases = fetchedSongs)
                    println("Updated newReleases size: ${_state.value.newReleases.size}")
                }
            } catch (e: Exception) {
                println("Error fetching new releases: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun fetchArtistAlbums() {
        val googleId = userManager.getGoogleId()
        viewModelScope.launch {
            if (googleId.isNullOrBlank()) {
                _state.value = _state.value.copy(albums = emptyList())
                return@launch
            }
            val artistIds = dbHelper.getTopArtists(googleId, limit = 5)
            val fetchedAlbums = songClient.fetchAlbums(artistIds)
            if (fetchedAlbums.isNotEmpty()) {
                _state.value = _state.value.copy(albums = fetchedAlbums)
            }
        }
    }

    fun fetchSongsByGenre(genreId: Int) {
        viewModelScope.launch {
            val fetchedSongsByGenre = songClient.fetchSongsByGenre(genreId)
            if (fetchedSongsByGenre.isNotEmpty()) {
                savedStateHandle["genre_songs"] = fetchedSongsByGenre
            }
        }
    }

    fun fetchPlayAll(songIds: List<Int>) {
        viewModelScope.launch {
            val fetchedPlayAll = songClient.fetchPlayAll(songIds)
            if (fetchedPlayAll.isNotEmpty()) {
                exoPlayer.updateSongs(fetchedPlayAll)
            }
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