package com.example.vivibe.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String?): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bear $token")
        }

        return chain.proceed(requestBuilder.build())
    }

}