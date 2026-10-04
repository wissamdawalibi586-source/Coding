package com.example.fakestore.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakestore.R
import com.example.fakestore.data.auth.AuthManager
import com.example.fakestore.ui.common.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        if (_state.value.isLoading) return // ignore double taps

        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty() || password.isEmpty()) {
            _state.update { it.copy(errorRes = R.string.error_empty_fields) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorRes = null) }
            try {
                authManager.login(trimmedUsername, password)
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorRes = e.toMessageRes()) }
            }
        }
    }
}
