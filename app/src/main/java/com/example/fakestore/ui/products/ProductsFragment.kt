package com.example.fakestore.ui.products

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.fakestore.R
import com.example.fakestore.data.model.Product
import com.example.fakestore.databinding.FragmentProductsBinding
import com.example.fakestore.ui.common.UiState
import com.example.fakestore.ui.common.collectWhenStarted
import com.example.fakestore.ui.detail.ProductDetailViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsFragment : Fragment(R.layout.fragment_products) {

    private val viewModel: ProductsViewModel by viewModels()

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private val adapter = ProductAdapter { product ->
        findNavController().navigate(
            R.id.action_products_to_detail,
            bundleOf(ProductDetailViewModel.ARG_PRODUCT_ID to product.id),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductsBinding.bind(view)

        binding.productsList.adapter = adapter
        binding.errorView.retryButton.setOnClickListener { viewModel.loadProducts() }
        setUpMenu()

        collectWhenStarted(viewModel.state) { render(it) }
        collectWhenStarted(viewModel.events) { handle(it) }
    }

    private fun render(state: UiState<List<Product>>) {
        binding.progress.isVisible = state is UiState.Loading
        binding.productsList.isVisible = state is UiState.Success
        binding.errorView.root.isVisible = state is UiState.Error

        when (state) {
            is UiState.Success -> adapter.submitList(state.data)
            is UiState.Error -> binding.errorView.errorMessage.setText(state.messageRes)
            UiState.Loading -> Unit
        }
    }

    private fun handle(event: ProductsEvent) {
        when (event) {
            ProductsEvent.LoggedOut -> findNavController().navigate(R.id.action_global_login)
            is ProductsEvent.ConcurrencyTestFinished -> Snackbar.make(
                binding.root,
                getString(R.string.concurrency_test_done, event.requestCount),
                Snackbar.LENGTH_LONG,
            ).show()
            is ProductsEvent.ShowError -> Snackbar.make(
                binding.root, event.messageRes, Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun setUpMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_products, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_refresh -> {
                    viewModel.loadProducts()
                    true
                }
                R.id.action_concurrency_test -> {
                    viewModel.runConcurrencyTest()
                    true
                }
                R.id.action_logout -> {
                    viewModel.logout()
                    true
                }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.productsList.adapter = null // the adapter outlives the view: avoid leaking it
        _binding = null
    }
}
