package com.simats.netadaptive.engine.prediction

import android.content.Context
import android.util.Log
import com.simats.netadaptive.data.model.NetworkMetrics
import com.simats.netadaptive.data.model.PredictionResult
import com.simats.netadaptive.ml.PredictionEngine

class PredictionManager(context: Context) {
    private val predictionEngine = PredictionEngine(context)

    fun getPrediction(metrics: NetworkMetrics): PredictionResult? {
        val result = predictionEngine.process(metrics)
        
        if (result != null) {
            // STEP 1 Print Requirement
            Log.d("NET_PREDICT", "Current Latency=${result.currentLatency}\nPredicted Latency=${result.predictedLatency}\nCurrent PL=${result.currentPacketLoss}\nPredicted PL=${result.predictedPacketLoss}")
        }
        
        return result
    }
}
