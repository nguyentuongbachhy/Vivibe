package com.example.vivibe.manager

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.vivibe.api.user.UserClient
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.model.PlaySong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class SharedExoPlayer private constructor(private val context: Context) {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var volumeContentObserver: ContentObserver? = null
    private var userManager: UserManager? = null
    private var dbHelper: DatabaseHelper? = null
    private var userClient: UserClient? = null
    private var playerScope: CoroutineScope? = null
    private var playerListener: Player.Listener? = null

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLikedCurrentSong = MutableStateFlow(false)
    val isLikedCurrentSong: StateFlow<Boolean> = _isLikedCurrentSong

    private val _isUpdatingLike = MutableStateFlow<Boolean>(false)
    val isUpdatingLike: StateFlow<Boolean> = _isUpdatingLike

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _currentSongId = MutableStateFlow(-1)
    val currentSongId: StateFlow<Int> = _currentSongId

    private val _listSong = MutableStateFlow<List<PlaySong>>(emptyList())
    val listSong: StateFlow<List<PlaySong>> = _listSong

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _isRepeatEnabled = MutableStateFlow(0)
    val isRepeatEnabled: StateFlow<Int> = _isRepeatEnabled

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
        playerListener = createPlayerListener()
        player = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_OFF
                shuffleModeEnabled = false
            }

        updatePlayerVolume()

        player?.addListener(playerListener!!)

        mediaSession = MediaSession.Builder(context, player!!).build()
        userManager = UserManager.getInstance(context)
        dbHelper = DatabaseHelper(context)
        userClient = UserClient(context, userManager?.getToken())

        playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        startPositionTracking()
    }

    private fun createPlayerListener(): Player.Listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    Log.d(TAG, "Player state: IDLE")
                }
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "Player state: BUFFERING")
                }
                Player.STATE_READY -> {
                    Log.d(TAG, "Player state: READY")
                    handleTrackReady()
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "Player state: ENDED")
                    handleTrackEnd()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    private fun handleTrackEnd() {
        if (_isRepeatEnabled.value == 2) {
            player?.seekTo(0)
            player?.play()
        } else {
            handleNextTrack()
        }
    }

    private fun handleTrackReady() {
        _duration.value = player?.duration ?: 0
    }

    private fun handlePlaybackError() {
        Log.e(TAG, "Playback error occurred")
        resetPlayback()
    }

    private fun startPositionTracking() {
        playerScope?.launch {
            while(true) {
                player?.let { exoPlayer ->
                    _currentPosition.value = exoPlayer.currentPosition
                    _isPlaying.value = exoPlayer.isPlaying

                    if (shouldHandleNextTrack(exoPlayer)) {
                        handleNextTrack()
                    }
                }
                delay(UPDATE_INTERVAL)
            }
        }
    }

    private fun shouldHandleNextTrack(exoPlayer: ExoPlayer): Boolean {
        return exoPlayer.playerError != null ||
                (exoPlayer.currentPosition >= exoPlayer.duration && exoPlayer.duration > 0)
    }

    fun updateSongs(list: List<PlaySong>, isOffline: Boolean = false) {
        if (list.isEmpty()) return
        _listSong.value = list
        prepareSong(list[0], isOffline)
    }

    fun prepareSong(song: PlaySong, isOffline: Boolean = false) {
        player?.let { exoPlayer ->
            try {
                _currentSongId.value = song.id
                _duration.value = (song.duration * 1000).toLong()
                _isOfflineMode.value = isOffline

                val mediaItem = if (isOffline) {
                    // Nếu là offline mode, lấy audio path từ local storage
                    val downloadedSong = dbHelper?.getDownloadedSong(
                        userManager?.getGoogleId() ?: return,
                        song.id
                    )
                    if (downloadedSong != null) {
                        MediaItem.fromUri("file://${downloadedSong.audioPath}")
                    } else {
                        Log.e(TAG, "Downloaded song not found in database")
                        return
                    }
                } else {
                    // Online mode - sử dụng URL
                    MediaItem.fromUri(song.audio)
                }

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                _isPlaying.value = true

                // Chỉ fetch like status khi online
                if (!isOffline) {
                    fetchLikeStatus(song.id)
                } else { }
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing song", e)
            }
        }
    }

    fun playPause() {
        player?.let { exoPlayer ->
            if (_isPlaying.value) exoPlayer.pause() else exoPlayer.play()
            _isPlaying.value = !_isPlaying.value
        }
    }

    fun seekToProgress(progress: Float) {
        player?.let { exoPlayer ->
            val seekPosition = (progress * _duration.value).toLong()
            exoPlayer.seekTo(seekPosition)
        }
    }

    fun seekTo(value: Int) {
        player?.let { exoPlayer ->
            val seekPosition = getDurationInMillis(value)
            exoPlayer.seekTo(seekPosition)
        }
    }

    fun handleNextTrack() {
        val songs = _listSong.value
        if (songs.isEmpty()) return

        val nextSong = getNextSong(songs)
        nextSong?.let { prepareSong(it) }
    }

    private fun getNextSong(songs: List<PlaySong>): PlaySong? {
        if (songs.size == 1) {
            return if (_isRepeatEnabled.value != 0) songs[0] else null
        }

        val currentIndex = songs.indexOfFirst { it.id == _currentSongId.value }

        if(_isShuffleEnabled.value) return getRandomSong(songs, currentIndex)

        if (_isRepeatEnabled.value == 0) {
            return if (currentIndex < songs.size - 1) {
                songs[currentIndex + 1]
            } else null
        }

        if(_isRepeatEnabled.value == 1) {
            return if(currentIndex < songs.size - 1) songs[currentIndex + 1] else songs[0]
        }

        return songs[currentIndex]
    }

    private fun getRandomSong(songs: List<PlaySong>, excludeIndex: Int): PlaySong {
        var randomIndex: Int
        do {
            randomIndex = Random.nextInt(songs.size)
        } while (randomIndex == excludeIndex)
        return songs[randomIndex]
    }

    fun handlePreviousTrack() {
        if (_currentPosition.value >= SEEK_THRESHOLD) {
            player?.seekTo(0L)
            _currentPosition.value = 0L
        } else {
            val songs = _listSong.value
            val currentIndex = songs.indexOfFirst { it.id == _currentSongId.value }
            if (currentIndex > 0) {
                prepareSong(songs[currentIndex - 1])
            }
        }
    }

    fun handleShuffle() {
        player?.let { exoPlayer ->
            if(_isShuffleEnabled.value) {
                _isShuffleEnabled.value = false
                exoPlayer.shuffleModeEnabled = false
            } else {
                _isShuffleEnabled.value = true
                exoPlayer.shuffleModeEnabled = true

            }
        }
    }

    fun handleRepeat() {
        player?.let { exoPlayer ->
            if(_isRepeatEnabled.value == 2) {
                   _isRepeatEnabled.value = 0
                exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            }
            else if (_isRepeatEnabled.value == 1) {
                _isRepeatEnabled.value = 2
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            } else {
                _isRepeatEnabled.value = 1
                exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            }
        }
    }

    fun play() {
        player?.let { exoPlayer ->
            exoPlayer.play()
            _isPlaying.value = true
        }
    }

    fun pause() {
        player?.let { exoPlayer ->
            exoPlayer.pause()
            _isPlaying.value = false
        }
    }


    fun handlePlayOneInListSong(songId: Int) {
        val songs = _listSong.value
        val songToPlay = songs.find { it.id == songId }
        songToPlay?.let { prepareSong(it, _isOfflineMode.value) }
    }

    private fun fetchLikeStatus(songId: Int) {
        val userId = userManager?.getId() ?: return

        playerScope?.launch {
            try {
                _isUpdatingLike.value = true

                _isLikedCurrentSong.value = userClient?.getLikeStatus(userId, songId) ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error handling like", e)
                _isUpdatingLike.value = false
            } finally {
                _isUpdatingLike.value = false
                delay(3000)
            }
        }
    }

    fun handleLike() {
        val songId = _currentSongId.value
        val userId = userManager?.getId() ?: return

        playerScope?.launch {
            try {
                _isUpdatingLike.value = true

                _isLikedCurrentSong.value = userClient?.toggleLike(userId, songId) ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error handling like", e)
                _isUpdatingLike.value = false
            } finally {
                _isUpdatingLike.value = false
                delay(3000)
            }
        }
    }


    private fun resetPlayback() {
        player?.stop()
        player?.clearMediaItems()
        _currentPosition.value = 0
        _duration.value = 0
        _isPlaying.value = false
    }

    private fun cleanup() {
        playerListener?.let { listener ->
            player?.removeListener(listener)
        }
        playerListener = null

        playerScope?.cancel()
        playerScope = null

        mediaSession?.release()
        mediaSession = null

        player?.release()
        player = null

        dbHelper = null
        userManager = null
        userClient = null
    }

    fun reset() {
        cleanup()
        resetState()
    }

    private fun resetState() {
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _currentSongId.value = -1
        _listSong.value = emptyList()
        _isShuffleEnabled.value = false
        _isRepeatEnabled.value = 0
        _isLikedCurrentSong.value = false
    }

    private fun getDurationInMillis(durationInSeconds: Int): Long {
        return durationInSeconds * 1000L
    }

    private fun isCurrentSong(songId: Int): Boolean {
        return _currentSongId.value == songId
    }

    fun getPlayer() = player

    companion object {
        private const val TAG = "SharedExoPlayer"
        private const val SEEK_THRESHOLD = 10000L
        private const val UPDATE_INTERVAL = 500L
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: SharedExoPlayer? = null

        fun getInstance(context: Context): SharedExoPlayer {
            return instance ?: synchronized(this) {
                instance ?: SharedExoPlayer(context).also { instance = it }
            }
        }
    }
}

