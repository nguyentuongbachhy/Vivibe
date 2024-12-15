package com.example.vivibe.pages.home

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.User
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.api.login.GlobalStateManager
import com.example.vivibe.components.home.HomeComponent
import com.example.vivibe.components.home.HomeComponentViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class HomeViewModel(private val context:Context) : ViewModel() {
    private val googleAuthClient = GoogleSignInClient(context)
    private val listOfSensitiveData = getSensitiveData()
    val token = listOfSensitiveData[0]
    private val googleId = listOfSensitiveData[1]


    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> get() = _isSignedIn

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _showTokenExpiredDialog = MutableStateFlow(false)
    val showTokenExpiredDialog: StateFlow<Boolean> get() = _showTokenExpiredDialog

    private val _homeComponentViewModel = MutableStateFlow(HomeComponentViewModel(context, token!!, googleId!!))
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
            homeComponentViewModel.value.fetchSpeedDial()
            homeComponentViewModel.value.fetchQuickPicks()
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

    private fun decode(encodedData: String): String {
        return String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun getSensitiveData(): List<String?> {
        try {
            val file = File(context.filesDir, "user_info.json")

            if(!file.exists()) {
                println("User data file not found")
                return emptyList()
            }

            val obfuscatedContent = file.readText()
            val content = decode(obfuscatedContent)
            val userData = JSONObject(content)
            val token = userData.optString("token")
            val googleId = userData.optString("googleId")

            if (token.isBlank() || googleId.isBlank()) {
                println("Missing required user data.")
                return emptyList()
            }

            return listOf(token, googleId)
        } catch (e: Exception) {
            println("Error loading user data from file: ${e.message}")
            return emptyList()
        }
    }
}