package com.example.vivibe

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.song.PlaybackService
import com.example.vivibe.manager.GlobalStateManager
import com.example.vivibe.manager.SharedExoPlayer
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

    private val _currentSong = MutableStateFlow<PlaySong?>(null)
    val currentSong: StateFlow<PlaySong?> get() = _currentSong

    val isPlaying = SharedExoPlayer.isPlaying
    val currentPosition = SharedExoPlayer.currentPosition
    val duration = SharedExoPlayer.duration

    val songClient = mutableStateOf<SongClient?>(null)

    @SuppressLint("StaticFieldLeak")
    private var playbackService: PlaybackService? = null
    private var serviceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.LocalBinder
            playbackService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            serviceBound = false
        }
    }

    init {
        GlobalStateManager.loadUserFromFile(appContext)
        SharedExoPlayer.initialize(appContext)

        if(GlobalStateManager.userState.value.premium == 1) {
            bindPlaybackService()
        } else {
            stopBackgroundPlayback()
        }

        viewModelScope.launch {
            GlobalStateManager.userState.collect { user ->
                songClient.value = SongClient(appContext, user.token ?: "")
            }
        }

        viewModelScope.launch {
            SharedExoPlayer.currentSong.collect { song ->
                _currentSong.value = song
            }
        }
    }

    private fun bindPlaybackService() {
        Intent(appContext, PlaybackService::class.java).also { intent ->
            appContext.startService(intent)
            appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopBackgroundPlayback() {
        if(serviceBound) {
            appContext.unbindService(serviceConnection)
            serviceBound = false
        }

        appContext.stopService(Intent(appContext, PlaybackService::class.java))
    }


    fun handleLifecycleEvent(event: Lifecycle.Event, isPremium: Int) {
        when(event) {
            Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                if (isPremium == 0) {
                    SharedExoPlayer.pause()
                    stopBackgroundPlayback()
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                if (SharedExoPlayer.isPlaying.value) {
                    SharedExoPlayer.play()
                } else {
                    SharedExoPlayer.pause()
                }
            }
            else -> {}
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
                    _currentSong.value = fetchedPlaySong
                    prepareSong(fetchedPlaySong)
                    println("Playing song fetched successfully")
                } else {
                    println("No playing song fetched")
                }
            } catch (e: Exception) {
                println("Error fetching playing song: ${e.message}")
            }
        }
    }

    fun prepareSong(song: PlaySong) {
        SharedExoPlayer.prepareSong(song)
    }

    fun playPause() {
        SharedExoPlayer.playPause()
    }

    private fun playPlayer() {
        SharedExoPlayer.play()
    }

    private fun pausePlayer() {
        SharedExoPlayer.pause()
    }

    fun seekTo(position: Long) {
        SharedExoPlayer.seekTo(position)
    }

    fun seekToProgress(progress: Float) {
        SharedExoPlayer.seekToProgress(progress)
    }
}