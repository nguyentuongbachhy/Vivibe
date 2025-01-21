package com.example.vivibe.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vivibe.components.song.PlaybackService

class MediaControlReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "MediaControlReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Received action: ${intent?.action}")

        // Forward the intent to PlaybackService
        context?.let {
            Intent(it, PlaybackService::class.java).also { serviceIntent ->
                serviceIntent.action = intent?.action
                it.startService(serviceIntent)
            }
        }
    }
}