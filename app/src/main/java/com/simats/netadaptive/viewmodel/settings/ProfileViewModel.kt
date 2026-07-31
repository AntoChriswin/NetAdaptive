package com.simats.netadaptive.viewmodel.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.simats.netadaptive.R
import com.simats.netadaptive.utils.DeviceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "User",
    val email: String = "",
    val photoUrl: String? = null,
    val deviceName: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val androidVersion: String = "",
    val apiLevel: String = "",
    val deviceId: String = "",
    val appVersion: String = "",
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            loadProfileData(user)
        } else {
            _uiState.update { it.copy(isLoading = false, isLoggedOut = true) }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private fun loadProfileData(user: FirebaseUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isLoggedOut = false) }
            val context = getApplication<Application>().applicationContext
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = getDisplayName(user),
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    deviceName = DeviceUtils.getDeviceName(),
                    manufacturer = DeviceUtils.getManufacturer(),
                    model = DeviceUtils.getModel(),
                    androidVersion = DeviceUtils.getAndroidVersion(),
                    apiLevel = DeviceUtils.getApiLevel(),
                    deviceId = DeviceUtils.getDeviceId(context),
                    appVersion = DeviceUtils.getAppVersion(context),
                    error = null
                )
            }
        }
    }

    private fun getDisplayName(user: FirebaseUser): String {
        // Case 1: Google Sign-In or manual name set
        val name = user.displayName
        if (!name.isNullOrBlank()) return name

        // Case 2: Email fallback
        val email = user.email
        if (!email.isNullOrBlank() && email.contains("@")) {
            val prefix = email.substringBefore("@")
            val result = if (prefix.length >= 5) {
                prefix.substring(0, 5)
            } else {
                prefix
            }
            return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        return "User"
    }
    
    fun logout() {
        viewModelScope.launch {
            // Sign out from Firebase
            auth.signOut()
            
            // Sign out from Google to allow account switching
            val context = getApplication<Application>().applicationContext
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    _uiState.update { ProfileUiState(isLoading = false, isLoggedOut = true) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
