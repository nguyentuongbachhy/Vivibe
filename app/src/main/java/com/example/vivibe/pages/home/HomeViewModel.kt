package com.example.vivibe.pages.home

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.genre.GenreClient
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.home.HomeComponentViewModel
import com.example.vivibe.database.DatabaseHelper
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.Genre
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel(appContext: Context, exoPlayer: SharedExoPlayer) : ViewModel() {
    data class HomeState(
        val isRefreshing: Boolean = false,
        val showTokenExpiredDialog: Boolean = false,
        val selectedGenre: Genre = Genre.ALL,
        val isNavigatingBack: Boolean = false
    )

    private val userManager = UserManager.getInstance(appContext)

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val savedStateHandle = SavedStateHandle()
    private val _homeComponentViewModel = MutableStateFlow(HomeComponentViewModel(
        savedStateHandle,
        SongClient(appContext, userManager.getToken()),
        GenreClient(appContext, userManager.getToken()),
        DatabaseHelper(appContext),
        userManager = userManager,
        exoPlayer = exoPlayer
    ))
    val homeComponentViewModel: StateFlow<HomeComponentViewModel> get() = _homeComponentViewModel

    private val googleAuthClient = GoogleSignInClient(appContext)

    init {
        observeTokenExpiration()
    }

    fun loadHomeComponent() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            delay(250)
            reload()
            delay(250)
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun reload() {
        viewModelScope.launch {
            _homeComponentViewModel.value.reloadPage()
        }
    }

    private fun observeTokenExpiration() {
        viewModelScope.launch {
            userManager.tokenExpired.collect { isExpired ->
                if(isExpired) {
                    _state.value = _state.value.copy(showTokenExpiredDialog = true)
                    userManager.setTokenExpired()
                }
            }
        }
    }

    fun updateSelectedGenre(genre: Genre) {
        viewModelScope.launch {
            _state.value = _state.value.copy(selectedGenre = genre)
        }
    }

    fun dismissTokenExpiredDialog() {
        viewModelScope.launch {
            _state.value = _state.value.copy(showTokenExpiredDialog = false)
        }
    }

    suspend fun signIn(activityContext: Context): Boolean {
        val success = googleAuthClient.signIn(activityContext)
        if (success) {
            userManager.resetTokenExpired()
        }
        return success
    }

    suspend fun signOut(): Boolean {
        return googleAuthClient.signOut()
    }
}