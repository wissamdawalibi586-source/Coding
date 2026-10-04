package com.example.fakestore

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fakestore.data.auth.AuthEvent
import com.example.fakestore.data.auth.AuthManager
import com.example.fakestore.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single activity hosting all screens (fragments). It also listens for forced logouts,
 * because they can happen on any screen.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authManager: AuthManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController

        // Start on the product list if a session exists (an expired token is refreshed
        // automatically on the first request), otherwise on the login screen.
        val graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
            setStartDestination(
                if (authManager.isLoggedIn()) R.id.productsFragment else R.id.loginFragment,
            )
        }
        navController.setGraph(graph, null)

        // Login and Products are top-level: no "back" arrow on them.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.loginFragment, R.id.productsFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        observeSession()
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    private fun observeSession() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // The session may have been cleared while the app was in the background.
                if (!authManager.isLoggedIn()) goToLogin(showMessage = false)

                authManager.events.collect { event ->
                    when (event) {
                        AuthEvent.SessionExpired -> goToLogin(showMessage = true)
                    }
                }
            }
        }
    }

    private fun goToLogin(showMessage: Boolean) {
        if (navController.currentDestination?.id == R.id.loginFragment) return
        if (showMessage) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_LONG).show()
        }
        navController.navigate(R.id.action_global_login)
    }
}
