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
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.components.song.DownloadManager
import com.example.vivibe.components.song.PlaybackService
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.DownloadedSong
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.model.SwipeSong
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModel(private val appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer) : ViewModel() {
    private val dbHelper = DatabaseHelper(appContext)
    private val songClient = MutableStateFlow<SongClient?>(null)
    private val userClient = MutableStateFlow<UserClient?>(null)
    private val genreClient = MutableStateFlow<GenreClient?>(null)
    private val downloadManager = MutableStateFlow<DownloadManager?>(null)
    private val googleAuthClient = GoogleSignInClient(appContext)

    private val _downloadedSongs = MutableStateFlow<Set<Int>>(emptySet())

    private val _downloadProgress = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<Int, Float>> = _downloadProgress

    private val _isShowBottomSheet = MutableStateFlow(false)
    val isShowBottomSheet: StateFlow<Boolean> get() = _isShowBottomSheet

    private val _showComments = MutableStateFlow(false)
    val showComments: StateFlow<Boolean> get() = _showComments

    private val _relatedSongs = MutableStateFlow<List<SwipeSong>>(emptyList())
    val relatedSongs: MutableStateFlow<List<SwipeSong>> = _relatedSongs

    private val _isLoadingRelated = MutableStateFlow(false)
    val isLoadingRelated: MutableStateFlow<Boolean> = _isLoadingRelated

    private val _currentBottomSheetSong = MutableStateFlow<QuickPicksSong?>(null)
    val currentBottomSheetSong: StateFlow<QuickPicksSong?> get() = _currentBottomSheetSong

    private val _currentSongCommentId = MutableStateFlow<Int>(0)
    val currentSongCommentId: StateFlow<Int> get() = _currentSongCommentId

    private val _isLikedBottomSheetSong = MutableStateFlow(false)
    val isLikedBottomSheetSong: StateFlow<Boolean> get() = _isLikedBottomSheetSong

    private val _isUpdatingLikeStatus = MutableStateFlow(false)

    private val _errorMessage = MutableStateFlow<String?>(null)

    val currentSongId = exoPlayer.currentSongId
    val listSong = exoPlayer.listSong
    val isPlaying = exoPlayer.isPlaying
    val isShuffle = exoPlayer.isShuffleEnabled
    val isRepeat = exoPlayer.isRepeatEnabled
    val currentPosition = exoPlayer.currentPosition
    val duration = exoPlayer.duration
    val isLikedCurrentSong = exoPlayer.isLikedCurrentSong
    val isUpdatingLike = exoPlayer.isUpdatingLike


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
        initialize()
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

    private fun initialize() {
        viewModelScope.launch {
            userManager.userState.collect { user ->
                Log.d(TAG, "User state changed: ${user != null}") // Thêm log
                handlePlaybackService(user?.premium == 1)

                // Khởi tạo clients
                val token = user?.token.orEmpty()
                Log.d(TAG, "Initializing clients with token: ${token.isNotEmpty()}") // Thêm log

                songClient.value = SongClient(appContext, token)
                userClient.value = UserClient(appContext, token)
                genreClient.value = GenreClient(appContext, token)

                if (songClient.value != null && userClient.value != null) {
                    Log.d(TAG, "Clients initialized successfully") // Thêm log
                    downloadManager.value = DownloadManager(
                        appContext,
                        dbHelper,
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
        try {
            // Check if context and required dependencies are available

            Intent(appContext, PlaybackService::class.java).also { intent ->
                try {
                    appContext.startService(intent)
                    appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                } catch (e: Exception) {
                    Log.e(TAG, "Error binding PlaybackService", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in bindPlaybackService", e)
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
        viewModelScope.launch {
            try {
                _currentBottomSheetSong.value = song
                _isShowBottomSheet.value = true

                val client = userClient.value
                if (client != null) {
                    val userId = userManager.getId()
                    if (!userId.isNullOrBlank()) {
                        try {
                            _isUpdatingLikeStatus.value = true
                            _isLikedBottomSheetSong.value = client.getLikeStatus(userId, song.id)
                            Log.d("MainViewModel", "Like status fetched successfully for song ${song.id}")
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Error getting like status", e)
                        } finally {
                            _isUpdatingLikeStatus.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error showing bottom sheet", e)
                e.printStackTrace()
            }
        }
    }

    fun hideBottomSheet() {
        viewModelScope.launch {
            _currentBottomSheetSong.value = null
            _isShowBottomSheet.value = false
            resetBottomSheetLikeStatus()
        }
    }

    private fun resetBottomSheetLikeStatus() {
        _isLikedBottomSheetSong.value = false
    }


    fun fetchPlaySong(songId: Int) {
        val googleId = userManager.getGoogleId() ?: return
        viewModelScope.launch {
            try {
                val fetchedPlaySongs = songClient.value?.fetchPlayingSong(songId)
                Log.d(TAG, "FetchedPlaySongs: $fetchedPlaySongs")

                if (!fetchedPlaySongs.isNullOrEmpty()) {
                    exoPlayer.updateSongs(fetchedPlaySongs)
                }

                Log.d(TAG, "GenreClient: ${genreClient.value}") // Check genreClient
                val genreIds = genreClient.value?.fetchGenresSong(songId)
                Log.d(TAG, "Fetched genreIds: $genreIds") // Check fetched genreIds

                if (!genreIds.isNullOrEmpty()) {
                    val result = dbHelper.insertOrUpdateGenrePlayHistory(genreIds, googleId)
                    Log.d(TAG, "DB Update result: $result") // Check DB update result
                    Log.d(TAG, "Updated genreIds: $genreIds")
                } else {
                    Log.e(TAG, "GenreIds is null or empty")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching playing song", e)
            }
        }
    }

    fun updateLikeStatusBottomSheet(songId: Int) {
        Log.d(TAG, "Starting updateLikeStatusBottomSheet for songId: $songId")

        val userId = userManager.getId() ?: run {
            Log.e(TAG, "UserId is null")
            _errorMessage.value = "Please sign in to like songs"
            return
        }

        Log.d(TAG, "UserId retrieved: $userId")

        if (_isUpdatingLikeStatus.value) {
            Log.d(TAG, "Already updating like status, returning")
            return
        }

        viewModelScope.launch {
            try {
                val client = userClient.value
                Log.d(TAG, "LikeClient obtained: ${client != null}")
                if(client != null) {
                    try {
                        _isUpdatingLikeStatus.value = true
                        Log.d(TAG, "Calling toggleLike for userId: $userId, songId: $songId")
                        val result = client.toggleLike(userId, songId)
                        Log.d(TAG, "ToggleLike result: $result") // Thêm log
                        _isLikedBottomSheetSong.value = result
                        Log.d("MainViewModel", "Like status updated successfully for song $songId")
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error updating like status", e)
                    } finally {
                        _isUpdatingLikeStatus.value = false
                    }
                } else {
                    Log.e(TAG, "LikeClient is null") // Thêm log
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to update like status: ${e.message}"
                Log.e(TAG, "Error updating like status", e)
            } finally {
                delay(3000)
                _errorMessage.value = null
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
                        _downloadProgress.update { it - songId }
                        refreshDownloadedSongs()
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
            try {
                val googleId = userManager.getGoogleId() ?: return@launch
                val isSuccess = downloadManager.value!!.deleteDownloadedSong(googleId, songId)
                if(isSuccess) {
                    _downloadedSongs.update { it - songId }
                    refreshDownloadedSongs()

                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting downloaded song", e)
            }
        }
    }

    fun shareSong(songId: Int) {
        viewModelScope.launch {
            if(songClient.value == null) return@launch
            try {
                val song = songClient.value!!.fetchDownloadedSong(songId) ?: return@launch

                val songData = EncryptionUtils.createSongData(
                    title = song.title,
                    artistName = song.artist.name,
                    thumbnailUrl = song.thumbnailUrl,
                    audioUrl = song.audio,
                    views = song.views
                )

                val encryptedData = EncryptionUtils.encrypt(songData)

                val shareUrl = "https://nguyentuongbachhy.github.io/youtube_music/?d=$encryptedData"

                val shareText = """
                    🎵 ${song.title}
                    🎤 ${song.artist.name}
                                    
                    Listen now: $shareUrl
                """.trimIndent()

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    // Optional: Thêm title cho share dialog
                    putExtra(Intent.EXTRA_TITLE, "${song.title} - ${song.artist.name}")
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Share via")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(chooserIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing song", e)
            }
        }
    }

    fun handleLikeCurrentSong() = exoPlayer.handleLike()


    fun showComments(songId: Int) {
        _currentSongCommentId.value = songId
        _showComments.value = true
    }

    fun hideComments() {
        _currentSongCommentId.value = 0
        _showComments.value = false
    }

    suspend fun signOut(): Boolean {
        return googleAuthClient.signOut()
    }

    fun updateHistory(userId: String, songId: Int) {
        viewModelScope.launch {
            try{
                val updated = userClient.value?.updateHistory(userId, songId)
                if(updated == true) {
                    Log.d(TAG, "Updated history successfully")
                } else {
                    Log.e(TAG, "Failed to update history")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Error updating history", e)
            }
        }
    }

    fun fetchRelatedSongs(songId: Int) {
        viewModelScope.launch {
            try {
                _isLoadingRelated.value = true
                val genreIds = genreClient.value?.fetchGenresSong(songId)
                if(genreIds.isNullOrEmpty()) return@launch
                val response = songClient.value?.fetchSwipeSongs(genreIds)
                if(response != null) {
                    _relatedSongs.value = response
                }
                _isLoadingRelated.value = false
            } catch (e: Exception) {
                _isLoadingRelated.value = false
                e.printStackTrace()
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