package com.example.fakestore.data.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class AuthManagerTest {

    private val server = FakeStoreServer()
    private val clock = FakeClock()
    private val storage = InMemoryTokenStorage()
    private val authManager = AuthManager(server.authApi(), storage, clock)

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `login stores token and tokenSavedAt`() = runTest {
        authManager.login("mor_2314", "83r5^_")

        val session = storage.session!!
        assertEquals("token-1", session.token)
        assertEquals(clock.nowMillis, session.savedAtMillis)
        assertTrue(authManager.isLoggedIn())
    }

    @Test
    fun `login with wrong password throws InvalidCredentialsException`() = runTest {
        server.loginResponseCode = 401
        try {
            authManager.login("mor_2314", "wrong")
            fail("Expected InvalidCredentialsException")
        } catch (e: InvalidCredentialsException) {
            assertEquals(401, e.httpCode)
        }
        assertNull(storage.session)
    }

    @Test
    fun `valid token is returned without any network call`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(59_000)

        assertEquals("token-0", authManager.getValidToken())
        assertEquals(0, server.loginCount.get())
    }

    @Test
    fun `token is treated as expired after exactly 60 seconds and refreshed`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(AuthManager.TOKEN_LIFETIME_MS)

        assertEquals("token-1", authManager.getValidToken())
        assertEquals(1, server.loginCount.get())
        assertEquals(clock.nowMillis, storage.session!!.savedAtMillis)
    }

    @Test
    fun `clock moving backwards is treated as expired`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(-1)

        assertEquals("token-1", authManager.getValidToken())
    }

    @Test
    fun `concurrent callers with an expired token trigger exactly one refresh`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(61_000)
        server.loginDelayMillis = 300 // keep the refresh in flight while others arrive

        val threads = 10
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val results = (1..threads).map {
            pool.submit<String> {
                start.await()
                authManager.getValidToken()
            }
        }
        start.countDown()
        val tokens = results.map { it.get(10, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, server.loginCount.get())
        assertTrue(tokens.all { it == "token-1" })
    }

    @Test
    fun `rejected refresh forces logout and emits SessionExpired`() = runTest {
        val events = mutableListOf<AuthEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            authManager.events.toList(events)
        }
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(61_000)
        server.loginResponseCode = 401

        try {
            authManager.getValidToken()
            fail("Expected SessionExpiredException")
        } catch (expected: SessionExpiredException) {
        }

        assertNull(storage.session)
        assertEquals(listOf<AuthEvent>(AuthEvent.SessionExpired), events)
        assertEquals(1, server.loginCount.get()) // no retry loop
    }

    @Test
    fun `network failure during refresh keeps the session`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(61_000)
        server.shutdown()

        try {
            authManager.getValidToken()
            fail("Expected IOException")
        } catch (e: IOException) {
            assertTrue(e !is SessionExpiredException)
        }
        assertNotNull(storage.session)
    }

    @Test
    fun `server outage during login throws ServerUnavailableException`() = runTest {
        server.loginResponseCode = 521 // Cloudflare: "web server is down"
        try {
            authManager.login("mor_2314", "83r5^_")
            fail("Expected ServerUnavailableException")
        } catch (e: ServerUnavailableException) {
            assertEquals(521, e.httpCode)
        }
        assertNull(storage.session)
    }

    @Test
    fun `server outage during refresh keeps the session and does not log out`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(61_000)
        server.loginResponseCode = 503

        try {
            authManager.getValidToken()
            fail("Expected ServerUnavailableException")
        } catch (expected: ServerUnavailableException) {
        }
        assertNotNull(storage.session)
        assertEquals(1, server.loginCount.get()) // no retry loop
    }

    @Test
    fun `no session means SessionExpiredException without network`() {
        try {
            authManager.getValidToken()
            fail("Expected SessionExpiredException")
        } catch (expected: SessionExpiredException) {
        }
        assertEquals(0, server.loginCount.get())
    }

    @Test
    fun `logout during a refresh does not bring the session back`() {
        storage.session = sessionSavedAt(clock)
        clock.advanceBy(61_000)
        server.loginDelayMillis = 300

        val pool = Executors.newSingleThreadExecutor()
        val refresh = pool.submit<Result<String>> { runCatching { authManager.getValidToken() } }
        Thread.sleep(100) // refresh is now waiting for the server
        authManager.logout()

        val result = refresh.get(5, TimeUnit.SECONDS)
        pool.shutdown()

        assertTrue(result.exceptionOrNull() is SessionExpiredException)
        assertNull(storage.session)
    }
}
