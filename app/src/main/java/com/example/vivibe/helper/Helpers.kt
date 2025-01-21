package com.example.vivibe.helper

import android.annotation.SuppressLint
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Date
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Helpers {
    private var transIdCounter = 1
    private const val MAX_TRANS_ID = 100000
    private const val TRANS_ID_FORMAT = "%s%06d"
    private const val DATE_FORMAT = "yyMMdd_hhmmss"

    @SuppressLint("SimpleDateFormat")
    fun getAppTransId(): String {
        transIdCounter = if (transIdCounter >= MAX_TRANS_ID) 1 else transIdCounter + 1

        val timeString = SimpleDateFormat(DATE_FORMAT)
            .format(Date())

        return TRANS_ID_FORMAT.format(timeString, transIdCounter)
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun getMac(key: String, data: String): String {
        return try {
            val sha256HMAC = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            sha256HMAC.init(secretKey)

            val macBytes = sha256HMAC.doFinal(data.toByteArray(Charsets.UTF_8))

            // Convert bytes to hex string
            buildString {
                macBytes.forEach { byte ->
                    append("%02x".format(byte))
                }
            }
        } catch (e: Exception) {
            throw Exception("MAC Generation failed: ${e.message}", e)
        }
    }
}