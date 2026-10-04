package com.example.fakestore.data.auth

import com.example.fakestore.data.remote.ProductApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** End-to-end: Retrofit + OkHttp + AuthInterceptor + AuthManager against a local server. */
class AuthInterceptorTest {

    private val server = FakeStoreServer()
    private val clock = FakeClock()
    private val storage = InMemoryTokenStorage()
    private val authManager = AuthManager(server.authApi(), storage, clock)
    private val productApi: ProductApi = server.productApi(authManager)

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `every product request carries the bearer token`() = runBlocking {
        storage.session = sessionSavedAt(clock, token = "abc")

        productApi.getProducts()
        productApi.getProduct(1)

        assertEquals(listOf("Bearer abc", "Bearer abc"), server.productAuthHeaders)
        assertEquals(0, server.loginCount.get())
    }

    @Test
    fun `expired token is refreshed before the request is sent`() = runBlocking {
        storage.session = sessionSavedAt(clock, token = "old")
        clock.advanceBy(61_000)

        productApi.getProducts()

        assertEquals(1, server.loginCount.get())
        assertEquals(listOf("Bearer token-1"), server.productAuthHeaders) // never "Bearer old"
    }

    @Test
    fun `parallel requests with an expired token share one refresh`() = runBlocking {
        storage.session = sessionSavedAt(clock, token = "old")
        clock.advanceBy(61_000)
        server.loginDelayMillis = 300

        (1..5).map { async(kotlinx.coroutines.Dispatchers.IO) { productApi.getProduct(it) } }.awaitAll()

        assertEquals(1, server.loginCount.get())
        assertEquals(5, server.productRequests.get())
        assertTrue(server.productAuthHeaders.all { it == "Bearer token-1" })
    }

    @Test
    fun `without a session the request fails and never reaches the server`() = runBlocking {
        try {
            productApi.getProducts()
            fail("Expected SessionExpiredException")
        } catch (expected: SessionExpiredException) {
        }
        assertEquals(0, server.productRequests.get())
    }
}
