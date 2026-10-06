package com.example.fakestore.data.mock

import com.example.fakestore.data.auth.AuthHeaders
import com.example.fakestore.data.remote.dto.LoginRequest
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.concurrent.atomic.AtomicInteger

/**
 * Development-only stand-in for fakestoreapi.com, used while the real server is down.
 *
 * It is added as the LAST interceptor, so everything before it runs exactly as in
 * production: AuthInterceptor, token expiry, the refresh lock, logging. Only the
 * remote end is replaced: the request never leaves the device.
 *
 * Enabled with `fakestore.useMockBackend=true` in gradle.properties (debug builds only).
 */
class MockBackendInterceptor(
    private val loginDelayMillis: Long = 800,
    private val productsDelayMillis: Long = 300,
) : Interceptor {

    /** Number of login calls received: lets tests prove that a refresh happened only once. */
    val loginCount = AtomicInteger()

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val segments = request.url.pathSegments.filter { it.isNotEmpty() }

        val (code, body) = when {
            request.method == "POST" && segments == listOf("auth", "login") -> login(chain)
            request.method == "GET" && segments.firstOrNull() == "products" -> products(chain, segments)
            else -> 404 to "Not found"
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(body.toResponseBody(JSON))
            .build()
    }

    private fun login(chain: Interceptor.Chain): Pair<Int, String> {
        val number = loginCount.incrementAndGet()
        Thread.sleep(loginDelayMillis) // keeps a refresh "in flight" so concurrent requests must wait

        val buffer = Buffer()
        chain.request().body?.writeTo(buffer)
        val credentials = runCatching { gson.fromJson(buffer.readUtf8(), LoginRequest::class.java) }.getOrNull()

        return if (credentials?.username == USERNAME && credentials.password == PASSWORD) {
            200 to """{"token":"mock-token-$number"}"""
        } else {
            401 to "username or password is incorrect"
        }
    }

    private fun products(chain: Interceptor.Chain, segments: List<String>): Pair<Int, String> {
        // Unlike the real FakeStore, the mock really enforces authentication.
        val token = AuthHeaders.tokenFrom(chain.request().header(AuthHeaders.AUTHORIZATION))
        if (token.isNullOrBlank()) return 401 to "Missing bearer token"

        Thread.sleep(productsDelayMillis)
        if (segments.size == 1) return 200 to MockProducts.listJson()

        val id = segments[1].toIntOrNull()
        val product = id?.let { MockProducts.productJson(it) } ?: return 404 to "Product not found"
        return 200 to product
    }

    companion object {
        const val USERNAME = "mor_2314"
        const val PASSWORD = "83r5^_"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
