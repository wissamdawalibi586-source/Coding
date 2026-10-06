package com.example.fakestore.ui.login

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.fakestore.BuildConfig
import com.example.fakestore.R
import com.example.fakestore.databinding.FragmentLoginBinding
import com.example.fakestore.ui.common.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        binding.mockBackendBanner.isVisible = BuildConfig.USE_MOCK_BACKEND

        binding.loginButton.setOnClickListener { submit() }
        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        collectWhenStarted(viewModel.state) { render(it) }
    }

    private fun submit() {
        viewModel.login(
            username = binding.usernameInput.text?.toString().orEmpty(),
            password = binding.passwordInput.text?.toString().orEmpty(),
        )
    }

    private fun render(state: LoginUiState) {
        binding.progress.isVisible = state.isLoading
        binding.loginButton.isEnabled = !state.isLoading
        binding.usernameLayout.isEnabled = !state.isLoading
        binding.passwordLayout.isEnabled = !state.isLoading

        binding.errorText.isVisible = state.errorRes != null
        state.errorRes?.let { binding.errorText.setText(it) }

        val navController = findNavController()
        if (state.isLoggedIn && navController.currentDestination?.id == R.id.loginFragment) {
            navController.navigate(R.id.action_login_to_products)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
