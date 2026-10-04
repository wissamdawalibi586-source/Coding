package com.example.fakestore.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakestore.data.model.Product
import com.example.fakestore.data.repository.ProductRepository
import com.example.fakestore.ui.common.UiState
import com.example.fakestore.ui.common.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
) : ViewModel() {

    // The navigation argument arrives through SavedStateHandle and survives process death.
    private val productId: Int = checkNotNull(savedStateHandle[ARG_PRODUCT_ID]) {
        "ProductDetailViewModel requires the '$ARG_PRODUCT_ID' argument"
    }

    private val _state = MutableStateFlow<UiState<Product>>(UiState.Loading)
    val state: StateFlow<UiState<Product>> = _state.asStateFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = try {
                UiState.Success(repository.getProduct(productId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e.toMessageRes())
            }
        }
    }

    companion object {
        /** Must match the `<argument android:name>` in nav_graph.xml. */
        const val ARG_PRODUCT_ID = "productId"
    }
}
