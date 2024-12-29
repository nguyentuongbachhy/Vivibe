package com.example.vivibe.pages.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.model.User
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.home.HomeComponentViewModel
import com.example.vivibe.manager.GlobalStateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(appContext: Context, songClient: SongClient) : ViewModel() {
    private val googleAuthClient = GoogleSignInClient(appContext)

    private val _showTokenExpiredDialog = MutableStateFlow(false)
    val showTokenExpiredDialog: StateFlow<Boolean> get() = _showTokenExpiredDialog

    private val _homeComponentViewModel = MutableStateFlow(HomeComponentViewModel(appContext, songClient))
    val homeComponentViewModel: StateFlow<HomeComponentViewModel> get() = _homeComponentViewModel

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    init {
        observeTokenExpiration()
    }

    fun loadHomeComponent() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            reload()
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun reload() {
        viewModelScope.launch {
            _homeComponentViewModel.value.fetchSpeedDial()
            _homeComponentViewModel.value.fetchQuickPicks()
        }
    }

    private fun observeTokenExpiration() {
        viewModelScope.launch {
            GlobalStateManager.tokenExpired.collect {
                if(it) {
                    _showTokenExpiredDialog.value = true
                    GlobalStateManager.setTokenExpired()
                }
            }
        }
    }

    fun dismissTokenExpiredDialog() {
        _showTokenExpiredDialog.value = false
    }

    suspend fun signIn(): Boolean {
        val success = googleAuthClient.signIn()
        if (success) {
            GlobalStateManager.resetTokenExpired()
        }
        return success
    }

    suspend fun signOut(): Boolean {
         return googleAuthClient.signOut()
    }
}