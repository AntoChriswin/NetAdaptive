package com.simats.netadaptive.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Manager class for handling real-time network data aggregation and TFLite model inference.
 */
class NetworkForecastingManager(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val TAG = "NetworkForecasting"

    // Hardcoded Min-Max values for the 8 features
    private val FEATURE_MIN_VALUES = floatArrayOf(
        -97.13920942794009f, 24.288308740068103f, 0.5717822701859224f, 10.374439461883409f,
        18.252532007984534f, 2.6576841979571757f, 0.8315677966101694f, 0.5361331819901627f
    )
    private val FEATURE_MAX_VALUES = floatArrayOf(
        -41.541430192962544f, 187.9820968203953f, 6.2741345932607615f, 129.45518476081352f,
        256.4646206995281f, 36.35110502283105f, 1.918075050128903f, 1.9076376554174068f
    )


    private val dataBuffer = mutableListOf<NetworkDataPoint>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var windowStartTime: Long = 0L
    private val PRODUCTION_WINDOW_MS = 5 * 60 * 1000L // 5 Minutes
    private val WARMUP_THRESHOLD = 5 // Reduced from 30 to 5 seconds for faster first prediction
    private var lastMinuteLogged = -1

    init {
        loadModel()
    }


    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile("network_predictor.tflite")
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
            Log.e(TAG, "✅ BOOTUP SUCCESS: Model 'network_predictor.tflite' loaded and ready!")
        } catch (e: Exception) {
            // This will print a massive red error the second the app opens, telling us EXACTLY why it failed
            Log.e(TAG, "❌ FATAL STARTUP ERROR: Could not load TFLite model! Reason: ${e.message}", e)
        }
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Collects live network data and triggers prediction logic every 5 minutes.
     */
    fun onNetworkDataCollected(
        rssi: Float,
        latency: Float,
        packetLoss: Float,
        jitter: Float,
        downloadSpeed: Float,
        uploadSpeed: Float,
        networkType: Int,
        frequencyBand: Int
    ) {
        val currentTime = System.currentTimeMillis()
        
        // Ensure inputs are not zero to avoid model stagnation
        val safeLatency = if (latency <= 0f) 20f else latency
        val safeRssi = if (rssi == 0f) -70f else rssi
        
        val point = NetworkDataPoint(
            safeRssi, safeLatency, packetLoss, jitter,
            downloadSpeed, uploadSpeed, networkType, frequencyBand
        )

        synchronized(dataBuffer) {
            // Initialize timer on the first item of a new batch
            if (dataBuffer.isEmpty()) {
                windowStartTime = currentTime
                lastMinuteLogged = 6 // Reset countdown
                Log.e(TAG, "🚀 Prediction TFLite model is started and monitoring!")
            }

            dataBuffer.add(point)
            
            val elapsedMs = currentTime - windowStartTime
            val remainingMs = (PRODUCTION_WINDOW_MS - elapsedMs).coerceAtLeast(0)
            val currentRemainingMins = (remainingMs / 60000).toInt() + 1
            
            if (currentRemainingMins < lastMinuteLogged && currentRemainingMins > 0 && currentRemainingMins <= 5) {
                Log.e(TAG, "⏱️ Prediction Cycle: $currentRemainingMins minute(s) remaining...")
                lastMinuteLogged = currentRemainingMins
            }

            // Trigger prediction if window is reached OR if it's the first time and we have enough "warmup" data
            val isFirstPrediction = com.simats.netadaptive.data.PredictionRepository.latestPrediction.value == null
            if (elapsedMs >= PRODUCTION_WINDOW_MS || (isFirstPrediction && dataBuffer.size >= WARMUP_THRESHOLD)) {
                try {
                    val logPrefix = if (elapsedMs >= PRODUCTION_WINDOW_MS) "PRODUCTION" else "WARMUP"
                    Log.e(TAG, "🔥 [ML ENGINE] Starting $logPrefix Inference (Window Full)")
                    
                    // 1. Calculate Averages of buffered features
                    val avgFeatures = floatArrayOf(
                        dataBuffer.map { it.rssi }.average().toFloat(),
                        dataBuffer.map { it.latency }.average().toFloat(),
                        dataBuffer.map { it.packetLoss }.average().toFloat(),
                        dataBuffer.map { it.jitter }.average().toFloat(),
                        dataBuffer.map { it.downloadSpeed }.average().toFloat(),
                        dataBuffer.map { it.uploadSpeed }.average().toFloat(),
                        dataBuffer.map { it.networkType.toFloat() }.average().toFloat(),
                        dataBuffer.map { it.frequencyBand.toFloat() }.average().toFloat()
                    )

                    // 2. Normalize averaged data
                    val normalizedInput = normalizeInput(avgFeatures)

                    // 3. Run Inference
                    val inferenceResult = predictNext5Minutes(normalizedInput)

                    // 4. De-normalize output to real-world values
                    val finalResult = denormalizeOutput(inferenceResult)
                    val qualityScore = (100 - (finalResult[0] / 5) - (finalResult[1] * 5)).toInt().coerceIn(0, 100)

                    // 5. Success Result Logging (Clearly formatted as requested)
                    Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.e(TAG, "📊 PREDICTION COMPLETE")
                    Log.e(TAG, "📡 Target: Next 5-Minute Window")
                    Log.e(TAG, "⏱️ Predicted Latency: ${"%.2f".format(finalResult[0])} ms")
                    Log.e(TAG, "📉 Predicted Pkt Loss: ${"%.2f".format(finalResult[1])}%")
                    Log.e(TAG, "⭐ Quality Score: $qualityScore/100")
                    Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    // Update Repository with Prediction
                    val prediction = com.simats.netadaptive.data.model.PredictionResult(
                        currentLatency = avgFeatures[1],
                        predictedLatency = finalResult[0],
                        currentPacketLoss = avgFeatures[2],
                        predictedPacketLoss = finalResult[1],
                        predictedQualityScore = qualityScore
                    )
                    // We don't need to update metrics here as Service handles it every second
                    com.simats.netadaptive.data.PredictionRepository.updatePrediction(prediction)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Prediction Error: ${e.message}", e)
                } finally {
                    if (elapsedMs >= PRODUCTION_WINDOW_MS) {
                        dataBuffer.clear()
                        windowStartTime = 0L // Reset timer for the next batch
                    }
                }
            }
        }
    }

    /**
     * Executes the TFLite model with strict shape compliance [1, 1, 8] -> [1, 2].
     */
    private fun predictNext5Minutes(normalizedInput: FloatArray): FloatArray {
        val tflite = interpreter ?: throw Exception("TFLite Interpreter not initialized")

        // Wrap 1D input into 3D array: [1, 1, 8]
        val inputArray = arrayOf(arrayOf(normalizedInput))

        // Prepare 2D output array: [1, 2]
        val outputArray = Array(1) { FloatArray(2) }

        // Run inference
        tflite.run(inputArray, outputArray)

        return outputArray[0]
    }

    /**
     * Scales input features using the Min-Max normalization formula: (x - min) / (max - min)
     */
    private fun normalizeInput(features: FloatArray): FloatArray {
        val normalized = FloatArray(8)
        for (i in 0 until 8) {
            val min = FEATURE_MIN_VALUES[i]
            val max = FEATURE_MAX_VALUES[i]
            normalized[i] = (features[i] - min) / (max - min)
        }
        return normalized
    }

    /**
     * Scales output back using the Denormalization formula: (x_norm * (max - min)) + min
     */
    private fun denormalizeOutput(normalizedOutput: FloatArray): FloatArray {
        val denormalized = FloatArray(2)
        val latencyMin = FEATURE_MIN_VALUES[1]
        val latencyMax = FEATURE_MAX_VALUES[1]
        val packetLossMin = FEATURE_MIN_VALUES[2]
        val packetLossMax = FEATURE_MAX_VALUES[2]

        denormalized[0] = (normalizedOutput[0] * (latencyMax - latencyMin)) + latencyMin
        denormalized[1] = (normalizedOutput[1] * (packetLossMax - packetLossMin)) + packetLossMin
        
        return denormalized
    }

    fun close() {
        interpreter?.close()
        scope.cancel()
    }

    private data class NetworkDataPoint(
        val rssi: Float,
        val latency: Float,
        val packetLoss: Float,
        val jitter: Float,
        val downloadSpeed: Float,
        val uploadSpeed: Float,
        val networkType: Int,
        val frequencyBand: Int
    )
}

