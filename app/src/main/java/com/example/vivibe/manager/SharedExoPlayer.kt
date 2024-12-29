package com.example.vivibe.manager

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.vivibe.model.PlaySong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object SharedExoPlayer {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _currentSong = MutableStateFlow<PlaySong?>(null)
    val currentSong: StateFlow<PlaySong?> = _currentSong

    private var playerScope: CoroutineScope? = null

    fun initialize(context: Context) {
        if(player == null) {
            player = ExoPlayer.Builder(context).build()
            mediaSession = MediaSession.Builder(context, player!!).build()

            playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            startPositionTracking()
        }
    }

    fun reset() {
        playerScope?.cancel()
        playerScope = null
        mediaSession?.release()
        player?.release()
        player = null
        mediaSession = null

        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _currentSong.value = null
    }

    private fun startPositionTracking() {
        playerScope?.launch {
            while(true) {
                player?.let { exoPlayer ->
                    _currentPosition.value = exoPlayer.currentPosition
                    _duration.value = exoPlayer.duration
                    _isPlaying.value = exoPlayer.isPlaying

                    if (exoPlayer.playerError != null ||
                        (exoPlayer.currentPosition >= exoPlayer.duration && exoPlayer.duration > 0)
                    ) {
                        _isPlaying.value = false
                    }
                }

                delay(500L)
            }
        }
    }

    fun getPlayer(): ExoPlayer? = player

    fun prepareSong(song: PlaySong) {
        player?.let { exoPlayer ->
            _currentSong.value = song
            val mediaItem = MediaItem.fromUri(song.audio)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            _isPlaying.value = true
        }
    }

    fun playPause() {
        player?.let { exoPlayer ->
            if(_isPlaying.value) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }

            _isPlaying.value = !_isPlaying.value
        }
    }

    fun play() {
        player?.play()
        _isPlaying.value = true
    }

    fun pause() {
        player?.pause()
        _isPlaying.value = false
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun seekToProgress(progress: Float) {
        player?.let { exoPlayer ->
            val seekPosition = (progress * _duration.value).toLong()
            exoPlayer.seekTo(seekPosition)
        }
    }

    fun handlePreviousTrack() {
        TODO("Not yet implemented")
    }

    fun handleNextTrack() {
        TODO("Not yet implemented")
    }

    fun handleShuffle() {
        TODO("Not yet implemented")
    }

    fun handleLike() {
        TODO("Not yet implemented")
    }

    fun handleRepeat() {
        TODO("Not yet implemented")
    }
}