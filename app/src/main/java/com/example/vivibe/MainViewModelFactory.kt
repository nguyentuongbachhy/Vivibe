package com.example.vivibe

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager

class MainViewModelFactory(private val appContext: Context, private val userManager: UserManager, private val exoPlayer: SharedExoPlayer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(appContext, userManager, exoPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}