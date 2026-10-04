package com.example.fakestore.data.auth

import com.example.fakestore.data.remote.AuthApi
import com.example.fakestore.data.remote.ProductApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicInteger

/** Clock whose time only moves when the test says so. */
class FakeClock(var nowMillis: Long = 1_000_000L) : Clock {
    override fun nowMillis(): Long = nowMillis
    fun advanceBy(millis: Long) {
        nowMillis += millis
    }
}

/** Thread-safe in-memory replacement for the encrypted storage. */
class InMemoryTokenStorage(initial: AuthSession? = null) : TokenStorage {
    @Volatile
    var session: AuthSession? = initial

    override fun read(): AuthSession? = session
    override fun save(session: AuthSession) {
        this.session = session
    }
    override fun clear() {
        session = null
    }
}

/**
 * Local HTTP server that imitates FakeStore.
 * - `POST /auth/login` returns `token-1`, `token-2`, ... (or [loginResponseCode] if not 200).
 * - `GET /products/...` returns [productsResponseCode] with a small JSON body.
 */
class FakeStoreServer {
    val server = MockWebServer()
    val loginCount = AtomicInteger()
    val productRequests = AtomicInteger()

    @Volatile var loginResponseCode = 200
    @Volatile var loginDelayMillis = 0L
    @Volatile var productsResponseCode = 200

    private val seenAuthHeaders = java.util.concurrent.ConcurrentLinkedQueue<String?>()
    val productAuthHeaders: List<String?> get() = seenAuthHeaders.toList()

    init {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                return when {
                    path.startsWith("/auth/login") -> {
                        val n = loginCount.incrementAndGet()
                        if (loginDelayMillis > 0) Thread.sleep(loginDelayMillis)
                        if (loginResponseCode == 200) {
                            MockResponse().setBody("""{"token":"token-$n"}""")
                        } else {
                            MockResponse().setResponseCode(loginResponseCode)
                                .setBody("username or password is incorrect")
                        }
                    }
                    path.startsWith("/products") -> {
                        productRequests.incrementAndGet()
                        seenAuthHeaders.add(request.getHeader(AuthHeaders.AUTHORIZATION))
                        MockResponse().setResponseCode(productsResponseCode).setBody(
                            if (path == "/products") "[]"
                            else """{"id":1,"title":"Bag","price":10.5,"description":"d","category":"c","image":"i","rating":{"rate":4.1,"count":7}}"""
                        )
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
    }

    private fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Same wiring as NetworkModule: login on a plain client, products on an authenticated one. */
    fun authApi(): AuthApi = retrofit(OkHttpClient()).create(AuthApi::class.java)

    fun productApi(authManager: AuthManager): ProductApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .authenticator(TokenAuthenticator(authManager))
            .build()
        return retrofit(client).create(ProductApi::class.java)
    }

    fun shutdown() = server.shutdown()
}

val TEST_CREDENTIALS = Credentials("mor_2314", "83r5^_")

fun sessionSavedAt(clock: FakeClock, token: String = "token-0") =
    AuthSession(token, clock.nowMillis, TEST_CREDENTIALS)
