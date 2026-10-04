package com.example.fakestore.data.repository

import com.example.fakestore.data.model.Product
import com.example.fakestore.data.remote.ProductApi
import com.example.fakestore.data.remote.dto.toDomain
import javax.inject.Inject

/** Single source of product data for the ViewModels. */
interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: Int): Product
}

class ProductRepositoryImpl @Inject constructor(
    private val api: ProductApi,
) : ProductRepository {

    override suspend fun getProducts(): List<Product> = api.getProducts().map { it.toDomain() }

    override suspend fun getProduct(id: Int): Product = api.getProduct(id).toDomain()
}
