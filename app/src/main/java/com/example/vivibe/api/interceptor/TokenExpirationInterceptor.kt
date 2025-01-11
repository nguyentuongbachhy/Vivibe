package com.example.vivibe.api.interceptor

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
        return response
    }
}