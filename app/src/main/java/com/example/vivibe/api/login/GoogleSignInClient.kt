package com.example.vivibe.api.login

import android.util.Base64
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.vivibe.R
import com.example.vivibe.manager.GlobalStateManager
import com.example.vivibe.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await


class GoogleSignInClient(
    private val context: Context,
) {
    private val tag = "GoogleSignInClient: "
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleLoginService = GoogleLoginService(context)

    private fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null && GlobalStateManager.userState.value != User("", "", "", "", "", 0)) {
            println(tag + "already signed in")
            return true
        }
        return false
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }

    suspend fun signIn(): Boolean {
        if (isSignedIn()) {
            return true
        }
        try {
            val result = buildCredentialRequest()

            return handleSignIn(result)

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            println(tag + "signIn error: ${e.message}")
        }
        return false
    }

    private suspend fun googleLoginWithServer(user: User): GoogleLoginResponse? {
        return googleLoginService.googleLogin(user)
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                val googleId = authResult.user?.providerData
                    ?.firstOrNull { it.providerId == "google.com" }
                    ?.uid

                val user = User(
                    token = "",
                    googleId = googleId,
                    name = authResult.user?.displayName ?: "",
                    email = authResult.user?.email ?: "",
                    profilePictureUri = authResult.user?.photoUrl.toString(),
                    premium = 0
                )

                val serverResponse = googleLoginWithServer(user)

                if (serverResponse?.err == 0) {
                    println("$tag server response: ${serverResponse.premium}")
                    val token: String = serverResponse.token!!
                    val updatedUser = User(
                        token = token,
                        googleId = user.googleId,
                        name = user.name,
                        email = user.email,
                        profilePictureUri = user.profilePictureUri,
                        premium = serverResponse.premium
                    )
                    GlobalStateManager.saveUserDataToFile(context, updatedUser)
                    println("$tag User signed in successfully")
                }

                return authResult.user != null

            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "handleSignIn error: ${e.message}")
                return false
            }
        } else {
            println(tag + "handleSignIn error: Invalid credential type")
            return false
        }
    }

    suspend fun signOut(): Boolean {
        try {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            firebaseAuth.signOut()
             GlobalStateManager.deleteUserDataFile(context)
            return true
        } catch (e: Exception) {
            println(tag + "Error clearing credentials: ${e.message}")
            return false
        }
    }
}