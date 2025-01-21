package com.example.vivibe.api

import com.example.vivibe.api.interceptor.AuthInterceptor
import com.example.vivibe.api.interceptor.TokenExpirationInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getClient(baseUrl: String, token: String? = null) : Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val excludedPaths = listOf(
            "/login",
            "/refresh-token",
            "/google"
        )
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .addInterceptor(TokenExpirationInterceptor(excludedPaths))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()


        if (retrofit == null || retrofit?.baseUrl()?.toString() != baseUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!
    }

}