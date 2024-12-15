package com.example.vivibe.api.login

import kotlinx.coroutines.flow.MutableStateFlow

object GlobalStateManager {
    private val _tokenExpired = MutableStateFlow(false)
    val tokenExpired: MutableStateFlow<Boolean> = _tokenExpired

    fun setTokenExpired() {
        _tokenExpired.value = true
    }

    fun resetTokenExpired() {
        _tokenExpired.value = false
    }
}