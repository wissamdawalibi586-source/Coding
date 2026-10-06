package com.example.fakestore.data.mock

import com.example.fakestore.data.auth.AuthInterceptor
import com.example.fakestore.data.auth.AuthManager
import com.example.fakestore.data.auth.FakeClock
import com.example.fakestore.data.auth.InMemoryTokenStorage
import com.example.fakestore.data.auth.InvalidCredentialsException
import com.example.fakestore.data.auth.TokenAuthenticator
import com.example.fakestore.data.remote.ApiConfig
import com.example.fakestore.data.remote.AuthApi
import com.example.fakestore.data.remote.ProductApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** The app wired exactly like NetworkModule with the mock enabled: no network at all. */
class MockBackendInterceptorTest {

    private val mock = MockBackendInterceptor(loginDelayMillis = 200, productsDelayMillis = 0)
    private val clock = FakeClock()
    private val storage = InMemoryTokenStorage()

    private fun retrofit(client: OkHttpClient) = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi = retrofit(OkHttpClient.Builder().addInterceptor(mock).build())
        .create(AuthApi::class.java)
    private val authManager = AuthManager(authApi, storage, clock)
    private val productApi = retrofit(
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .addInterceptor(mock)
            .authenticator(TokenAuthenticator(authManager))
            .build(),
    ).create(ProductApi::class.java)

    @Test
    fun `login and products work end to end`() = runBlocking {
        authManager.login(MockBackendInterceptor.USERNAME, MockBackendInterceptor.PASSWORD)

        val products = productApi.getProducts()
        val first = productApi.getProduct(1)

        assertEquals(10, products.size)
        assertEquals(1, first.id)
        assertEquals("mock-token-1", storage.session!!.token)
    }

    @Test
    fun `wrong password is rejected`() = runBlocking {
        try {
            authManager.login(MockBackendInterceptor.USERNAME, "wrong")
            fail("Expected InvalidCredentialsException")
        } catch (e: InvalidCredentialsException) {
            assertEquals(401, e.httpCode)
        }
    }

    @Test
    fun `expired token with parallel requests triggers a single mock login`() = runBlocking {
        authManager.login(MockBackendInterceptor.USERNAME, MockBackendInterceptor.PASSWORD)
        clock.advanceBy(61_000)

        (1..5).map { async(Dispatchers.IO) { productApi.getProduct(it) } }.awaitAll()

        assertEquals(2, mock.loginCount.get()) // the initial login + exactly one refresh
        assertEquals("mock-token-2", storage.session!!.token)
    }

    @Test
    fun `unknown product returns 404`() = runBlocking {
        authManager.login(MockBackendInterceptor.USERNAME, MockBackendInterceptor.PASSWORD)
        try {
            productApi.getProduct(999)
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(404, e.code())
        }
        assertTrue(MockProducts.ids.contains(1))
    }
}
