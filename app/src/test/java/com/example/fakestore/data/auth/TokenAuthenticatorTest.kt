package com.example.fakestore.data.auth

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException

/** The 401 safety net: refresh + retry once, never loop. */
class TokenAuthenticatorTest {

    private val server = FakeStoreServer()
    private val clock = FakeClock()
    private val storage = InMemoryTokenStorage()
    private val authManager = AuthManager(server.authApi(), storage, clock)
    private val productApi = server.productApi(authManager)

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `server keeps answering 401 - one retry then forced logout`() = runBlocking {
        storage.session = sessionSavedAt(clock, token = "rejected")
        server.productsResponseCode = 401

        try {
            productApi.getProducts()
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }

        assertEquals(2, server.productRequests.get()) // original + exactly one retry
        assertEquals(1, server.loginCount.get())
        assertEquals(listOf("Bearer rejected", "Bearer token-1"), server.productAuthHeaders)
        assertNull(storage.session)
    }
}
