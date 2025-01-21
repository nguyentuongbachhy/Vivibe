package com.example.vivibe.api.login

import com.example.vivibe.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class GoogleLoginRequest(
    val user: User
)

data class GoogleLoginResponse(
    val err: Int,
    val msg: String,
    val id: String,
    val token: String? = null,
    val premium: Int
)

interface GoogleLoginInterface {
    @POST("/api/v1/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest) : Response<GoogleLoginResponse>
}