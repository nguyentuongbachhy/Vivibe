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
import org.json.JSONObject
import java.io.File



class GoogleSignInClient(
    private val context: Context,
) {
    private val tag = "GoogleSignInClient: "
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleLoginService = GoogleLoginService(context)

    private fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null && GlobalStateManager.userState.value != User("", "", "", "", "")) {
            println(tag + "already signed in")
            return true
        }
        return false
    }

    private fun encode(data: String): String {
        return Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    private fun saveUserDataToFile(user: User) {
        try {
            val userData = JSONObject().apply {
                put("token", user.token)
                put("googleId", user.googleId)
                put("name", user.name)
                put("email", user.email)
                put("profilePictureUri", user.profilePictureUri)
            }

            val obfuscatedData = encode(userData.toString())
            val file = File(context.filesDir, "user_info.json")
            file.writeText(obfuscatedData)
            println("$tag User data saved successfully.")
        } catch (e: Exception) {
            println("$tag Error saving user data to file: ${e.message}")
        }
    }

    private fun deleteUserDataFile() {
        try {
            val file = File(context.filesDir, "user_info.json")
            if(file.exists()) {
                val success = file.delete()
                println("User data file deleted: $success")
            }
        } catch (e: Exception) {
            print(tag + "Error deleting user data file: ${e.message}")
        }
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
                    profilePictureUri = authResult.user?.photoUrl.toString()
                )

                val serverResponse = googleLoginWithServer(user)

                if (serverResponse?.err == 0) {
                    val token: String = serverResponse.token!!
                    val updatedUser = User(
                        token = token,
                        googleId = user.googleId,
                        name = user.name,
                        email = user.email,
                        profilePictureUri = user.profilePictureUri
                    )
                    saveUserDataToFile(updatedUser)
                    GlobalStateManager.updateUser(updatedUser)
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

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            firebaseAuth.signOut()
            deleteUserDataFile()
             GlobalStateManager.updateUser(User("", "", "", "", ""))
        } catch (e: Exception) {
            println(tag + "Error clearing credentials: ${e.message}")
        }
    }
}