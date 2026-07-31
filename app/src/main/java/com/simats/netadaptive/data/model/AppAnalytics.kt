package com.simats.netadaptive.data.model

import com.google.firebase.Timestamp

data class AppAnalytics(
    val appPackageName: String = "",
    val appName: String = "",
    val totalUsageMB: Double = 0.0,
    val foregroundUsageMB: Double = 0.0,
    val backgroundUsageMB: Double = 0.0,
    val tier: String = "",
    val lastUpdated: Timestamp = Timestamp.now()
)

data class DayAnalytics(
    val date: String = "",
    val lastUpdated: Timestamp = Timestamp.now()
)
