package com.example.fakestore.data.remote.dto

import com.example.fakestore.data.model.Product

/**
 * Product exactly as returned by the API. Every field is nullable because the server
 * is outside our control; [toDomain] turns it into a safe [Product].
 */
data class ProductDto(
    val id: Int,
    val title: String?,
    val price: Double?,
    val description: String?,
    val category: String?,
    val image: String?,
    val rating: RatingDto?,
)

data class RatingDto(
    val rate: Double?,
    val count: Int?,
)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title.orEmpty(),
    price = price ?: 0.0,
    description = description.orEmpty(),
    category = category.orEmpty(),
    imageUrl = image.orEmpty(),
    rating = rating?.rate ?: 0.0,
    ratingCount = rating?.count ?: 0,
)
