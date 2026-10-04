package com.example.fakestore.data.model

/**
 * Product as the UI layer sees it: non-null, already cleaned up from the network DTO.
 */
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUrl: String,
    val rating: Double,
    val ratingCount: Int,
)
