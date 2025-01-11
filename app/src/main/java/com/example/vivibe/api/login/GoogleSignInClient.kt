package com.example.vivibe.api.login

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.vivibe.R
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await


class GoogleSignInClient(
    private val context: Context,
    private val userManager: UserManager = UserManager.getInstance(context),
    private val googleLoginService: GoogleLoginService = GoogleLoginService.getInstance(context)
) {
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val isSignedIn: Boolean
        get() = firebaseAuth.currentUser != null && userManager.userState.value != null

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return false
        }

        return try {
            val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).await()

            authResult.user?.let { firebaseUser ->
                val googleId = firebaseUser.providerData
                    .firstOrNull { it.providerId == "google.com" }
                    ?.uid

                val user = User(
                    googleId = googleId,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profilePictureUri = firebaseUser.photoUrl?.toString() ?: ""
                )

                googleLoginService.googleLogin(user)?.let { response ->
                    return response.err == 0
                }
            }

            false
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            false
        }
    }

    suspend fun signIn(activityContext: Context): Boolean {
        if (isSignedIn) return true

        return try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(activityContext.getString(R.string.default_web_client_id))
                        .setAutoSelectEnabled(false)
                        .build()
                )
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            handleSignIn(result)
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            false
        }
    }

    suspend fun signOut(): Boolean {
        return try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            firebaseAuth.signOut()
            userManager.clearUser()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}