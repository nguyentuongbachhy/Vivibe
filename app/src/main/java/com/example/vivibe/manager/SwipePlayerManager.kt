package com.example.vivibe.manager

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.model.SwipeSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class SwipePlayerManager(private val context: Context, private val userManager: UserManager) {
    private var player: ExoPlayer? = null
    private var playerScope: CoroutineScope? = null
    private var playerListener: Player.Listener? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var volumeContentObserver: ContentObserver? = null
    private var climaxEndJob: Job? = null
    private val songClient = SongClient(context, userManager.getToken())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _currentSongId = MutableStateFlow(-1)
    val currentSongId: StateFlow<Int> = _currentSongId

    private val _listSong = MutableStateFlow<List<SwipeSong>>(emptyList())
    val listSong: StateFlow<List<SwipeSong>> = _listSong

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isLoading = MutableStateFlow(false)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        initialize()
        setupVolumeControl()
    }

    private fun setupVolumeControl() {
        // Create content observer to listen for system volume changes
        volumeContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                updatePlayerVolume()
            }
        }

        // Register the observer
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeContentObserver!!
        )

        // Initial volume setup
        updatePlayerVolume()
    }

    private fun updatePlayerVolume() {
        try {
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumePercent = currentVolume.toFloat() / maxVolume.toFloat()

            player?.volume = volumePercent

            Log.d(TAG, "System volume updated: $volumePercent")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating volume", e)
        }
    }

    private fun initialize() {
        Log.d(TAG, "Starting player initialization")
        try {
            // Tạo player listener trước
            playerListener = createPlayerListener()

            // Khởi tạo ExoPlayer
            player = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                shuffleModeEnabled = false
                setHandleAudioBecomingNoisy(true)
                playWhenReady = true
            }

            updatePlayerVolume()

            player?.addListener(playerListener!!)
            playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            startPositionTracking()

            Log.d(TAG, "Player initialized successfully: ${player != null}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing player", e)
            e.printStackTrace()
        }
    }

    private fun ensurePlayerInitialized() {
        if (player == null) {
            Log.d(TAG, "Player is null, reinitializing...")
            initialize()
        }
    }

    private fun createPlayerListener(): Player.Listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "Playback state changed to: $playbackState")
            when (playbackState) {
                Player.STATE_READY -> {
                    Log.d(TAG, "Player is ready, duration: ${player?.duration}")
                    _duration.value = player?.duration ?: 0
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "Playback ended")
                    moveToNextSong()
                }
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "Player is buffering")
                }
                Player.STATE_IDLE -> {
                    Log.d(TAG, "Player is idle")
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "Is playing changed to: $isPlaying")
            _isPlaying.value = isPlaying
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error occurred", error)
            error.printStackTrace()
            _error.value = error.message
        }
    }

    private fun startPositionTracking() {
        playerScope?.launch {
            while(true) {
                player?.let { exoPlayer ->
                    val position = exoPlayer.currentPosition
                    val duration = exoPlayer.duration

                    if (duration > 0) {
                        _currentPosition.value = position.coerceIn(0L, duration)
                        _duration.value = duration
                    } else {
                        _currentPosition.value = 0L
                        _duration.value = 0L
                    }

                    _isPlaying.value = exoPlayer.isPlaying
                }
                delay(UPDATE_INTERVAL)
            }
        }
    }

    suspend fun fetchSwipeSongs(genreIds: List<Int>) {
        try {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "Fetching swipe songs with genres: $genreIds")
            val swipeSongs = songClient.fetchSwipeSongs(genreIds)
            Log.d(TAG, "Fetched swipe songs: ${swipeSongs.size}")
            if (swipeSongs.isNotEmpty()) {
                _listSong.value = swipeSongs
                _currentIndex.value = 0
                swipeSongs.firstOrNull()?.let { song ->
                    playFromClimaxPoint(song)
                }
            } else {
                _error.value = "No songs available"
            }
        } catch (e: Exception) {
            _error.value = e.message
            Log.e(TAG, "Error fetching songs", e)
        } finally {
            _isLoading.value = false
        }
    }

    private fun playFromClimaxPoint(song: SwipeSong) {
        ensurePlayerInitialized()

        if (player == null) {
            Log.e(TAG, "Player is still null after initialization attempt")
            _error.value = "Failed to initialize player"
            return
        }

        Log.d(TAG, "Attempting to play song: ${song.title}")
        Log.d(TAG, "Song audio URL: ${song.audio}")

        cancelClimaxEndJob()
        player?.let { exoPlayer ->
            try {
                Log.d(TAG, "Player current state before operations: ${exoPlayer.playbackState}")

                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                _currentSongId.value = song.id

                if (song.audio.isBlank()) {
                    Log.e(TAG, "Audio URL is null or blank")
                    _error.value = "Invalid audio URL"
                    return
                }

                Log.d(TAG, "Playing song: ${song.title}, Audio URL: ${song.audio}")

                val mediaItem = MediaItem.Builder()
                    .setUri(song.audio)
                    .setTag(song.id)
                    .build()

                Log.d(TAG, "Setting media item")
                exoPlayer.setMediaItem(mediaItem)
                Log.d(TAG, "Preparing player")
                exoPlayer.prepare()

                val climaxStart = (song.climaxStart * 1000).toLong()
                val climaxEnd = min(((song.climaxStart + 40) * 1000).toLong(), exoPlayer.duration)
                val climaxDuration = climaxEnd - climaxStart

                Log.d(TAG, "Climax timing - Start: $climaxStart, End: $climaxEnd, Duration: $climaxDuration")

                playerScope?.launch {
                    delay(100) // Wait for player to be ready
                    Log.d(TAG, "Seeking to climax point: $climaxStart")
                    exoPlayer.seekTo(climaxStart)
                    Log.d(TAG, "Starting playback")
                    exoPlayer.play()
                    _isPlaying.value = true

                    // Check if player is actually playing
                    Log.d(TAG, "Is playing after play(): ${exoPlayer.isPlaying}")
                    Log.d(TAG, "Player state after play(): ${exoPlayer.playbackState}")

                    scheduleNextSong(climaxDuration)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing from climax point", e)
                e.printStackTrace()
                _error.value = "Error playing song"
            }
        } ?: run {
            Log.e(TAG, "Player is null")
        }
    }

    private fun scheduleNextSong(delay: Long) {
        climaxEndJob = playerScope?.launch {
            delay(delay)
            moveToNextSong()
        }
    }

    private fun moveToNextSong() {
        val currentIdx = _currentIndex.value
        val nextIndex = if (currentIdx >= _listSong.value.size - 1) 0 else currentIdx + 1

        playerScope?.launch {
            _currentIndex.value = nextIndex
            delay(100)
            _listSong.value.getOrNull(nextIndex)?.let { song ->
                playFromClimaxPoint(song)
            }
        }
    }

    fun handleEndOfList() {
        playerScope?.launch {
            if (_listSong.value.isNotEmpty()) {
                _currentIndex.value = 0
                playFromClimaxPoint(_listSong.value[0])
            }
        }
    }

    fun onPageChanged(newIndex: Int) {
        if (_listSong.value.isEmpty()) return

        if (newIndex != _currentIndex.value && newIndex in _listSong.value.indices) {
            playerScope?.launch {
                player?.pause()
                cancelClimaxEndJob()

                _currentIndex.value = newIndex
                _listSong.value.getOrNull(newIndex)?.let { song ->
                    player?.stop()
                    player?.clearMediaItems()
                    _currentSongId.value = song.id
                    playFromClimaxPoint(song)
                }
            }
        }
    }

    fun playPause() {
        player?.let { exoPlayer ->
            if (_isPlaying.value) {
                pause()
                cancelClimaxEndJob()
            } else {
                play()
                // Recalculate remaining climax time and schedule next song
                val currentSong = _listSong.value.getOrNull(_currentIndex.value) ?: return
                val currentPos = exoPlayer.currentPosition
                val climaxEnd = min(((currentSong.climaxStart + 40) * 1000).toLong(), exoPlayer.duration)
                val remainingTime = climaxEnd - currentPos
                if (remainingTime > 0) {
                    scheduleNextSong(remainingTime)
                } else {
                    moveToNextSong()
                }
            }
        }
    }

    fun play() {
        player?.let { exoPlayer ->
            exoPlayer.play()
            _isPlaying.value = true

            val currentSong = _listSong.value.getOrNull(_currentIndex.value) ?: return
            val currentPos = exoPlayer.currentPosition
            val climaxEnd = min(((currentSong.climaxStart + 40) * 1000).toLong(), exoPlayer.duration)
            val remainingTime = climaxEnd - currentPos
            if (remainingTime > 0) {
                scheduleNextSong(remainingTime)
            }
        }
    }

    fun pause() {
        player?.pause()
        _isPlaying.value = false
        cancelClimaxEndJob()
    }

    fun onScreenPause() {
        pause()
    }

    fun onScreenResume() {
        if (_isPlaying.value) {
            play()
        }
    }

    private fun cancelClimaxEndJob() {
        climaxEndJob?.cancel()
        climaxEndJob = null
    }

    fun resetPlayer() {
        Log.d(TAG, "Resetting player")
        player?.let { exoPlayer ->
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                _currentSongId.value = -1
                _isPlaying.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting player", e)
                e.printStackTrace()
            }
        }
    }



    fun release() {
        volumeContentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
        }
        volumeContentObserver = null

        cancelClimaxEndJob()

        try {
            playerListener?.let { listener ->
                player?.removeListener(listener)
            }
            playerListener = null

            playerScope?.cancel()
            playerScope = null

            player?.release()
            player = null

            _isPlaying.value = false
            _currentPosition.value = 0L
            _duration.value = 0L
            _currentSongId.value = -1
            _currentIndex.value = 0
            _listSong.value = emptyList()
            _isLoading.value = false
            _error.value = null

            Log.d(TAG, "Player resources released successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player", e)
            e.printStackTrace()
        }
    }

    fun reinitializePlayer() {
        Log.d(TAG, "Reinitializing player")
        release()
        initialize()
    }

    companion object {
        private const val TAG = "SwipePlayerManager"
        private const val UPDATE_INTERVAL = 500L
    }
}