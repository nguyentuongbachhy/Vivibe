package com.example.vivibe.components.song

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.vivibe.MainActivity
import com.example.vivibe.R
import com.example.vivibe.manager.GlobalStateManager
import com.example.vivibe.manager.SharedExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlaybackService: MediaSessionService() {
    private var mediaSession: MediaSession? = null

    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ViVibe"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            GlobalStateManager.userState.collect { user ->
                if(user.premium != 1) {
                    stopSelf()
                    return@collect
                }
            }
        }
        SharedExoPlayer.initialize(this)

        SharedExoPlayer.getPlayer()?.let { player ->
            mediaSession = MediaSession.Builder(this, player)
                .setId("ViVibeSession")
                .setCallback(object: MediaSession.Callback {
                    @Deprecated("Deprecated in Java", ReplaceWith(
                        "super.onPlayerCommandRequest(session, controller, playerCommand)",
                        "androidx.media3.session.MediaSession.Callback"
                    )
                    )
                    override fun onPlayerCommandRequest(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        playerCommand: Int
                    ): Int {
                        when (playerCommand) {
                            Player.COMMAND_SET_SHUFFLE_MODE -> {
                                SharedExoPlayer.handleShuffle()
                            }
                            Player.COMMAND_SEEK_TO_PREVIOUS -> {
                                SharedExoPlayer.handlePreviousTrack()
                            }
                            Player.COMMAND_PLAY_PAUSE -> {
                                if (player.isPlaying) {
                                    SharedExoPlayer.pause()
                                } else {
                                    SharedExoPlayer.play()
                                }
                            }
                            Player.COMMAND_SEEK_TO_NEXT -> {
                                SharedExoPlayer.handleNextTrack()
                            }
                            Player.COMMAND_SET_REPEAT_MODE -> {
                                SharedExoPlayer.handleRepeat()
                            }
                            else -> super.onPlayerCommandRequest(session, controller, playerCommand)
                        }
                        updateNotification()
                        return playerCommand
                    }

                    override fun onPlayerInteractionFinished(
                        session: MediaSession,
                        controllerInfo: MediaSession.ControllerInfo,
                        playerCommands: Player.Commands
                    ) {
                        super.onPlayerInteractionFinished(session, controllerInfo, playerCommands)
                        updateNotification()
                    }
                }
                ).build()

            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateNotification()
                }
            })
        }

        createNotificationChannel()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            SharedExoPlayer.currentSong.collect {
                updateNotification()
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            SharedExoPlayer.isPlaying.collect {
                updateNotification()
            }
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
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("RestrictedApi")
    @OptIn(UnstableApi::class)
    private fun updateNotification() {
        val player = SharedExoPlayer.getPlayer() ?: return
        val currentSong = SharedExoPlayer.currentSong.value ?: return
        val isPlaying = player.isPlaying

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getBroadcast(
            this, 1, Intent("ACTION_PREV"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = PendingIntent.getBroadcast(
            this, 2, Intent("ACTION_PLAY"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getBroadcast(
            this, 3, Intent("ACTION_NEXT"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .build()

        imageLoader.enqueue(
            ImageRequest.Builder(this)
                .data(currentSong.thumbnailUrl)
                .target {drawable ->
                    val bitmap = when(drawable) {
                        is BitmapDrawable -> drawable.bitmap
                        else -> drawable.toBitmap()
                    }

                    val notification = NotificationCompat.Builder(this@PlaybackService, CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.drawable.ic_music_note)
                        .setContentTitle(currentSong.title)
                        .setContentText(currentSong.artist.name)
                        .setLargeIcon(bitmap)
                        .setContentIntent(contentPendingIntent)
                        .addAction(R.drawable.ic_skip_previous, "Previous", prevIntent)
                        .addAction(
                            if (isPlaying) R.drawable.ic_pause_outline else R.drawable.ic_play_outline,
                            if (isPlaying) "Pause" else "Play",
                            playIntent
                        )
                        .addAction(R.drawable.ic_skip_next, "Next", nextIntent)
                        .setStyle(
                            mediaSession?.let {
                                MediaStyle(it)
                                    .setShowActionsInCompactView(0, 1, 2, 3)
                                    .setShowCancelButton(true)
                            }
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOnlyAlertOnce(true)
                        .setOngoing(isPlaying)
                        .build()

                    if (isPlaying) {
                        startForeground(NOTIFICATION_ID, notification)
                    } else {
                        stopForeground(false)
                        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                }
                .build()
        )
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession
}