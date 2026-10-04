package com.example.postsapp.data.remote

import com.example.postsapp.data.model.Post
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * تعريف الـ endpoints. نكتب interface فقط، وRetrofit يولّد الكود الفعلي للطلبات.
 *
 * suspend: الدالة تعمل داخل coroutine، فلا يتجمّد الـ Main thread أثناء انتظار الشبكة.
 */
interface PostApi {

    /** GET https://jsonplaceholder.typicode.com/posts */
    @GET("posts")
    suspend fun getPosts(): List<Post>

    /** GET https://jsonplaceholder.typicode.com/posts/{id} */
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    companion object {
        const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }
}
