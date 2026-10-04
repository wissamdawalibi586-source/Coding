package com.example.postsapp.data.model

/**
 * Model: شكل البيانات القادمة من الـ API.
 *
 * أسماء الحقول تطابق مفاتيح JSON تماماً، فيستطيع Gson التحويل تلقائياً:
 * { "userId": 1, "id": 1, "title": "...", "body": "..." }
 *
 * data class تولّد equals و hashCode و toString و copy تلقائياً.
 */
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
)
