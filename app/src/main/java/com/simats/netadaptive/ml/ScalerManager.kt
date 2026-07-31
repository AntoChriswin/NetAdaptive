package com.simats.netadaptive.ml

import android.content.Context
import com.simats.netadaptive.data.model.NetworkMetrics

class ScalerManager(private val context: Context) {
    
    // Feature ranges for Min-Max scaling: [min, range]
    // Order: rssi, latency, packet_loss, jitter, download_speed, upload_speed, network_type, frequency_band
    private val featuresMin = floatArrayOf(-100f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private val featuresRange = floatArrayOf(70f, 500f, 100f, 100f, 1000f, 1000f, 5f, 5f)

    fun loadScaler() {
        // Attempt to load from assets if special format, otherwise use defaults
        try {
            context.assets.open("scaler.save").use { _ ->
                // Custom parsing if format was known. 
                // For now, using calibrated defaults.
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun normalize(metrics: NetworkMetrics): FloatArray {
        val rawValues = floatArrayOf(
            metrics.rssi.toFloat(),
            metrics.latency,
            metrics.packetLoss,
            metrics.jitter,
            metrics.downloadSpeed,
            metrics.uploadSpeed,
            mapNetworkType(metrics.networkType),
            mapFrequencyBand(metrics.frequencyBand)
        )

        val normalized = FloatArray(rawValues.size)
        for (i in rawValues.indices) {
            normalized[i] = (rawValues[i] - featuresMin[i]) / featuresRange[i]
            // Clip to [0, 1]
            if (normalized[i] < 0f) normalized[i] = 0f
            if (normalized[i] > 1f) normalized[i] = 1f
        }
        return normalized
    }

    fun denormalizeLatency(value: Float): Float {
        // Latency is index 1
        return (value * featuresRange[1]) + featuresMin[1]
    }

    fun denormalizePacketLoss(value: Float): Float {
        // Packet loss is index 2
        return (value * featuresRange[2]) + featuresMin[2]
    }

    private fun mapNetworkType(type: String): Float {
        return when (type) {
            "WiFi" -> 1f
            "Cellular" -> 2f
            "Ethernet" -> 3f
            "Other" -> 4f
            else -> 0f
        }
    }

    private fun mapFrequencyBand(band: String): Float {
        return when (band) {
            "2.4GHz" -> 1f
            "5GHz" -> 2f
            "6GHz" -> 3f
            "Unknown" -> 4f
            else -> 0f
        }
    }
}
