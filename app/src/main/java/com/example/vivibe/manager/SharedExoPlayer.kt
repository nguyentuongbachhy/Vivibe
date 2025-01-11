package com.example.vivibe.manager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
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
    private var userManager: UserManager? = null
    private var dbHelper: DatabaseHelper? = null
    private var playerScope: CoroutineScope? = null
    private var playerListener: Player.Listener? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isLikedCurrentPlaySong = MutableStateFlow(false)
    val isLikedCurrentPlaySong: StateFlow<Boolean> = _isLikedCurrentPlaySong

    private val _isDislikedCurrentPlaySong = MutableStateFlow(false)
    val isDislikedCurrentPlaySong: StateFlow<Boolean> = _isDislikedCurrentPlaySong

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
    }

    private fun initialize() {
        playerListener = createPlayerListener()
        player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            shuffleModeEnabled = false
        }

        player?.addListener(playerListener!!)

        mediaSession = MediaSession.Builder(context, player!!).build()
        userManager = UserManager.getInstance(context)
        dbHelper = DatabaseHelper(context)

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

    fun updateSongs(list: List<PlaySong>) {
        if (list.isEmpty()) return
        _listSong.value = list
        prepareSong(list[0])
    }

    fun prepareSong(song: PlaySong) {
        player?.let { exoPlayer ->
            try {
                _currentSongId.value = song.id
                _duration.value = (song.duration * 1000).toLong()
                val mediaItem = MediaItem.fromUri(song.audio)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
                _isPlaying.value = true

                updateLikeDislikeStatus(song.id)
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

        return when {
            _isShuffleEnabled.value -> getRandomSong(songs, currentIndex)
            currentIndex < songs.size - 1 -> songs[currentIndex + 1]
            _isRepeatEnabled.value == 1 -> songs[0]
            else -> null
        }
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
            _isShuffleEnabled.value = !_isShuffleEnabled.value
            exoPlayer.shuffleModeEnabled = _isShuffleEnabled.value
        }
    }

    fun handleRepeat() {
        player?.let { exoPlayer ->
            _isRepeatEnabled.value = (_isRepeatEnabled.value + 1) % 3

            exoPlayer.repeatMode = when(_isRepeatEnabled.value) {
                0 -> Player.REPEAT_MODE_OFF
                1 -> Player.REPEAT_MODE_ALL
                2 -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
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
        songToPlay?.let { prepareSong(it) }
    }

    private fun updateLikeDislikeStatus(songId: Int) {
        val currentUser = userManager?.getGoogleId() ?: return
        val currentDb = dbHelper ?: return

        _isLikedCurrentPlaySong.value = currentDb.isLikedSong(currentUser, songId)
        _isDislikedCurrentPlaySong.value = currentDb.isDislikedSong(currentUser, songId)
    }

    fun handleLike() {
        val songId = _currentSongId.value
        val currentUser = userManager?.getGoogleId() ?: return
        val currentDb = dbHelper ?: return

        playerScope?.launch {
            try {
                val newLikeState = !_isLikedCurrentPlaySong.value
                _isLikedCurrentPlaySong.value = newLikeState

                currentDb.updateLikedStatus(currentUser, songId, newLikeState)

                if (newLikeState && _isDislikedCurrentPlaySong.value) {
                    _isDislikedCurrentPlaySong.value = false
                    currentDb.updateDislikedStatus(currentUser, songId, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling like", e)
                _isLikedCurrentPlaySong.value = !_isLikedCurrentPlaySong.value
            }
        }
    }

    fun handleDislike() {
        val songId = _currentSongId.value
        val currentUser = userManager?.getGoogleId() ?: return
        val currentDb = dbHelper ?: return

        playerScope?.launch {
            try {
                val newDislikeState = !_isDislikedCurrentPlaySong.value
                _isDislikedCurrentPlaySong.value = newDislikeState

                currentDb.updateDislikedStatus(currentUser, songId, newDislikeState)

                if (newDislikeState && _isLikedCurrentPlaySong.value) {
                    _isLikedCurrentPlaySong.value = false
                    currentDb.updateLikedStatus(currentUser, songId, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling dislike", e)
                _isDislikedCurrentPlaySong.value = !_isDislikedCurrentPlaySong.value
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
        _isLikedCurrentPlaySong.value = false
        _isDislikedCurrentPlaySong.value = false
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

