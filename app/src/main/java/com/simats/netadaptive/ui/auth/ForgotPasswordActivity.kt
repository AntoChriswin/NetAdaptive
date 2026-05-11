package com.simats.netadaptive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.simats.netadaptive.core.Resource
import com.simats.netadaptive.data.repository.AuthRepository
import com.simats.netadaptive.databinding.ActivityForgotPasswordBinding
import com.simats.netadaptive.viewmodel.auth.AuthViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModelFactory

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSendResetLink.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (validateEmail(email)) {
                viewModel.resetPassword(email)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            return false
        }
        binding.tilEmail.error = null
        return true
    }

    private fun observeViewModel() {
        viewModel.resetPasswordState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    val email = binding.etEmail.text.toString().trim()
                    val intent = Intent(this, CheckInboxActivity::class.java).apply {
                        putExtra("EMAIL", email)
                    }
                    startActivity(intent)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSendResetLink.isEnabled = !isLoading
    }
}