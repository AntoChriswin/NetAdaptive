package com.simats.netadaptive.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.simats.netadaptive.core.Resource
import com.simats.netadaptive.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let {
            User(it.uid, getDisplayName(it), it.email, it.photoUrl?.toString())
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.let {
                ProfileRepository.syncProfile(it)
                User(it.uid, getDisplayName(it), it.email, it.photoUrl?.toString())
            }
            if (user != null) Resource.Success(user) else Resource.Error("Login failed")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login error")
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user?.let {
                ProfileRepository.syncProfile(it)
                User(it.uid, getDisplayName(it), it.email, it.photoUrl?.toString())
            }
            if (user != null) Resource.Success(user) else Resource.Error("Registration failed")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration error")
        }
    }

    suspend fun signInWithGoogle(credential: AuthCredential): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.let {
                ProfileRepository.syncProfile(it)
                User(it.uid, getDisplayName(it), it.email, it.photoUrl?.toString())
            }
            if (user != null) Resource.Success(user) else Resource.Error("Google sign in failed")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Google sign in error")
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Reset password error")
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    private fun getDisplayName(user: FirebaseUser): String {
        user.displayName?.let { if (it.isNotEmpty()) return it }
        val email = user.email ?: return "User"
        val prefix = email.split("@").firstOrNull() ?: return "User"
        val name = if (prefix.length >= 5) prefix.substring(0, 5) else prefix
        return name.replaceFirstChar { it.uppercase() }
    }
}
