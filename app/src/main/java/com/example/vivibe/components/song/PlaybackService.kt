package com.example.vivibe.components.song

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.collectAsState
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vivibe.MainActivity
import com.example.vivibe.R
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.PlaySong
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main)
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val exoPlayer by lazy { SharedExoPlayer.getInstance(this) }

    companion object {
        private const val TAG = "PlaybackService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ViVibe"
        private const val CHANNEL_NAME = "Playback"
        private const val CHANNEL_DESCRIPTION = "Media playback controls"

        // Actions
        private const val ACTION_PREFIX = "com.example.vivibe"
        private const val ACTION_PREV = "$ACTION_PREFIX.PREV"
        private const val ACTION_PLAY_PAUSE = "$ACTION_PREFIX.PLAY_PAUSE"
        private const val ACTION_NEXT = "$ACTION_PREFIX.NEXT"
        private const val ACTION_SHUFFLE = "$ACTION_PREFIX.SHUFFLE"
        private const val ACTION_REPEAT = "$ACTION_PREFIX.REPEAT"
    }

    override fun onCreate() {
        super.onCreate()
        initializeService()
        startProgressUpdate()
    }

    private fun initializeService() {
        createNotificationChannel()
        observeUserPremiumStatus()
        setupMediaSession()
    }

    private fun observeUserPremiumStatus() {
        serviceScope.launch {
            UserManager.getInstance(this@PlaybackService)
                .userState
                .distinctUntilChangedBy { it?.premium }
                .collect { user ->
                    if (user?.premium != 1) stopSelf()
                }
        }
    }

    private fun setupMediaSession() {
        mediaSessionCompat = MediaSessionCompat(this, "ViVibeSession").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
            )

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    exoPlayer.getPlayer()?.play()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                override fun onPause() {
                    exoPlayer.getPlayer()?.pause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }

                override fun onSkipToNext() = exoPlayer.handleNextTrack()
                override fun onSkipToPrevious() = exoPlayer.handlePreviousTrack()

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        "SHUFFLE" -> {
                            exoPlayer.handleShuffle()
                            updatePlaybackState(if (exoPlayer.isPlaying.value)
                                PlaybackStateCompat.STATE_PLAYING
                            else PlaybackStateCompat.STATE_PAUSED)
                        }
                        "REPEAT" -> {
                            exoPlayer.handleRepeat()
                            updatePlaybackState(if (exoPlayer.isPlaying.value)
                                PlaybackStateCompat.STATE_PLAYING
                            else PlaybackStateCompat.STATE_PAUSED)
                        }
                    }
                }
                override fun onSeekTo(pos: Long) {
                    exoPlayer.seekTo((pos / 1000).toInt())
                    updatePlaybackState(
                        if (exoPlayer.isPlaying.value) PlaybackStateCompat.STATE_PLAYING
                        else PlaybackStateCompat.STATE_PAUSED
                    )
                }

            })

            // Set initial state
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            isActive = true
        }

        // Khởi tạo MediaSession như cũ
        mediaSession = MediaSession.Builder(this, exoPlayer.getPlayer()!!)
            .setId("ViVibeSession")
            .setCallback(createMediaSessionCallback())
            .build()
            .also {
                exoPlayer.getPlayer()?.addListener(createPlayerListener())
            }
    }

    private fun updatePlaybackState(state: Int) {
        val currentPosition = exoPlayer.currentPosition.value
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
                PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                state,
                currentPosition,
                1f
            )
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "SHUFFLE",
                    "Shuffle",
                    if (exoPlayer.isShuffleEnabled.value) R.drawable.ic_shuffle else R.drawable.ic_shuffle_off
                ).build()
            )
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "REPEAT",
                    "Repeat",
                    when(exoPlayer.isRepeatEnabled.value) {
                        0 -> R.drawable.ic_repeat_off
                        1 -> R.drawable.ic_repeat
                        else -> R.drawable.ic_repeat_one
                    }
                ).build()
            )
            .build()

        val duration = exoPlayer.duration.value
        val metadata = MediaMetadataCompat.Builder()
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)  // Thêm duration
            .build()

        mediaSessionCompat?.apply {
            setPlaybackState(playbackState)
            setMetadata(metadata)  // Set metadata
        }
    }




    private fun createMediaSessionCallback() = object : MediaSession.Callback {
        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            updateNotification()
        }
    }

    private fun createPlayerListener() = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY, Player.STATE_BUFFERING -> {
                    updateNotification()
                }

                Player.STATE_ENDED -> {
                    if (exoPlayer.isRepeatEnabled.value == 2) {
                        exoPlayer.getPlayer()?.seekTo(0)
                        exoPlayer.getPlayer()?.play()
                    } else {
                        exoPlayer.handleNextTrack()
                    }
                }

                Player.STATE_IDLE -> {
                    TODO()
                }
            }
            updateNotification()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updateNotification()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updateNotification()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePlaybackState(
                if (exoPlayer.isPlaying.value) PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH  // Thay đổi từ LOW thành HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                // Bỏ 2 dòng này đi
                // enableLights(false)
                // enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Thêm một coroutine để cập nhật progress định kỳ
    private fun startProgressUpdate() {
        serviceScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying.value) {
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
                delay(1000) // Cập nhật mỗi giây
            }
        }
    }

    private fun updateNotification() {
        serviceScope.launch {
            try {
                val currentSongId = exoPlayer.currentSongId.value
                if (currentSongId == -1) return@launch

                val currentSong = exoPlayer.listSong.value.find { it.id == currentSongId }
                    ?: return@launch

                val notificationData = NotificationData(
                    song = currentSong,
                    isPlaying = exoPlayer.isPlaying.value,
                    isShuffleEnabled = exoPlayer.isShuffleEnabled.value,
                    repeatMode = when(exoPlayer.isRepeatEnabled.value) {
                        0 -> Player.REPEAT_MODE_OFF
                        1 -> Player.REPEAT_MODE_ALL
                        else -> Player.REPEAT_MODE_ONE
                    },
                    thumbnail = loadThumbnail(currentSong.thumbnailUrl)
                )

                val notification = createNotification(notificationData)
                handleNotificationState(notification, notificationData.isPlaying)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification", e)
            }
        }
    }

    private suspend fun loadThumbnail(url: String): Bitmap = withContext(Dispatchers.IO) {
        Glide.with(applicationContext)
            .asBitmap()
            .load(url)
            .apply(RequestOptions().centerCrop())
            .submit()
            .get()
    }

    private data class NotificationData(
        val song: PlaySong,
        val isPlaying: Boolean,
        val isShuffleEnabled: Boolean,
        val repeatMode: Int,
        val thumbnail: Bitmap
    )

    @OptIn(UnstableApi::class)
    private fun createNotification(data: NotificationData): Notification {
        updatePlaybackState(
            if (data.isPlaying) PlaybackStateCompat.STATE_PLAYING
            else PlaybackStateCompat.STATE_PAUSED
        )

        val intents = createNotificationIntents()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(data.song.title)
            .setContentText(data.song.artist.name)
            .setLargeIcon(data.thumbnail)
            .setContentIntent(intents.contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSessionCompat?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        builder.setStyle(mediaStyle)

        return builder.build()
    }

    private fun handleNotificationState(notification: Notification, isPlaying: Boolean) {
        if (isPlaying) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(false)
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private data class NotificationIntents(
        val contentIntent: PendingIntent,
        val prevIntent: PendingIntent,
        val playPauseIntent: PendingIntent,
        val nextIntent: PendingIntent,
        val shuffleIntent: PendingIntent,
        val repeatIntent: PendingIntent
    )

    private fun createNotificationIntents(): NotificationIntents {
        val contentIntent = createContentIntent()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return NotificationIntents(
            contentIntent = contentIntent,
            prevIntent = createActionIntent(ACTION_PREV, 1, flags),
            playPauseIntent = createActionIntent(ACTION_PLAY_PAUSE, 2, flags),
            nextIntent = createActionIntent(ACTION_NEXT, 3, flags),
            shuffleIntent = createActionIntent(ACTION_SHUFFLE, 4, flags),
            repeatIntent = createActionIntent(ACTION_REPEAT, 5, flags)
        )
    }

    private fun createContentIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun createActionIntent(action: String, requestCode: Int, flags: Int): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(action).setPackage(packageName),
            flags
        )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleIntent(intent)
        return START_STICKY
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_PREV -> {
                exoPlayer.handlePreviousTrack()
                updateNotification()
            }
            ACTION_PLAY_PAUSE -> {
                exoPlayer.playPause()
                updateNotification()
            }
            ACTION_NEXT -> {
                exoPlayer.handleNextTrack()
                updateNotification()
            }
            ACTION_SHUFFLE -> {
                exoPlayer.handleShuffle()
                updateNotification()
            }
            ACTION_REPEAT -> {
                exoPlayer.handleRepeat()
                updateNotification()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return LocalBinder()
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSessionCompat?.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.launch {
            try {
                exoPlayer.reset()
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting ExoPlayer", e)
            } finally {
                serviceJob.cancel()
            }
        }
        super.onDestroy()
    }
}