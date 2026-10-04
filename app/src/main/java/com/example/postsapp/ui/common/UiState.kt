package com.example.postsapp.ui.common

import retrofit2.HttpException
import java.io.IOException

/**
 * كل الحالات الممكنة لأي شاشة تحمّل بيانات.
 *
 * sealed: مجموعة مغلقة من الأنواع، فيُجبرنا `when` على معالجة الحالات الثلاث كلها،
 * ولن ننسى حالة Error مثلاً.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/** يحوّل الـ exception التقني إلى رسالة مفهومة للمستخدم. */
fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "No internet connection. Check your network and try again."
    is HttpException -> "Server error (${code()}). Please try again later."
    else -> message ?: "Something went wrong."
}
