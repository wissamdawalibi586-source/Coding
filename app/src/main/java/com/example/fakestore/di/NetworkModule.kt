package com.example.fakestore.di

import com.example.fakestore.BuildConfig
import com.example.fakestore.data.auth.AuthHeaders
import com.example.fakestore.data.auth.AuthInterceptor
import com.example.fakestore.data.auth.TokenAuthenticator
import com.example.fakestore.data.mock.MockBackendInterceptor
import com.example.fakestore.data.remote.ApiConfig
import com.example.fakestore.data.remote.AuthApi
import com.example.fakestore.data.remote.ProductApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 20L

    /**
     * Dev-only fake server, shared by both clients. Null in normal builds: requests then
     * go to the real fakestoreapi.com. (Kept out of the Hilt graph on purpose: it is a
     * build-time switch, not a dependency anyone else needs.)
     */
    private val mockBackend: MockBackendInterceptor? =
        if (BuildConfig.USE_MOCK_BACKEND) MockBackendInterceptor() else null

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // BASIC prints one line per request (method + URL + status): enough to see
            // in Logcat that only one /auth/login runs. Never log bodies: they hold the password.
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader(AuthHeaders.AUTHORIZATION)
        }

    @Provides
    @Singleton
    @PlainClient
    fun providePlainClient(logging: HttpLoggingInterceptor): OkHttpClient =
        baseClientBuilder()
            .addInterceptor(logging)
            .addMockBackend(mockBackend)
            .build()

    @Provides
    @Singleton
    @AuthClient
    fun provideAuthClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor,
    ): OkHttpClient =
        baseClientBuilder()
            .addInterceptor(authInterceptor) // first: attach a valid token
            .addInterceptor(logging)         // then: log the final request
            .addMockBackend(mockBackend)     // last (dev only): answer instead of the server
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(@PlainClient client: OkHttpClient): AuthApi =
        buildRetrofit(client).create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProductApi(@AuthClient client: OkHttpClient): ProductApi =
        buildRetrofit(client).create(ProductApi::class.java)

    private fun baseClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    /** The mock must stay last: it answers the request instead of passing it on. */
    private fun OkHttpClient.Builder.addMockBackend(mock: MockBackendInterceptor?) =
        apply { if (mock != null) addInterceptor(mock) }

    private fun buildRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
