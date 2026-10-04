package com.example.postsapp.data.repository

import com.example.postsapp.data.model.Post
import com.example.postsapp.data.remote.PostApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository: المصدر الوحيد للبيانات (Single Source of Truth) بالنسبة للـ ViewModels.
 *
 * هو interface حتى:
 *  1. لا يعرف الـ ViewModel هل البيانات من الإنترنت أم من Room أم من cache.
 *  2. نستبدله في الاختبارات بنسخة وهمية (Fake) دون أي شبكة.
 */
interface PostRepository {
    suspend fun getPosts(): List<Post>
    suspend fun getPost(id: Int): Post
}

/**
 * التنفيذ الحقيقي الذي يستخدم Retrofit.
 * @Inject constructor: يخبر Hilt كيف ينشئ هذا الكلاس، ويعطيه [PostApi] تلقائياً.
 */
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: PostApi,
) : PostRepository {

    override suspend fun getPosts(): List<Post> = api.getPosts()

    override suspend fun getPost(id: Int): Post = api.getPost(id)
}
