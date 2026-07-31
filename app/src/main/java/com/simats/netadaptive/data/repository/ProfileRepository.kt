package com.simats.netadaptive.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.simats.netadaptive.data.model.UserProfile
import kotlinx.coroutines.tasks.await

object ProfileRepository {
    private const val TAG = "ProfileRepository"

    suspend fun syncProfile(user: FirebaseUser) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = user.uid
        Log.i(TAG, "syncProfile: STARTING sync for user $uid")
        
        try {
            val authProvider = if (user.providerData.any { it.providerId == "google.com" }) "Google" else "Email"
            val username = getDisplayName(user)
            val email = user.email ?: ""
            val photoUrl = user.photoUrl?.toString() ?: ""
            
            val creationTime = user.metadata?.creationTimestamp?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now()
            val lastLoginTime = user.metadata?.lastSignInTimestamp?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now()

            // 1. Prepare Profile Object
            val profile = UserProfile(
                uid = uid,
                username = username,
                email = email,
                photoUrl = photoUrl,
                authProvider = authProvider,
                accountCreatedAt = creationTime,
                lastLoginAt = lastLoginTime,
                profileUpdatedAt = Timestamp.now()
            )

            // 2. Write to users/{uid}/profile/profile (Sub-collection document)
            val subDocRef = firestore.collection("users")
                .document(uid)
                .collection("profile")
                .document("profile")
            
            Log.d(TAG, "syncProfile: Writing to ${subDocRef.path}")
            subDocRef.set(profile, SetOptions.merge()).await()

            // 3. ALSO write to users/{uid} directly (Top-level fields for visibility)
            // This ensures the user document exists even if they are only looking for a 'profile' field
            val userDocRef = firestore.collection("users").document(uid)
            val topLevelData = mapOf(
                "uid" to uid,
                "email" to email,
                "lastUpdated" to Timestamp.now(),
                "profile" to profile // Nesting it as a map as well for compatibility
            )
            
            Log.d(TAG, "syncProfile: Writing top-level data to ${userDocRef.path}")
            userDocRef.set(topLevelData, SetOptions.merge()).await()

            Log.i(TAG, "syncProfile: SUCCESSFUL sync for $uid")
        } catch (e: Exception) {
            Log.e(TAG, "syncProfile: FAILED for $uid. Reason: ${e.message}", e)
        }
    }

    private fun getDisplayName(user: FirebaseUser): String {
        user.displayName?.let { if (it.isNotEmpty()) return it }
        val email = user.email ?: return "User"
        val prefix = email.split("@").firstOrNull() ?: return "User"
        return prefix.replaceFirstChar { it.uppercase() }
    }
}
