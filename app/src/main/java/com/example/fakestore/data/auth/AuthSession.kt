package com.example.fakestore.data.auth

/**
 * Username and password, kept only to simulate a token refresh (FakeStore has no
 * refresh endpoint). A real backend would issue a refresh token instead.
 */
data class Credentials(
    val username: String,
    val password: String,
) {
    override fun toString(): String = "Credentials(username=$username, password=***)"
}

/**
 * Everything we persist after a successful login.
 *
 * @property savedAtMillis the `tokenSavedAt` timestamp used to compute expiry.
 */
data class AuthSession(
    val token: String,
    val savedAtMillis: Long,
    val credentials: Credentials,
) {
    override fun toString(): String =
        "AuthSession(token=***, savedAtMillis=$savedAtMillis, credentials=$credentials)"
}
