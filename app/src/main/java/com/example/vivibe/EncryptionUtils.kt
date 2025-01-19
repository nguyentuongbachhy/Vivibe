package com.example.vivibe

import android.util.Base64
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
    private val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val KEY = "ViVibeMusic2024!"
    private val IV= "MusicInit2024!@#"

    fun encrypt(value: String): String {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(KEY.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(IV.toByteArray())
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedValue = cipher.doFinal(value.toByteArray())
            Base64.encodeToString(encryptedValue, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun createSongData(
        title: String,
        artistName: String,
        thumbnailUrl: String,
        audioUrl: String,
        views: Long
    ): String {
        val expiryTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12)
        val songData = JSONObject().apply {
            put("title", title)
            put("artistName", artistName)
            put("thumbnailUrl", thumbnailUrl)
            put("audioUrl", audioUrl)
            put("views", views)
            put("expiryTime", expiryTime)
        }

        return songData.toString()
    }
}