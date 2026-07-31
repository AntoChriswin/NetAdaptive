package com.simats.netadaptive.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Production analytics snapshot model for Firestore.
 */
data class AnalyticsSnapshot(
    val dailyUsageMB: Double = 0.0,
    val dailyWifiUsageMB: Double = 0.0,
    val dailyMobileUsageMB: Double = 0.0,
    
    val weeklyUsageMB: Double = 0.0,
    val weeklyWifiUsageMB: Double = 0.0,
    val weeklyMobileUsageMB: Double = 0.0,
    
    val totalForegroundUsageMB: Double = 0.0,
    val totalBackgroundUsageMB: Double = 0.0,
    val foregroundUsagePercentage: Double = 0.0,
    val backgroundUsagePercentage: Double = 0.0,

    val top5PeakDays: List<PeakUsageDay> = emptyList(),
    val avgLatencyMs: Double = 0.0,
    val avgPacketLossPercent: Double = 0.0,
    @ServerTimestamp val lastUpdated: Date? = null
)
