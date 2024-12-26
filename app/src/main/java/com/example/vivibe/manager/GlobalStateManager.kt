package com.example.vivibe.manager

import android.content.Context
import android.util.Base64
import com.example.vivibe.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File

object GlobalStateManager {
    private val _userState = MutableStateFlow(User("", "", "", "", ""))
    val userState = _userState.asStateFlow()

    private val _tokenExpired = MutableStateFlow(false)
    val tokenExpired: MutableStateFlow<Boolean> = _tokenExpired

    fun updateUser(user: User) {
        _userState.value = user
    }

    fun setTokenExpired() {
        _tokenExpired.value = true
    }

    fun resetTokenExpired() {
        _tokenExpired.value = false
    }

    private fun decode(encodedData: String): String {
        return String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)
    }

    fun loadUserFromFile(context: Context) {
        try {
            val file = File(context.filesDir, "user_info.json")

            if(!file.exists()) {
                println("File does not exist!")
                _userState.value = User("", "", "", "", "")
            }
            else {
                val obfuscatedContent = file.readText()
                val content = decode(obfuscatedContent)
                val userData = JSONObject(content)
                val user = User(
                    token = userData.optString("token", ""),
                    googleId = userData.optString("googleId", ""),
                    name = userData.optString("name", ""),
                    email = userData.optString("email", ""),
                    profilePictureUri = userData.optString("profilePictureUri", "")
                )
                if (user.token!!.isBlank() || user.googleId!!.isBlank()) {
                    _userState.value = User("", "", "", "", "")
                } else {
                    _userState.value = user
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}