package com.example.fakestore.data.auth

/**
 * Persistence for the current [AuthSession]. Implementations must be thread-safe:
 * several OkHttp threads may read while a refresh writes.
 */
interface TokenStorage {
    fun read(): AuthSession?
    fun save(session: AuthSession)
    fun clear()
}
