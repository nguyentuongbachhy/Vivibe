package com.example.vivibe.api.login

import android.util.Base64
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.vivibe.R
import com.example.vivibe.User
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

    var user: User? = null


    fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null) {
            println(tag + "already signed in")
            return true
        }
        return false
    }

    private fun encode(data: String): String {
        return Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    private fun decode(encodedData: String): String {
        return String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun saveUserDataToFile(token: String, user: User) {
        try {
            val userData = JSONObject().apply {
                put("token", token)
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

    fun loadUserDataFromFile(): User? {
        try {
            val file = File(context.filesDir, "user_info.json")

            if(!file.exists()) {
                println("$tag User data file not found")
                return null
            }

            val obfuscatedContent = file.readText()
            val content = decode(obfuscatedContent)
            val userData = JSONObject(content)

            val googleId = userData.optString("googleId")
            val name = userData.optString("name")
            val email = userData.optString("email")
            val profilePictureUri = userData.optString("profilePictureUri")

            if (googleId.isBlank() || name.isBlank() || email.isBlank()) {
                println("$tag Missing required user data.")
                return null
            }

            println("$tag Loaded user data: Google ID=$googleId, Name=$name, Email=$email")


            return User(googleId, name, email, profilePictureUri)
        } catch (e: Exception) {
            println("$tag Error loading user data from file: ${e.message}")
            return null
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
                val idToken: String = tokenCredential.idToken

                if (idToken.isEmpty()) {
                    println(tag + "handleSignIn error: Missing ID token")
                    return false
                }

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                val googleId = authResult.user?.providerData
                    ?.firstOrNull { it.providerId == "google.com" }
                    ?.uid

                authResult.user?.let {
                    user = User(
                        googleId = googleId,
                        name = it.displayName,
                        email = it.email,
                        profilePictureUri = it.photoUrl?.toString()
                    )
                }

                refreshIdToken()

                val serverResponse = googleLoginWithServer(user!!)

                if (serverResponse?.err == 0) {
                    val token: String = serverResponse.token!!
                    saveUserDataToFile(token, user!!)
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

    private fun refreshIdToken() {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newToken = task.result?.token
                println("$tag New token: $newToken")

                val sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("token", newToken).apply()
            } else {
                println("$tag Failed to refresh token: ${task.exception?.message}")
            }
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest()
            )
            firebaseAuth.signOut()
            user = null

            deleteUserDataFile()
        } catch (e: Exception) {
            println(tag + "Error clearing credentials: ${e.message}")
        }
    }
}