package com.example.fakestore.ui.common

import androidx.annotation.StringRes
import com.example.fakestore.R
import com.example.fakestore.data.auth.InvalidCredentialsException
import com.example.fakestore.data.auth.SessionExpiredException
import retrofit2.HttpException
import java.io.IOException

/** Maps any failure to a message the user can understand. Order matters: most specific first. */
@StringRes
fun Throwable.toMessageRes(): Int = when (this) {
    is InvalidCredentialsException -> R.string.error_invalid_credentials
    is SessionExpiredException -> R.string.error_session_expired
    is HttpException -> R.string.error_server
    is IOException -> R.string.error_network
    else -> R.string.error_unknown
}
