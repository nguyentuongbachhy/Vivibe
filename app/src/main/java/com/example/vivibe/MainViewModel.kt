package com.example.vivibe

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.manager.GlobalStateManager
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val appContext: Context) : ViewModel() {

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> get() = _showBottomSheet

    private val _currentBottomSheetSong = MutableStateFlow<QuickPicksSong?>(null)
    val currentBottomSheetSong: StateFlow<QuickPicksSong?> get() = _currentBottomSheetSong

    private val _playingSong = MutableStateFlow<PlaySong?>(null)
    val playingSong: StateFlow<PlaySong?> get() = _playingSong

    val songClient = mutableStateOf<SongClient?>(null)


    init {
        GlobalStateManager.loadUserFromFile(appContext)
        songClient.value = SongClient(appContext, GlobalStateManager.userState.value.token ?: "")

        viewModelScope.launch {
            GlobalStateManager.userState.collect { user ->
                songClient.value = SongClient(appContext, user.token ?: "")
            }
        }
    }


    fun showBottomSheet(song: QuickPicksSong) {
        viewModelScope.launch {
            _currentBottomSheetSong.value = song
            _showBottomSheet.value = true
        }
    }

    fun hideBottomSheet() {
        viewModelScope.launch {
            _currentBottomSheetSong.value = null
            _showBottomSheet.value = false
        }
    }


    fun fetchPlaySong(songId: Int) {
        viewModelScope.launch {
            try {
                val fetchedPlaySong = songClient.value?.fetchPlayingSong(songId)
                if(fetchedPlaySong != null) {
                    _playingSong.value = fetchedPlaySong
                    println("Playing song fetched successfully")
                } else {
                    println("No playing song fetched")
                }
            } catch (e: Exception) {
                println("Error fetching playing song: ${e.message}")
            }
        }
    }

}