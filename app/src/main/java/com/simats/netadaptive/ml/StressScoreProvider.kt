package com.simats.netadaptive.ml

import com.simats.netadaptive.data.PredictionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StressScoreProvider @Inject constructor() {
    fun getCurrentScore(): Float {
        val prediction = PredictionRepository.latestPrediction.value
        val qualityScore = prediction?.predictedQualityScore ?: 85
        // Stress score: 0.0 (no stress) to 1.0 (max stress)
        // High quality (100) -> 0.0 stress
        // Low quality (0) -> 1.0 stress
        return (100f - qualityScore.toFloat()).coerceIn(0f, 100f) / 100f
    }
}
