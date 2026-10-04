package com.example.postsapp.di

import com.example.postsapp.data.remote.PostApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt Module: "وصفة" لإنشاء كائنات لا نملك كودها (OkHttp و Retrofit من مكتبات خارجية)،
 * فلا نستطيع وضع @Inject على constructor لها.
 *
 * SingletonComponent + @Singleton: نسخة واحدة تعيش طوال عمر التطبيق.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            // يطبع الطلبات والردود في Logcat، وهذا مفيد جداً أثناء التطوير
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(PostApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // JSON ⇄ Kotlin objects
            .build()

    @Provides
    @Singleton
    fun providePostApi(retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)
}
