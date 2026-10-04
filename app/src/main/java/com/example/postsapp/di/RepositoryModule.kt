package com.example.postsapp.di

import com.example.postsapp.data.repository.PostRepository
import com.example.postsapp.data.repository.PostRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * يربط الـ interface بالتنفيذ: كلما طلب أحد [PostRepository] يعطيه Hilt [PostRepositoryImpl].
 * @Binds أخف من @Provides لأنه لا يحتاج كتابة كود إنشاء.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}
