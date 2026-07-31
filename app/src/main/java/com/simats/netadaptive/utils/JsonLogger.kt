package com.simats.netadaptive.utils

import android.util.Log
import com.google.gson.Gson
import com.simats.netadaptive.data.model.NetworkMetrics
import com.simats.netadaptive.data.model.PredictionResult

object JsonLogger {
    private const val TAG = "NETADAPTIVE_AI"
    private val gson = Gson()

    fun logPrediction(metrics: NetworkMetrics, prediction: PredictionResult) {
        val logMap = mapOf(
            "timestamp" to metrics.timestamp,
            "rssi" to metrics.rssi,
            "currentLatency" to metrics.latency,
            "predictedLatency" to prediction.predictedLatency,
            "currentPacketLoss" to metrics.packetLoss,
            "predictedPacketLoss" to prediction.predictedPacketLoss,
            "qualityScore" to prediction.predictedQualityScore,
            "networkType" to metrics.networkType
        )
        Log.d(TAG, gson.toJson(logMap))
    }
}
