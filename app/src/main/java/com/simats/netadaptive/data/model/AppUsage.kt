package com.simats.netadaptive.data.model

import com.google.firebase.Timestamp

data class AppUsage(
    val appName: String = "",
    val packageName: String = "",
    val totalUsageMB: Double = 0.0,
    val foregroundUsageMB: Double = 0.0,
    val backgroundUsageMB: Double = 0.0,
    val tier: String = "",
    val lastUpdated: Timestamp = Timestamp.now()
)

data class TopAppsUsage(
    val lastUpdated: Timestamp = Timestamp.now(),
    val topApps: List<AppUsage> = emptyList()
)
