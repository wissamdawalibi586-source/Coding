package com.example.fakestore.ui.products

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakestore.data.auth.AuthManager
import com.example.fakestore.data.model.Product
import com.example.fakestore.data.repository.ProductRepository
import com.example.fakestore.ui.common.UiState
import com.example.fakestore.ui.common.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One-time events for the products screen (navigation, snackbars). */
sealed interface ProductsEvent {
    data object LoggedOut : ProductsEvent
    data class ConcurrencyTestFinished(val requestCount: Int) : ProductsEvent
    data class ShowError(@StringRes val messageRes: Int) : ProductsEvent
}

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val authManager: AuthManager,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Product>>> = _state.asStateFlow()

    // Channel: each event is delivered once, even if the screen is rotated.
    private val _events = Channel<ProductsEvent>(Channel.BUFFERED)
    val events: Flow<ProductsEvent> = _events.receiveAsFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getProducts())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e.toMessageRes())
            }
        }
    }

    /**
     * Demo for the reviewer: fires [CONCURRENT_REQUESTS] product requests at the same time.
     * Wait > 60 s after login, run it, and Logcat shows a single `/auth/login`.
     */
    fun runConcurrencyTest() {
        viewModelScope.launch {
            val event = try {
                coroutineScope {
                    (1..CONCURRENT_REQUESTS)
                        .map { id -> async { repository.getProduct(id) } }
                        .awaitAll()
                }
                ProductsEvent.ConcurrencyTestFinished(CONCURRENT_REQUESTS)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ProductsEvent.ShowError(e.toMessageRes())
            }
            _events.send(event)
        }
    }

    fun logout() {
        authManager.logout()
        viewModelScope.launch { _events.send(ProductsEvent.LoggedOut) }
    }

    private companion object {
        const val CONCURRENT_REQUESTS = 5
    }
}
