package com.simats.netadaptive.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.simats.netadaptive.data.repository.AuthRepository
import com.simats.netadaptive.ui.auth.LoginActivity
import com.simats.netadaptive.ui.settings.ProfileScreen
import com.simats.netadaptive.viewmodel.auth.AuthViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModelFactory

class DashboardActivity : ComponentActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf("dashboard") }
                    val user by viewModel.currentUser.observeAsState()

                    LaunchedEffect(user) {
                        if (user == null) {
                            navigateToLogin()
                        }
                    }

                    Crossfade(targetState = currentScreen, label = "DashboardTransition") { screen ->
                        when (screen) {
                            "dashboard" -> DashboardScreen(
                                onProfileClick = { currentScreen = "profile" }
                            )
                            "profile" -> ProfileScreen(
                                onBackClick = { currentScreen = "dashboard" },
                                onLogoutClick = {
                                    viewModel.logout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
