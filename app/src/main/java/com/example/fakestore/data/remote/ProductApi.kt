package com.example.fakestore.data.remote

import com.example.fakestore.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Protected product endpoints. Built on the authenticated OkHttp client, so every call
 * automatically carries `Authorization: Bearer <token>`.
 */
interface ProductApi {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto
}
