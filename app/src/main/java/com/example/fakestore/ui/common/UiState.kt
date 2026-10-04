package com.example.fakestore.ui.common

import androidx.annotation.StringRes

/** State of a screen that loads data: exactly one of these at a time. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(@StringRes val messageRes: Int) : UiState<Nothing>
}
