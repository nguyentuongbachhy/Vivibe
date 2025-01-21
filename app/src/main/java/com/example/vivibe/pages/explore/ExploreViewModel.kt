package com.example.vivibe.pages.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.vivibe.api.login.GoogleSignInClient
import com.example.vivibe.manager.UserManager

class ExploreViewModel(appContext: Context, private val userManager: UserManager): ViewModel() {
    private val googleAuthClient = GoogleSignInClient(appContext)

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