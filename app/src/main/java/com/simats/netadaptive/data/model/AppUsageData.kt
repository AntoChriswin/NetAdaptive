package com.simats.netadaptive.data.model

enum class PriorityLevel {
    HIGH, MEDIUM, LOW
}

data class AppUsageData(
    val packageName: String,
    val name: String,
    val usageBytes: Long,
    val usageDisplay: String,
    val currentSpeed: String = "0 B/s",
    val currentSpeedBytes: Long = 0,
    val status: String? = null,
    val category: String = "General",
    val priority: PriorityLevel = PriorityLevel.LOW,
    val isError: Boolean = false,
    val isThrottled: Boolean = false,
    val isDelayed: Boolean = false,
    val isItalic: Boolean = false,
    val priorityScore: Int = 3,
    val uid: Int = 0,
    val fgUsageBytes: Long = 0,
    val bgUsageBytes: Long = 0,
    val wifiUsageBytes: Long = 0,
    val mobileUsageBytes: Long = 0
)
