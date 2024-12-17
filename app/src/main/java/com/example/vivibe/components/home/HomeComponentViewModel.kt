package com.example.vivibe.components.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.Genre
import com.example.vivibe.QuickPicksSong
import com.example.vivibe.SpeedDialSong
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeComponentViewModel(
    appContext: Context,
    token: String,
    private val googleId: String,
    private val songClient: SongClient,
): ViewModel() {
    private val genreClient = GenreClient(appContext, token)
    private val dbHelper = DatabaseHelper(appContext)

    private val _speedDial = MutableStateFlow<List<SpeedDialSong>>(emptyList())
    val speedDial: StateFlow<List<SpeedDialSong>> get() = _speedDial

    private val _quickPicks = MutableStateFlow<List<QuickPicksSong>>(emptyList())
    val quickPicks: StateFlow<List<QuickPicksSong>> get() = _quickPicks

    private val _genreList = MutableStateFlow<List<Genre>>(emptyList())
    val genreList: StateFlow<List<Genre>> get() = _genreList


    init {
        loadStuff()
    }

    private fun loadStuff() {
        viewModelScope.launch {
            fetchGenreList()
            fetchQuickPicks()
            fetchSpeedDial()
        }
    }

    private fun fetchGenreList() {
        viewModelScope.launch {
            try {
                val fetchedGenres = genreClient.fetchGenres()
                if (fetchedGenres.isNotEmpty()) {
                    _genreList.value = fetchedGenres
                    println("Genres fetched successfully")
                } else {
                    println("No genres fetched")
                }
            } catch (e: Exception) {
                println("Error fetching genres: ${e.message}")
            }
        }
    }

    fun fetchQuickPicks() {
        viewModelScope.launch {
            try {
                val songIds = dbHelper.getTopSongs(googleId, limit = 20)
                val fetchedSongs = songClient.fetchQuickPickSongs(songIds)
                if(fetchedSongs.isNotEmpty()) {
                    _quickPicks.value = fetchedSongs
                    println("Quick picks fetched successfully")
                } else {
                    println("No quick picks fetched")
                }
            } catch (e: Exception) {
                println("Error fetching quick picks: ${e.message}")
            }
        }
    }

    fun fetchSpeedDial() {
        viewModelScope.launch {
            try {
                val fetchedSongs = songClient.fetchSpeedDialSongs()
                if (fetchedSongs.isNotEmpty()) {
                    _speedDial.value = fetchedSongs
                    println("Speed dial fetched successfully")
                } else {
                    println("No speed dial fetched")
                }
            } catch (e: Exception) {
                println("Error fetching speed dial: ${e.message}")
            }
        }
    }

    fun updatePlayHistory(songId: Int) {
        dbHelper.insertOrUpdateSongPlayHistory(songId, googleId)
    }
}