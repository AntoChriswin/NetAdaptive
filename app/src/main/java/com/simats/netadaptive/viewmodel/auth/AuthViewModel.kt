package com.simats.netadaptive.viewmodel.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.simats.netadaptive.R
import com.simats.netadaptive.core.Resource
import com.simats.netadaptive.data.firebase.FirestoreManager
import com.simats.netadaptive.data.model.User
import com.simats.netadaptive.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application, private val repository: AuthRepository) : AndroidViewModel(application) {

    private val _authActionState = MutableLiveData<Resource<User>>()
    val authActionState: LiveData<Resource<User>> = _authActionState

    private val _resetPasswordState = MutableLiveData<Resource<Unit>>()
    val resetPasswordState: LiveData<Resource<Unit>> = _resetPasswordState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            _currentUser.value = repository.getCurrentUser()
        } else {
            _currentUser.value = null
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        _currentUser.value = repository.getCurrentUser()
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authActionState.value = Resource.Loading
            val result = repository.signInWithEmail(email, password)
            _authActionState.value = result
            if (result is Resource.Success) {
                _currentUser.value = result.data
                FirestoreManager().testFirestoreConnection()
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authActionState.value = Resource.Loading
            val result = repository.signUpWithEmail(email, password)
            _authActionState.value = result
            if (result is Resource.Success) {
                _currentUser.value = result.data
                FirestoreManager().testFirestoreConnection()
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _authActionState.value = Resource.Loading
            val result = repository.signInWithGoogle(credential)
            _authActionState.value = result
            if (result is Resource.Success) {
                _currentUser.value = result.data
                FirestoreManager().testFirestoreConnection()
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading
            _resetPasswordState.value = repository.resetPassword(email)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            
            val context = getApplication<Application>().applicationContext
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    _currentUser.value = null
                    _authActionState.value = Resource.Error("Logged out") // Reset action state
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}
