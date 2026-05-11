package com.simats.netadaptive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.simats.netadaptive.R
import com.simats.netadaptive.core.Resource
import com.simats.netadaptive.data.repository.AuthRepository
import com.simats.netadaptive.databinding.ActivityLoginBinding
import com.simats.netadaptive.ui.dashboard.DashboardActivity
import com.simats.netadaptive.viewmodel.auth.AuthViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupGoogleSignIn()
        observeViewModel()

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // Assuming you added these IDs to activity_login.xml
        // If not, I will update activity_login.xml next
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.signInWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                viewModel.signInWithGoogle(credential)
            } catch (e: ApiException) {
                showError("Google sign in failed: ${e.message}")
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                navigateToDashboard()
            }
        }

        viewModel.authActionState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setButtonsEnabled(false)
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    navigateToDashboard()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    setButtonsEnabled(true)
                    showError(resource.message)
                }
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        binding.btnGoogleSignIn.isEnabled = enabled
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
