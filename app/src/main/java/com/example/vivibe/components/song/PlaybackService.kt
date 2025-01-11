package com.example.vivibe.components.song

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.vivibe.MainActivity
import com.example.vivibe.R
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlaybackService: MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val binder = LocalBinder()


    inner class LocalBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    companion object {
        private const val TAG = "PlaybackService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ViVibe"

        private const val ACTION_PREV = "com.example.vivibe.PREV"
        private const val ACTION_PLAY_PAUSE = "com.example.vivibe.PLAY_PAUSE"
        private const val ACTION_NEXT = "com.example.vivibe.NEXT"
        private const val ACTION_SHUFFLE = "com.example.vivibe.SHUFFLE"
        private const val ACTION_REPEAT = "com.example.vivibe.REPEAT"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        initializeService()
    }

    private fun initializeService() {
        createNotificationChannel()
        observeUserPremiumStatus()
        setupExoPlayer()
    }

    private fun observeUserPremiumStatus() {
        val userManager = UserManager.getInstance(this)
        serviceScope.launch {
            userManager.userState.collect { user ->
                if (user?.premium != 1) {
                    stopSelf()
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupExoPlayer() {
        val exoPlayer = SharedExoPlayer.getInstance(this)

        mediaSession = MediaSession.Builder(this, exoPlayer.getPlayer()!!)
            .setId("ViVibeSession")
            .setCallback(createMediaSessionCallback())
            .build()

        exoPlayer.getPlayer()?.addListener(createPlayerListener())
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSessionCallback() = object : MediaSession.Callback {
        @Deprecated("Deprecated in Java")
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            val exoPlayer = SharedExoPlayer.getInstance(this@PlaybackService)
            return when(playerCommand) {
                Player.COMMAND_SEEK_TO_PREVIOUS -> {
                    exoPlayer.handlePreviousTrack()
                    playerCommand
                }
                Player.COMMAND_PLAY_PAUSE -> {
                    exoPlayer.playPause()
                    playerCommand
                }
                Player.COMMAND_SEEK_TO_NEXT -> {
                    exoPlayer.handleNextTrack()
                    playerCommand
                }
                Player.COMMAND_SET_SHUFFLE_MODE -> {
                    exoPlayer.handleShuffle()
                    playerCommand
                }
                Player.COMMAND_SET_REPEAT_MODE -> {
                    exoPlayer.handleRepeat()
                    playerCommand
                }
                else -> super.onPlayerCommandRequest(session, controller, playerCommand)
            }.also { updateNotification() }
        }
    }

    private fun createPlayerListener() = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if(playbackState == Player.STATE_ENDED) {
                SharedExoPlayer.getInstance(this@PlaybackService).handleNextTrack()
            }
            updateNotification()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updateNotification()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updateNotification()
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("RestrictedApi")
    @OptIn(UnstableApi::class)
    private fun updateNotification() {
        val exoPlayer = SharedExoPlayer.getInstance(this)
        val currentSongId = exoPlayer.currentSongId.value
        if (currentSongId == -1) return

        val currentSong = exoPlayer.listSong.value.find { it.id == currentSongId } ?: return
        val isPlaying = exoPlayer.isPlaying.value
        val isShuffleEnabled = exoPlayer.isShuffleEnabled.value
        val repeatMode = exoPlayer.isRepeatEnabled.value

        val notificationIntents = createNotificationIntents()

        val imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .build()

        imageLoader.enqueue(
            ImageRequest.Builder(this)
                .data(currentSong.thumbnailUrl)
                .target { drawable ->
                    val bitmap = when (drawable) {
                        is BitmapDrawable -> drawable.bitmap
                        else -> drawable.toBitmap()
                    }

                    val notification = NotificationCompat.Builder(this@PlaybackService, CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.ic_music_note)
                        .setContentTitle(currentSong.title)
                        .setContentText(currentSong.artist.name)
                        .setLargeIcon(bitmap)
                        .setContentIntent(notificationIntents.contentIntent)
                        .setShowWhen(false)
                        // Controls cho compact view
                        .addAction(
                            R.drawable.ic_skip_previous,
                            "Previous",
                            notificationIntents.prevIntent
                        ) // index: 0
                        .addAction(
                            if (isPlaying) R.drawable.ic_pause_outline
                            else R.drawable.ic_play_outline,
                            if (isPlaying) "Pause" else "Play",
                            notificationIntents.playPauseIntent
                        ) // index: 1
                        .addAction(
                            R.drawable.ic_skip_next,
                            "Next",
                            notificationIntents.nextIntent
                        ) // index: 2
                        // Controls thêm cho expanded view
                        .addAction(
                            if (isShuffleEnabled) R.drawable.ic_shuffle else R.drawable.ic_shuffle_off,
                            "Shuffle",
                            notificationIntents.shuffleIntent
                        ) // index: 3
                        .addAction(
                            when (repeatMode) {
                                0 -> R.drawable.ic_repeat_off
                                1 -> R.drawable.ic_repeat
                                else -> R.drawable.ic_repeat_one
                            },
                            "Repeat",
                            notificationIntents.repeatIntent
                        ) // index: 4
                        .setStyle(
                            MediaStyle(mediaSession!!)
                                .setShowActionsInCompactView(0, 1, 2, 3, 4)
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .setOngoing(isPlaying)
                        .build()

                    if (isPlaying) {
                        startForeground(NOTIFICATION_ID, notification)
                    } else {
                        stopForeground(STOP_FOREGROUND_DETACH)
                        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                }
                .build()
        )
    }

    private fun handleNotificationState(notification: Notification, isPlaying: Boolean) {
        if (isPlaying) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(false)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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

    private fun createActionIntent(action: String, requestCode: Int, flags: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(this, PlaybackService::class.java).setAction(action),
            flags
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val exoPlayer = SharedExoPlayer.getInstance(this)
        when(intent?.action) {
            ACTION_PREV -> exoPlayer.handlePreviousTrack()
            ACTION_PLAY_PAUSE -> exoPlayer.playPause()
            ACTION_NEXT -> exoPlayer.handleNextTrack()
            ACTION_SHUFFLE -> exoPlayer.handleShuffle()
            ACTION_REPEAT -> exoPlayer.handleRepeat()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.launch {
            try {
                SharedExoPlayer.getInstance(this@PlaybackService).reset()
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting ExoPlayer", e)
            }
        }
        super.onDestroy()
    }
}