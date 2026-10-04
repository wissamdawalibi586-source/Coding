package com.example.fakestore.data.remote

import com.example.fakestore.data.remote.dto.LoginRequest
import com.example.fakestore.data.remote.dto.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Authentication endpoint.
 *
 * Returns [Call] (not a suspend function) because the token refresh runs inside
 * an OkHttp interceptor, which is synchronous and must block until the new token arrives.
 *
 * IMPORTANT: this API is built on an OkHttp client WITHOUT the AuthInterceptor,
 * otherwise a refresh would trigger another refresh forever.
 */
interface AuthApi {

    @POST("auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>
}
