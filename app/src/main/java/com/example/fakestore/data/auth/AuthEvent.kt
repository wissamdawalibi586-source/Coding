package com.example.fakestore.data.auth

/** One-time events emitted by [AuthManager] and observed by the UI. */
sealed interface AuthEvent {
    /** Refresh failed and the session was wiped: the UI must return to the login screen. */
    data object SessionExpired : AuthEvent
}
