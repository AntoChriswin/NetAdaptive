package com.simats.netadaptive.vpn.models

import com.simats.netadaptive.ml.AppTier

data class VpnMetrics(
    val forwardedPackets: Long = 0L,
    val delayedPackets: Long = 0L,
    val blockedPackets: Long = 0L,
    val activeQueues: Int = 0,
    val currentStressScore: Float = 0f,
    val appTierUsage: Map<AppTier, Long> = emptyMap(),  // tier → packet count
    val timestampMs: Long = System.currentTimeMillis()
)
