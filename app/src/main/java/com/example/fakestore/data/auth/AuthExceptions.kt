package com.example.fakestore.data.auth

import java.io.IOException

/*
 * Both exceptions extend IOException on purpose: they can be thrown from inside an
 * OkHttp interceptor, and OkHttp only reports IOExceptions as normal call failures.
 * Any other exception type thrown there would crash the app.
 */

/** The user is not logged in anymore (no session, or the refresh was rejected). */
class SessionExpiredException(cause: Throwable? = null) :
    IOException("Session expired. Please log in again.", cause)

/** The server rejected the username/password (HTTP 4xx from /auth/login). */
class InvalidCredentialsException(val httpCode: Int) :
    IOException("Invalid username or password (HTTP $httpCode).")

/**
 * The server answered with an error that is not the user's fault (HTTP 5xx, e.g. the
 * Cloudflare 52x pages shown when FakeStore is down). The session is kept: retry later.
 */
class ServerUnavailableException(val httpCode: Int) :
    IOException("Server is unavailable (HTTP $httpCode).")
