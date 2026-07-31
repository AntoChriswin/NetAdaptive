package com.simats.netadaptive.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val authProvider: String = "",
    val accountCreatedAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp = Timestamp.now(),
    val profileUpdatedAt: Timestamp = Timestamp.now()
)
