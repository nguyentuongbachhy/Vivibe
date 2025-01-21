package com.example.vivibe.api

import android.util.Log
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object HttpProvider {
    fun sendPost(url: String, formBody: RequestBody): JSONObject {
        return try {
            val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build()

            val client = OkHttpClient.Builder()
                .connectionSpecs(listOf(spec))
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body

                if (responseBody == null) {
                    Log.e("HttpProvider", "Response body is null")
                    return@use JSONObject().apply {
                        put("return_code", "-1")
                        put("return_message", "Empty response from server")
                    }
                }

                val responseString = responseBody.string()
                Log.d("HttpProvider", "Response: $responseString")

                if (!response.isSuccessful) {
                    Log.e("HttpProvider", "Request failed with code: ${response.code}")
                    return@use JSONObject().apply {
                        put("return_code", "-1")
                        put("return_message", "HTTP ${response.code}: $responseString")
                    }
                }

                JSONObject(responseString)
            }

        } catch (e: IOException) {
            Log.e("HttpProvider", "IO Exception: ${e.message}")
            e.printStackTrace()
            try {
                JSONObject().apply {
                    put("return_code", "-1")
                    put("return_message", "Network error: ${e.message}")
                }
            } catch (je: JSONException) {
                Log.e("HttpProvider", "JSON Exception while handling IO Exception", je)
                createErrorResponse("Unknown error occurred")
            }
        } catch (e: JSONException) {
            Log.e("HttpProvider", "JSON Exception: ${e.message}")
            e.printStackTrace()
            try {
                JSONObject().apply {
                    put("return_code", "-1")
                    put("return_message", "Invalid JSON response: ${e.message}")
                }
            } catch (je: JSONException) {
                Log.e("HttpProvider", "JSON Exception while handling JSON Exception", je)
                createErrorResponse("Unknown error occurred")
            }
        }
    }

    private fun createErrorResponse(message: String): JSONObject {
        return try {
            JSONObject().apply {
                put("return_code", "-1")
                put("return_message", message)
            }
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }
}