package com.example.fakestore.di

import javax.inject.Qualifier

/** OkHttp client WITHOUT auth: used only for `/auth/login`. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlainClient

/** OkHttp client WITH AuthInterceptor + TokenAuthenticator: used for protected APIs. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient
