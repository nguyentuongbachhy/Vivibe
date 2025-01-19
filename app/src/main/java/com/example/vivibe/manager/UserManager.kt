package com.example.vivibe.manager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import com.example.vivibe.model.User
import com.google.common.base.Charsets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File

class UserManager private constructor(private val context: Context){
    private val _userState = MutableStateFlow<User?>(null)
    val userState = _userState.asStateFlow()

    private val _tokenExpired = MutableStateFlow(false)
    val tokenExpired = _tokenExpired.asStateFlow()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: UserManager? =null
        private const val USER_FILE = "user_info.json"

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        loadUserFromFile()
    }

    fun setTokenExpired() {
        _tokenExpired.value = true
    }

    fun resetTokenExpired() {
        _tokenExpired.value = false
    }

    private fun encode(data: String): String = Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)

    private fun decode(encodedData: String): String = String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)

    private fun getUserFile() = File(context.filesDir, USER_FILE)

    private fun loadUserFromFile() {
        try {
            val file = getUserFile()
            if(!file.exists()) {
                _userState.value = null
                return
            }

            val userData = JSONObject(decode(file.readText()))
            val user = User(
                token = userData.optString("token"),
                googleId = userData.optString("googleId"),
                name = userData.optString("name"),
                email = userData.optString("email"),
                profilePictureUri = userData.optString("profilePictureUri"),
                premium = userData.optInt("premium")
            )

            _userState.value = if (user.isValid()) user else null

        } catch (e: Exception) {
            _userState.value = null
            e.printStackTrace()
        }
    }

    fun saveUser(user: User) {
        try {
            val userData = JSONObject().apply {
                put("token", user.token)
                put("googleId", user.googleId)
                put("name", user.name)
                put("email", user.email)
                put("profilePictureUri", user.profilePictureUri)
                put("premium", user.premium)
            }

            getUserFile().writeText(encode(userData.toString()))
            _userState.value = user
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearUser() {
        try {
            getUserFile().delete()
            _userState.value = null
            _tokenExpired.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getToken(): String? = userState.value?.token

    fun getGoogleId(): String? = userState.value?.googleId
}