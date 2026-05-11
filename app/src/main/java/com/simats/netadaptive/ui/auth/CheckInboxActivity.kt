package com.simats.netadaptive.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.simats.netadaptive.core.Resource
import com.simats.netadaptive.data.repository.AuthRepository
import com.simats.netadaptive.databinding.ActivityCheckInboxBinding
import com.simats.netadaptive.viewmodel.auth.AuthViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModelFactory

class CheckInboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckInboxBinding
    private lateinit var viewModel: AuthViewModel
    private var resendTimer: CountDownTimer? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("EMAIL")

        val repository = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupUI()
        startTimer()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnOpenEmailApp.setOnClickListener {
            openEmailApp()
        }

        binding.btnBackToLogin.setOnClickListener {
            navigateToLogin()
        }

        binding.tvResendAction.setOnClickListener {
            email?.let {
                viewModel.resetPassword(it)
                startTimer()
            }
        }
    }

    private fun openEmailApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        binding.tvResendTimer.visibility = View.VISIBLE
        binding.tvResendAction.visibility = View.GONE

        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(45000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvResendTimer.text = "Resend in 0:${String.format("%02d", secondsRemaining)}"
            }

            override fun onFinish() {
                binding.tvResendTimer.visibility = View.GONE
                binding.tvResendAction.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun observeViewModel() {
        viewModel.resetPasswordState.observe(this) { resource ->
            if (resource is Resource.Success) {
                Toast.makeText(this, "Reset link resent", Toast.LENGTH_SHORT).show()
            } else if (resource is Resource.Error) {
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}