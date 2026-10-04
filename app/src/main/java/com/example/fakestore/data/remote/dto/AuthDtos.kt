package com.example.fakestore.data.remote.dto

/** Body of `POST /auth/login`. */
data class LoginRequest(
    val username: String,
    val password: String,
)

/** Response of `POST /auth/login`. Nullable because Gson can leave fields null. */
data class LoginResponse(
    val token: String?,
)
