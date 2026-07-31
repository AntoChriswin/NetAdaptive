package com.simats.netadaptive.data.model

/**
 * Data model for a single peak usage day.
 */
data class PeakUsageDay(
    val date: String,    // Format: YYYY-MM-DD
    val usageMB: Long
)
