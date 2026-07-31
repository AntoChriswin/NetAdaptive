package com.simats.netadaptive.ml

import kotlin.math.max
import kotlin.math.min

object QualityScoreCalculator {

    fun calculate(rssi: Int, latency: Float, packetLoss: Float): Int {
        val signalScore = calculateSignalScore(rssi)
        val latencyScore = calculateLatencyScore(latency)
        val packetScore = calculatePacketLossScore(packetLoss)

        val quality = (signalScore * 0.4f) + (latencyScore * 0.35f) + (packetScore * 0.25f)
        return min(100, max(0, quality.toInt()))
    }

    private fun calculateSignalScore(rssi: Int): Float {
        // -100 dBm is 0, -30 dBm is 100
        return when {
            rssi <= -100 -> 0f
            rssi >= -30 -> 100f
            else -> (rssi + 100) * (100f / 70f)
        }
    }

    private fun calculateLatencyScore(latency: Float): Float {
        // 0ms is 100, 200ms or more is 0
        return when {
            latency <= 0 -> 100f
            latency >= 200 -> 0f
            else -> 100f - (latency * (100f / 200f))
        }
    }

    private fun calculatePacketLossScore(packetLoss: Float): Float {
        // 0% is 100, 10% or more is 0
        return when {
            packetLoss <= 0 -> 100f
            packetLoss >= 10 -> 0f
            else -> 100f - (packetLoss * 10f)
        }
    }
}
