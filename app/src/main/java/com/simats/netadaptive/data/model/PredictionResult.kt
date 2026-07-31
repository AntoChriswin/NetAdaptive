package com.simats.netadaptive.data.model

data class PredictionResult(
    val currentLatency: Float,
    val predictedLatency: Float,
    val currentPacketLoss: Float,
    val predictedPacketLoss: Float,
    val predictedQualityScore: Int
)
