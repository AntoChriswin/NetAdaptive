package com.simats.netadaptive.engine.model

enum class AppState {
    FOREGROUND, BACKGROUND, CLOSED
}

data class AppSessionMetrics(
    val appName: String,
    val packageName: String,
    val uid: Int,
    var sessionUsageMB: Float = 0f,
    var rxBytes: Long = 0,
    var txBytes: Long = 0,
    var sessionBaselineBytes: Long = 0,
    var currentBandwidthMBps: Float = 0f,
    var avgBandwidth5minMBps: Float = 0f,
    var peakBandwidthMBps: Float = 0f,
    var foregroundDurationMs: Long = 0,
    var backgroundDurationMs: Long = 0,
    var totalActiveDurationMs: Long = 0,
    val openTimestamp: Long = System.currentTimeMillis(),
    var lastSeenTimestamp: Long = System.currentTimeMillis(),
    var state: AppState = AppState.CLOSED,
    var interactionScore: Float = 0f,
    var networkContribution: Float = 0f
)

data class AppUsageWindow(
    val windowStartMs: Long,
    val windowEndMs: Long,
    val predictedLatency: Float,
    val predictedPacketLoss: Float,
    val apps: List<AppWindowEntry>
)

data class AppWindowEntry(
    val packageName: String,
    val appName: String,
    val usageMB: Float,
    val foregroundMs: Long,
    val backgroundMs: Long,
    val avgBandwidth: Float,
    val peakBandwidth: Float,
    val interactionScore: Float,
    val networkContribution: Float,
    val state: AppState
)
