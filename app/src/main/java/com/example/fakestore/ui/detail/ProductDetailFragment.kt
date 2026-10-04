package com.example.fakestore.ui.detail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.fakestore.R
import com.example.fakestore.data.model.Product
import com.example.fakestore.databinding.FragmentProductDetailBinding
import com.example.fakestore.ui.common.UiState
import com.example.fakestore.ui.common.collectWhenStarted
import com.example.fakestore.ui.common.formatPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val viewModel: ProductDetailViewModel by viewModels()

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductDetailBinding.bind(view)

        binding.errorView.retryButton.setOnClickListener { viewModel.loadProduct() }
        collectWhenStarted(viewModel.state) { render(it) }
    }

    private fun render(state: UiState<Product>) {
        binding.progress.isVisible = state is UiState.Loading
        binding.content.isVisible = state is UiState.Success
        binding.errorView.root.isVisible = state is UiState.Error

        when (state) {
            is UiState.Success -> showProduct(state.data)
            is UiState.Error -> binding.errorView.errorMessage.setText(state.messageRes)
            UiState.Loading -> Unit
        }
    }

    private fun showProduct(product: Product) {
        binding.image.load(product.imageUrl) { crossfade(true) }
        binding.title.text = product.title
        binding.price.text = product.price.formatPrice()
        binding.category.text = product.category
        binding.rating.text = getString(R.string.product_rating, product.rating, product.ratingCount)
        binding.description.text = product.description
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
