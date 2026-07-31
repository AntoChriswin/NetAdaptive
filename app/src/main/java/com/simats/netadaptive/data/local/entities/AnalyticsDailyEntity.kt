package com.simats.netadaptive.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_daily")
data class AnalyticsDailyEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val totalDataSavedBytes: Long,
    val spikesAvoided: Int,
    val avgQualityScore: Float,
    val uptimeMs: Long,
    val totalPacketsProcessed: Long,
    val totalPacketsOptimized: Long,
    val totalPacketsDropped: Long,
    val dnsRequestsHandled: Int
)
