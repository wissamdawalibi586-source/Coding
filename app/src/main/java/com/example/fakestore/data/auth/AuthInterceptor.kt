package com.example.fakestore.data.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds `Authorization: Bearer <token>` to every request of the authenticated client.
 *
 * [AuthManager.getValidToken] guarantees the token is not expired: if it is, the call
 * blocks here until the (single, shared) refresh finishes. A request is therefore never
 * sent with an expired token.
 */
class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authManager.getValidToken()

        val authorizedRequest = chain.request().newBuilder()
            .header(AuthHeaders.AUTHORIZATION, AuthHeaders.bearer(token))
            .build()

        return chain.proceed(authorizedRequest)
    }
}
