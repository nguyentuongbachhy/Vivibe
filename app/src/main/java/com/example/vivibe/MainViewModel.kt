package com.example.vivibe

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.database.DatabaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class MainViewModel(private val appContext: Context) : ViewModel() {

    private val listOfSensitiveData = getSensitiveData()
    val token = listOfSensitiveData[0]
    val googleId = listOfSensitiveData[1]
    val songClient = SongClient(appContext, token!!)

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> get() = _showBottomSheet

    private val _currentBottomSheetSong = MutableStateFlow<QuickPicksSong?>(null)
    val currentBottomSheetSong: StateFlow<QuickPicksSong?> get() = _currentBottomSheetSong

    private val _playingSong = MutableStateFlow<PlaySong?>(null)
    val playingSong: StateFlow<PlaySong?> get() = _playingSong


    fun showBottomSheet(song: QuickPicksSong) {
        viewModelScope.launch {
            _currentBottomSheetSong.value = song
            _showBottomSheet.value = true
        }
    }

    fun hideBottomSheet() {
        viewModelScope.launch {
            _currentBottomSheetSong.value = null
            _showBottomSheet.value = false
        }
    }


    fun fetchPlaySong(songId: Int) {
        viewModelScope.launch {
            try {
                val fetchedPlaySong = songClient.fetchPlayingSong(songId)
                if(fetchedPlaySong != null) {
                    _playingSong.value = fetchedPlaySong
                    println("Playing song fetched successfully")
                } else {
                    println("No playing song fetched")
                }
            } catch (e: Exception) {
                println("Error fetching playing song: ${e.message}")
            }
        }
    }


    // Get sensitive data
    private fun decode(encodedData: String): String {
        return String(Base64.decode(encodedData, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun getSensitiveData(): List<String?> {
        try {
            val file = File(appContext.filesDir, "user_info.json")

            if(!file.exists()) {
                println("User data file not found")
                return listOf("", "")
            }

            val obfuscatedContent = file.readText()
            val content = decode(obfuscatedContent)
            val userData = JSONObject(content)
            val token = userData.optString("token")
            val googleId = userData.optString("googleId")

            if (token.isBlank() || googleId.isBlank()) {
                println("Missing required user data.")
                return listOf("", "")
            }

            return listOf(token, googleId)
        } catch (e: Exception) {
            println("Error loading user data from file: ${e.message}")
            return listOf("", "")
        }
    }
}