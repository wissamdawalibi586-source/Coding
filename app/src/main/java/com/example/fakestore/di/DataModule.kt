package com.example.fakestore.di

import com.example.fakestore.data.auth.Clock
import com.example.fakestore.data.auth.EncryptedTokenStorage
import com.example.fakestore.data.auth.SystemTimeClock
import com.example.fakestore.data.auth.TokenStorage
import com.example.fakestore.data.repository.ProductRepository
import com.example.fakestore.data.repository.ProductRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds each interface to its production implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindTokenStorage(impl: EncryptedTokenStorage): TokenStorage

    @Binds
    abstract fun bindClock(impl: SystemTimeClock): Clock

    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}
