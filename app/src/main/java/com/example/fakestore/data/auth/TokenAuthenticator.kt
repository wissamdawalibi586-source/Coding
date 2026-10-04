package com.example.fakestore.data.auth

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * Safety net for a real backend: OkHttp calls this when the server answers 401.
 * (FakeStore never does, because it does not check tokens; expiry is handled up front
 * by [AuthInterceptor].)
 *
 * Retries at most ONCE per request, so a server that keeps rejecting us can never
 * cause an infinite refresh loop.
 */
class TokenAuthenticator @Inject constructor(
    private val authManager: AuthManager,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // priorResponse != null means this request was already retried once and failed again.
        if (response.priorResponse != null) {
            authManager.forceLogout()
            return null
        }

        val rejectedToken = AuthHeaders.tokenFrom(response.request.header(AuthHeaders.AUTHORIZATION))
        val newToken = authManager.refreshAfterUnauthorized(rejectedToken) ?: return null

        return response.request.newBuilder()
            .header(AuthHeaders.AUTHORIZATION, AuthHeaders.bearer(newToken))
            .build()
    }
}
