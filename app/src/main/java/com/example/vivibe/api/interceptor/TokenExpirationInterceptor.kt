package com.example.vivibe.api.interceptor

import com.example.vivibe.api.login.GlobalStateManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenExpirationInterceptor(private val excludedPaths: List<String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (excludedPaths.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            GlobalStateManager.setTokenExpired()
        } else {
            GlobalStateManager.resetTokenExpired()
        }

        return response
    }
}