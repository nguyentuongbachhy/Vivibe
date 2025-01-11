package com.example.vivibe.api.login

import android.annotation.SuppressLint
import android.content.Context
import com.example.vivibe.R
import com.example.vivibe.model.User
import com.example.vivibe.api.RetrofitClient
import com.example.vivibe.manager.UserManager

class GoogleLoginService private constructor(
    context: Context,
    private val userManager: UserManager
) {
    private val api: GoogleLoginInterface

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var instance: GoogleLoginService? = null

        fun getInstance(context: Context): GoogleLoginService {
            return instance ?: synchronized(this) {
                instance ?: GoogleLoginService(
                    context.applicationContext,
                    UserManager.getInstance(context)
                ).also { instance = it }
            }
        }
    }

    init {
        val baseURL = context.getString(R.string.BASE_URL)
        val retrofit = RetrofitClient.getClient(baseURL, userManager.getToken())
        api = retrofit.create(GoogleLoginInterface::class.java)
    }

    suspend fun googleLogin(user: User): GoogleLoginResponse? {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(user))
            response.body()?.also {
                if (it.err == 0 && it.token != null) {
                    userManager.saveUser(user.copy(
                        token = it.token,
                        premium = it.premium
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}