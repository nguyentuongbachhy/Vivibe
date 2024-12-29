package com.example.vivibe.manager

import android.content.Context
import android.util.Base64
import com.example.vivibe.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File

object GlobalStateManager {
    private val _userState = MutableStateFlow(User("", "", "", "", "", 0))
    val userState = _userState.asStateFlow()

    private val _tokenExpired = MutableStateFlow(false)
    val tokenExpired: MutableStateFlow<Boolean> = _tokenExpired

    fun setTokenExpired() {
        _tokenExpired.value = true
    }

    fun resetTokenExpired() {
        _tokenExpired.value = false
    }

    private fun encode(data: String): String {
        return Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    private fun decode(encodedData: String): String {
        return String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)
    }

    fun loadUserFromFile(context: Context) {
        try {
            val file = File(context.filesDir, "user_info.json")

            if(!file.exists()) {
                println("File does not exist!")
                _userState.value = User("", "", "", "", "", 0)
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
                    profilePictureUri = userData.optString("profilePictureUri", ""),
                    premium = userData.optInt("premium", 0)
                )
                if (user.token!!.isBlank() || user.googleId!!.isBlank()) {
                    _userState.value = User("", "", "", "", "", 0)
                } else {
                    _userState.value = user
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun saveUserDataToFile(context: Context, user: User) {
        try {
            val userData = JSONObject().apply {
                put("token", user.token)
                put("googleId", user.googleId)
                put("name", user.name)
                put("email", user.email)
                put("profilePictureUri", user.profilePictureUri)
                put("premium", user.premium)
            }

            val obfuscatedData = encode(userData.toString())
            val file = File(context.filesDir, "user_info.json")
            file.writeText(obfuscatedData)
            loadUserFromFile(context)
            println("User data saved successfully.")
        } catch (e: Exception) {
            println("Error saving user data to file: ${e.message}")
        }
    }

    fun deleteUserDataFile(context: Context) {
        try {
            val file = File(context.filesDir, "user_info.json")
            if(file.exists()) {
                val success = file.delete()
                println("User data file deleted: $success")
            }
            loadUserFromFile(context)
        } catch (e: Exception) {
            print("Error deleting user data file: ${e.message}")
        }
    }

    fun reset() {
        _userState.value = User("", "", "", "", "", 0)
        _tokenExpired.value = false
    }
}