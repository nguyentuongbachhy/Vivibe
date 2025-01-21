package com.example.vivibe.pages.samples

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SwipePlayerManager
import com.example.vivibe.manager.UserManager
import kotlinx.coroutines.launch

class SampleViewModel(context: Context, private val userManager: UserManager) : ViewModel() {
    private val swipePlayer = SwipePlayerManager(context, userManager)
    private val dbHelper = DatabaseHelper(context)
    private val tag = "SampleViewModel"

    val isPlaying = swipePlayer.isPlaying
    val duration = swipePlayer.duration
    val currentSongId = swipePlayer.currentSongId
    val currentIndex = swipePlayer.currentIndex
    val error = swipePlayer.error
    val listSong = swipePlayer.listSong

    fun fetchSwipeSongs() {
        viewModelScope.launch {
            swipePlayer.reinitializePlayer()
            val googleId = userManager.getGoogleId()
            Log.d(tag, "Google ID: $googleId")

            val genreIds = googleId?.let { id ->
                val genres = dbHelper.getTopGenres(id)
                Log.d(tag, "Genre IDs: $genres")
                genres
            } ?: emptyList()

            swipePlayer.fetchSwipeSongs(genreIds)
        }
    }

    fun resetPlayer() = swipePlayer.resetPlayer()

    fun playPause() = swipePlayer.playPause()
    fun play() = swipePlayer.play()
    fun onPageChanged(newPage: Int) = swipePlayer.onPageChanged(newPage)
    fun onScreenPause() = swipePlayer.onScreenPause()
    fun onScreenResume() = swipePlayer.onScreenResume()

    override fun onCleared() {
        super.onCleared()
        swipePlayer.release()
    }
}