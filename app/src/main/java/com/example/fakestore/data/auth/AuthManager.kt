package com.example.fakestore.data.auth

import com.example.fakestore.data.remote.AuthApi
import com.example.fakestore.data.remote.dto.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single owner of the authentication state: login, token expiry, refresh and logout.
 *
 * Token rules (simulated, because FakeStore tokens never really expire):
 * - A token is valid for [TOKEN_LIFETIME_MS] after it was saved (`tokenSavedAt`).
 * - An expired token is never handed out: it is refreshed first.
 * - Refresh = calling `/auth/login` again with the stored credentials.
 * - Concurrent callers share ONE refresh: the first one refreshes while the others
 *   wait on [refreshLock], then reuse the new token (double-checked locking).
 * - If the server rejects the refresh, the user is logged out ([AuthEvent.SessionExpired]).
 */
@Singleton
class AuthManager @Inject constructor(
    private val authApi: AuthApi,
    private val storage: TokenStorage,
    private val clock: Clock,
) {

    /** Serializes refreshes: only one thread at a time may request a new token. */
    private val refreshLock = Any()

    /**
     * Guards writes to [storage] together with [sessionGeneration]. Held only briefly
     * (never during network calls), so logout() on the main thread never waits for a refresh.
     */
    private val storageLock = Any()

    /** Incremented on every logout, so a refresh that started before it cannot revive the session. */
    private var sessionGeneration = 0

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun isLoggedIn(): Boolean = storage.read() != null

    /**
     * Logs in from the UI and stores the new session.
     *
     * @throws InvalidCredentialsException if the server rejects the username/password.
     * @throws IOException on network or server errors.
     */
    suspend fun login(username: String, password: String) {
        withContext(Dispatchers.IO) {
            val generation = currentGeneration()
            val session = requestNewSession(Credentials(username, password))
            saveIfStillCurrent(generation, session)
        }
    }

    /**
     * Returns a token that is safe to send right now, refreshing it first if it expired.
     *
     * Blocking: called by [AuthInterceptor] on an OkHttp background thread.
     *
     * @throws SessionExpiredException if there is no session or the refresh was rejected.
     * @throws IOException if the refresh failed because of the network (session is kept).
     */
    fun getValidToken(): String {
        // Fast path: no locking when the token is still valid (the common case).
        storage.read()?.takeUnless { it.isExpired() }?.let { return it.token }

        synchronized(refreshLock) {
            // Second check: while we waited for the lock, another thread may have refreshed.
            val session = storage.read() ?: throw SessionExpiredException()
            if (!session.isExpired()) return session.token
            return refresh(session.credentials)
        }
    }

    /**
     * Called by [TokenAuthenticator] when the server answered 401 to [rejectedToken].
     *
     * @return a token to retry with, or null to give up.
     */
    fun refreshAfterUnauthorized(rejectedToken: String?): String? {
        synchronized(refreshLock) {
            val session = storage.read() ?: return null
            // Another request already replaced the rejected token: just retry with the new one.
            if (session.token != rejectedToken) return session.token
            return try {
                refresh(session.credentials)
            } catch (e: IOException) {
                null
            }
        }
    }

    /** User-initiated logout. */
    fun logout() {
        synchronized(storageLock) {
            sessionGeneration++
            storage.clear()
        }
    }

    /** Logout caused by a failed refresh: clears the session and tells the UI. */
    fun forceLogout() {
        logout()
        _events.tryEmit(AuthEvent.SessionExpired)
    }

    /** Must be called while holding [refreshLock]. */
    private fun refresh(credentials: Credentials): String {
        val generation = currentGeneration()
        val newSession = try {
            requestNewSession(credentials)
        } catch (e: InvalidCredentialsException) {
            // The server refused our credentials: retrying would fail forever, so log out.
            forceLogout()
            throw SessionExpiredException(e)
        }
        saveIfStillCurrent(generation, newSession)
        return newSession.token
    }

    /** Blocking network call to `/auth/login`. */
    private fun requestNewSession(credentials: Credentials): AuthSession {
        val response = authApi
            .login(LoginRequest(credentials.username, credentials.password))
            .execute()

        if (response.code() in 400..499) throw InvalidCredentialsException(response.code())
        if (!response.isSuccessful) throw ServerUnavailableException(response.code())

        val token = response.body()?.token
        if (token.isNullOrBlank()) throw IOException("Login response did not contain a token")

        return AuthSession(token, clock.nowMillis(), credentials)
    }

    private fun currentGeneration(): Int = synchronized(storageLock) { sessionGeneration }

    /** Saves [session] unless a logout happened since [generation] was read. */
    private fun saveIfStillCurrent(generation: Int, session: AuthSession) {
        synchronized(storageLock) {
            if (generation != sessionGeneration) throw SessionExpiredException()
            storage.save(session)
        }
    }

    private fun AuthSession.isExpired(): Boolean {
        val age = clock.nowMillis() - savedAtMillis
        // A negative age means the device clock moved backwards: don't trust the token.
        return age < 0 || age >= TOKEN_LIFETIME_MS
    }

    companion object {
        const val TOKEN_LIFETIME_MS = 60_000L
    }
}
