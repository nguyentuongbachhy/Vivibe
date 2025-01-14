package com.example.vivibe

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.song.DownloadManager
import com.example.vivibe.components.song.PlaybackService
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.DownloadedSong
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer) : ViewModel() {
    private val dbHelper = DatabaseHelper(appContext)
    private val songClient = MutableStateFlow<SongClient?>(null)
    private val downloadManager = MutableStateFlow<DownloadManager?>(null)

    private val _downloadedSongs = MutableStateFlow<Set<Int>>(emptySet())

    private val _downloadProgress = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<Int, Float>> = _downloadProgress

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> get() = _showBottomSheet

    private val _currentBottomSheetSong = MutableStateFlow<QuickPicksSong?>(null)
    val currentBottomSheetSong: StateFlow<QuickPicksSong?> get() = _currentBottomSheetSong

    private val _isLikedBottomSheetSong = MutableStateFlow(false)
    val isLikedBottomSheetSong: StateFlow<Boolean> get() = _isLikedBottomSheetSong

    private val _isDislikedBottomSheetSong = MutableStateFlow(false)
    val isDislikedBottomSheetSong: StateFlow<Boolean> get() = _isDislikedBottomSheetSong

    val currentSongId = exoPlayer.currentSongId
    val listSong = exoPlayer.listSong
    val isPlaying = exoPlayer.isPlaying
    val isShuffle = exoPlayer.isShuffleEnabled
    val isRepeat = exoPlayer.isRepeatEnabled
    val currentPosition = exoPlayer.currentPosition
    val duration = exoPlayer.duration

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
        initializeUserState()
        refreshDownloadedSongs()
    }

    private fun refreshDownloadedSongs() {
        viewModelScope.launch {
            val googleId = userManager.getGoogleId() ?: return@launch
            val songs = dbHelper.getAllDownloadedSongs(googleId)
            _downloadedSongs.value = songs.map { it.id }.toSet()
        }
    }

    fun isDownloaded(songId: Int): Boolean {
        return _downloadedSongs.value.contains(songId)
    }

    private fun initializeUserState() {
        viewModelScope.launch {
            userManager.userState.collect { user ->
                handlePlaybackService(user?.premium == 1)
                songClient.value = SongClient(appContext, user?.token.orEmpty())
                if (songClient.value != null) {
                    downloadManager.value = DownloadManager(appContext, dbHelper,
                        songClient.value!!
                    )
                }
            }
        }
    }

    private fun handlePlaybackService(isPremium: Boolean) {
        if (isPremium) {
            bindPlaybackService()
        } else {
            stopBackgroundPlayback()
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
                    exoPlayer.pause()
                    stopBackgroundPlayback()
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                if (exoPlayer.isPlaying.value) {
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
            }
            else -> {}
        }
    }

    fun showBottomSheet(song: QuickPicksSong) {
        Log.d("MainViewModel", "Received song: $song")
        val googleId = userManager.getGoogleId()
        viewModelScope.launch {
            try {
                _currentBottomSheetSong.value = song
                _showBottomSheet.value = true

                updateBottomSheetLikeStatus(song.id)

                Log.d("MainViewModel", "Bottom sheet shown for song ${song.id}")
                Log.d("MainViewModel", "GoogleId: $googleId")
                Log.d("MainViewModel", "Like status: ${_isLikedBottomSheetSong.value}")
                Log.d("MainViewModel", "Dislike status: ${_isDislikedBottomSheetSong.value}")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error showing bottom sheet", e)
                e.printStackTrace()
            }
        }
    }

    fun hideBottomSheet() {
        viewModelScope.launch {
            _currentBottomSheetSong.value = null
            _showBottomSheet.value = false
            resetBottomSheetLikeStatus()
        }
    }

    private fun updateBottomSheetLikeStatus(songId: Int) {
        val googleId = userManager.getGoogleId()
        if (googleId.isNullOrBlank()) {
            resetBottomSheetLikeStatus()
            return
        }

        _isLikedBottomSheetSong.value = dbHelper.isLikedSong(googleId, songId)
        _isDislikedBottomSheetSong.value = dbHelper.isDislikedSong(googleId, songId)
    }

    private fun resetBottomSheetLikeStatus() {
        _isLikedBottomSheetSong.value = false
        _isDislikedBottomSheetSong.value = false
    }


    fun fetchPlaySong(songId: Int) {
        viewModelScope.launch {
            try {
                val fetchedPlaySongs = songClient.value?.fetchPlayingSong(songId) ?: return@launch
                if (fetchedPlaySongs.isNotEmpty()) {
                    exoPlayer.updateSongs(fetchedPlaySongs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching playing song", e)
            }
        }
    }

    fun updateLikeStatusBottomSheet(songId: Int) {
        val googleId = userManager.getGoogleId() ?: return
        val newLikeState = !_isLikedBottomSheetSong.value

        viewModelScope.launch {
            try {
                _isLikedBottomSheetSong.value = newLikeState

                val response = songClient.value?.updateLikes(songId.toString(), newLikeState)
                if (response.isNullOrBlank()) {
                    _isLikedBottomSheetSong.value = !newLikeState
                    return@launch
                }

                dbHelper.updateLikedStatus(googleId, songId, newLikeState)

                if (newLikeState && _isDislikedBottomSheetSong.value) {
                    _isDislikedBottomSheetSong.value = false
                    dbHelper.updateDislikedStatus(googleId, songId, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating like status", e)
                _isLikedBottomSheetSong.value = !newLikeState
            }
        }
    }


    fun updateDislikeStatusBottomSheet(songId: Int) {
        val googleId = userManager.getGoogleId() ?: return
        val newDislikeState = !_isDislikedBottomSheetSong.value

        viewModelScope.launch {
            try {
                _isDislikedBottomSheetSong.value = newDislikeState
                dbHelper.updateDislikedStatus(googleId, songId, newDislikeState)

                if (newDislikeState && _isLikedBottomSheetSong.value) {
                    _isLikedBottomSheetSong.value = false
                    dbHelper.updateLikedStatus(googleId, songId, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating dislike status", e)
                _isDislikedBottomSheetSong.value = !newDislikeState
            }
        }
    }

    fun prepareSong(song: PlaySong) = exoPlayer.prepareSong(song)
    fun playPause() = exoPlayer.playPause()
    fun previous() = exoPlayer.handlePreviousTrack()
    fun next() = exoPlayer.handleNextTrack()
    fun shuffle() = exoPlayer.handleShuffle()
    fun repeat() = exoPlayer.handleRepeat()
    fun playOneInListSong(songId: Int) = exoPlayer.handlePlayOneInListSong(songId)
    fun seekToProgress(progress: Float) = exoPlayer.seekToProgress(progress)
    fun reset() = exoPlayer.reset()

    fun downloadSong(songId: Int) {
        viewModelScope.launch {
            try {
                val googleId = userManager.getGoogleId() ?: return@launch

                _downloadProgress.update { it + (songId to 0f) }

                downloadManager.value?.downloadSong(googleId, songId) { progress ->
                    _downloadProgress.update { it + (songId to progress) }
                }

                    ?.onSuccess {
                        // Add to downloaded songs set
                        _downloadedSongs.update { it + songId }
                        refreshDownloadedSongs()
                        _downloadProgress.update { it - songId }
                        println("$TAG: Song downloaded successfully")
                    }
                    ?.onFailure { error ->
                        _downloadProgress.update { it - songId }
                        Log.e(TAG, "Failed to download song", error)
                    }
            } catch (e: Exception) {
                _downloadProgress.update { it - songId }
                Log.e(TAG, "Error during download", e)
            }
        }
    }

    fun getDownloadedSongs(): List<DownloadedSong> {
        val googleId = userManager.getGoogleId() ?: return emptyList()
        return downloadManager.value!!.getAllDownloadedSongs(googleId)
    }

    fun deleteDownloadedSong(songId: Int) {
        viewModelScope.launch {
            val googleId = userManager.getGoogleId() ?: return@launch
            downloadManager.value!!.deleteDownloadedSong(googleId, songId)
        }
    }

    fun updateDownloadProgress(songId: Int, progress: Float) {
        viewModelScope.launch {
            _downloadProgress.update { current ->
                current + (songId to progress)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBackgroundPlayback()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}