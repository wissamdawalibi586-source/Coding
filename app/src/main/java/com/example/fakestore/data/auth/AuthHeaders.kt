package com.example.fakestore.data.auth

object AuthHeaders {
    const val AUTHORIZATION = "Authorization"
    private const val BEARER_PREFIX = "Bearer "

    fun bearer(token: String): String = BEARER_PREFIX + token

    /** Extracts the token from `Bearer <token>`, or null if the header is missing. */
    fun tokenFrom(headerValue: String?): String? =
        headerValue?.takeIf { it.startsWith(BEARER_PREFIX) }?.removePrefix(BEARER_PREFIX)
}
