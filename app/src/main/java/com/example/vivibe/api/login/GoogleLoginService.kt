package com.example.vivibe.api.login

import android.content.Context
import android.util.Base64
import com.example.vivibe.R
import com.example.vivibe.User
import com.example.vivibe.api.RetrofitClient
import org.json.JSONObject
import java.io.File

class GoogleLoginService(private val context: Context) {
    private val api: GoogleLoginInterface

    init {
        val baseURL:String = context.getString(R.string.BASE_URL)
        val token = getToken()
        val retrofit = RetrofitClient.getClient(baseURL, token)
        api = retrofit.create(GoogleLoginInterface::class.java)
    }

    private fun decode(encodedData: String): String {
        return String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun getToken(): String? {
        try {
            val file = File(context.filesDir, "user_info.json")

            if(!file.exists()) {
                println("User data file not found")
                return null
            }

            val obfuscatedContent = file.readText()
            val content = decode(obfuscatedContent)
            val userData = JSONObject(content)
            val token = userData.optString("token")

            if (token.isBlank()) {
                println("Missing required user data.")
                return null
            }

            return token
        } catch (e: Exception) {
            println("Error loading user data from file: ${e.message}")
            return null
        }
    }


    suspend fun googleLogin(user: User): GoogleLoginResponse? {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(user))
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}