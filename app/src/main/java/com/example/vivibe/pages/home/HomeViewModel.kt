package com.example.vivibe.pages.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.model.User
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.api.login.GlobalStateManager
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.home.HomeComponentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(appContext: Context, token: String, googleId: String, songClient: SongClient) : ViewModel() {
    private val googleAuthClient = GoogleSignInClient(appContext)

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> get() = _isSignedIn

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _showTokenExpiredDialog = MutableStateFlow(false)
    val showTokenExpiredDialog: StateFlow<Boolean> get() = _showTokenExpiredDialog

    private val _homeComponentViewModel = MutableStateFlow(HomeComponentViewModel(appContext, token, googleId, songClient))
    val homeComponentViewModel: StateFlow<HomeComponentViewModel> get() = _homeComponentViewModel

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    init {
        initializeUser()
        observeTokenExpiration()
    }

    fun loadHomeComponent() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            homeComponentViewModel.value.fetchSpeedDial()
            homeComponentViewModel.value.fetchQuickPicks()
            delay(500)
            _isRefreshing.value = false
        }
    }


    private fun initializeUser() {
        val savedUser = googleAuthClient.loadUserDataFromFile()
        if (savedUser != null) {
            googleAuthClient.user = savedUser
            _isSignedIn.value = true
            _user.value = savedUser
            println("User loaded successfully: ${savedUser.name}")
        } else {
            println("No saved user data found.")
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

    fun signIn() {
        viewModelScope.launch {
            val success = googleAuthClient.signIn()
            _isSignedIn.value = googleAuthClient.isSignedIn()

            if (success) {
                _user.value = googleAuthClient.user
                GlobalStateManager.resetTokenExpired()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthClient.signOut()
            _isSignedIn.value = googleAuthClient.isSignedIn()
            _user.value = null
        }
    }
}