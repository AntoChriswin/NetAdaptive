package com.simats.netadaptive.data.model

data class NetworkMetrics(
    val rssi: Int,
    val latency: Float,
    val packetLoss: Float,
    val jitter: Float,
    val downloadSpeed: Float,
    val uploadSpeed: Float,
    val networkType: String,
    val frequencyBand: String,
    val ssid: String? = null,
    val ipAddress: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
