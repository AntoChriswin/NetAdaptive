package com.simats.netadaptive.ml

import android.content.Context
import android.util.Log
import com.simats.netadaptive.data.model.NetworkMetrics
import com.simats.netadaptive.data.model.PredictionResult

/**
 * Engine that processes network metrics and generates predictions using TFLite with Flex support.
 */
class PredictionEngine(private val context: Context) {
    private val tag = "PredictionEngine"
    private val tfliteManager = TFLiteManager(context)
    private val scalerManager = ScalerManager(context)
    private val sequenceBuffer = SequenceBuffer(10)

    init {
        scalerManager.loadScaler()
    }

    /**
     * Processes network metrics and returns a prediction result.
     * Input shape: [1, 10, 8]
     * Returns a PredictionResult (data class) with predicted latency and packet loss.
     */
    fun process(metrics: NetworkMetrics): PredictionResult? {
        return try {
            sequenceBuffer.addMetrics(metrics)
            
            if (!sequenceBuffer.isReady()) return null
            
            val sequence = sequenceBuffer.getSequence() ?: return null
            
            val interpreter = tfliteManager.getInterpreter()
            if (interpreter == null) {
                Log.e(tag, "Inference skipped: Interpreter is NULL")
                return null
            }
            
            // Prepare input: shape [1, 10, 8]
            val inputArray = Array(1) { Array(10) { FloatArray(8) } }
            for (i in sequence.indices) {
                inputArray[0][i] = scalerManager.normalize(sequence[i])
            }
            
            // Prepare output map for 2 outputs: latency and packet loss
            // Latency output [1, 1], Packet loss output [1, 1]
            val latencyOutput = Array(1) { FloatArray(1) }
            val packetLossOutput = Array(1) { FloatArray(1) }
            
            val inputs = arrayOf<Any>(inputArray)
            val outputs = mutableMapOf<Int, Any>()
            outputs[0] = latencyOutput
            outputs[1] = packetLossOutput
            
            // Run inference for multiple outputs
            interpreter.runForMultipleInputsOutputs(inputs, outputs)
            
            // Denormalize results
            val predictedLatency = scalerManager.denormalizeLatency(latencyOutput[0][0])
            val predictedPacketLoss = scalerManager.denormalizePacketLoss(packetLossOutput[0][0])
            
            // Calculate quality score
            val qualityScore = QualityScoreCalculator.calculate(
                metrics.rssi,
                predictedLatency,
                predictedPacketLoss
            )
            
            PredictionResult(
                currentLatency = metrics.latency,
                predictedLatency = predictedLatency,
                currentPacketLoss = metrics.packetLoss,
                predictedPacketLoss = predictedPacketLoss,
                predictedQualityScore = qualityScore
            )
        } catch (e: Exception) {
            Log.e(tag, "Inference error in process(): ${e.message}", e)
            null
        }
    }

    /**
     * Cleans up resources.
     */
    fun destroy() {
        tfliteManager.close()
    }
}
