package com.simats.netadaptive.engine.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import com.simats.netadaptive.R
import com.simats.netadaptive.engine.monitoring.AppUsageTracker
import com.simats.netadaptive.engine.priority.PriorityEngine
import com.simats.netadaptive.ml.NetworkForecastingManager
import com.simats.netadaptive.utils.NetworkUtils
import com.simats.netadaptive.vpn.PriorityResolver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class NetworkMonitorService : Service() {

    @Inject lateinit var vpnPriorityResolver: PriorityResolver
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var priorityEngine: PriorityEngine
    private lateinit var networkUtils: NetworkUtils
    private lateinit var appUsageTracker: AppUsageTracker
    private lateinit var forecastingManager: NetworkForecastingManager
    private var job: Job? = null

    companion object {
        private const val CHANNEL_ID = "PriorityEngineChannel"
        private const val NOTIFICATION_ID = 101
        private const val POLLING_INTERVAL = 1000L // 1 second for high-fidelity LSTM data
        private const val LSTM_INTERVAL = 5 * 60 * 1000L // 5 minutes for production
        private const val SYNC_INTERVAL = 30 * 60 * 1000L // 30 minutes for Firebase sync
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("PriorityEngine", "██████████████████████████████████████████")
        Log.e("PriorityEngine", "█ SERVICE CREATED: consolidated monitoring █")
        Log.e("PriorityEngine", "██████████████████████████████████████████")
        priorityEngine = PriorityEngine()
        networkUtils = NetworkUtils(this)
        appUsageTracker = AppUsageTracker(this)
        forecastingManager = NetworkForecastingManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("PriorityEngine", "█ SERVICE STARTED █")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        startPipeline()
        return START_STICKY
    }

    private fun startPipeline() {
        job?.cancel()
        job = serviceScope.launch {
            var lastLstmTime = 0L
            var lastSyncTime = System.currentTimeMillis()
            Log.d("PriorityEngine", "Pipeline started with 1s polling, 5min LSTM and 30min Sync")
            
            while (isActive) {
                try {
                    val currentTime = System.currentTimeMillis()
                    
                    // 1. Collect and Publish basic metrics (1s interval)
                    val metrics = networkUtils.collectMetrics()
                    com.simats.netadaptive.data.PredictionRepository.update(metrics)
                    
                    // 2. Update Prediction Window (handled inside forecastingManager)
                    forecastingManager.onNetworkDataCollected(
                        rssi = metrics.rssi.toFloat(),
                        latency = metrics.latency,
                        packetLoss = metrics.packetLoss,
                        jitter = metrics.jitter,
                        downloadSpeed = metrics.downloadSpeed,
                        uploadSpeed = metrics.uploadSpeed,
                        networkType = if (metrics.networkType == "WiFi") 1 else 2,
                        frequencyBand = if (metrics.frequencyBand == "2.4GHz") 1 else 2
                    )

                    // 3. App Usage and Tracking
                    try {
                        com.simats.netadaptive.data.repository.AppUsageRepository.updateUsage(this@NetworkMonitorService)
                        appUsageTracker.updateMetrics()
                    } catch (e: Exception) { }

                    // 4. 5-Minute LSTM Cycle
                    if (currentTime - lastLstmTime >= LSTM_INTERVAL) {
                        val prediction = com.simats.netadaptive.data.PredictionRepository.latestPrediction.value
                        if (prediction != null) {
                            Log.d("PriorityEngine", "🔥 [LSTM CYCLE] Processing 5-min window...")
                            val window = appUsageTracker.generateWindow(prediction.predictedLatency, prediction.predictedPacketLoss)
                            priorityEngine.processWindow(window)
                            priorityEngine.updateTiers()
                            vpnPriorityResolver.onTiersUpdated(priorityEngine.getCurrentTiers())
                            lastLstmTime = currentTime
                        }
                    }

                    // 5. 30-Minute Firebase Sync Cycle (Fixed for reliability)
                    if (currentTime - lastSyncTime >= SYNC_INTERVAL) {
                        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            Log.d("PriorityEngine", "📤 [SYNC CYCLE] Triggering 30-min Firebase update...")
                            try {
                                val repo = com.simats.netadaptive.data.repository.AnalyticsFirestoreRepository(this@NetworkMonitorService)
                                repo.syncPredictionLogs(uid)
                                Log.d("PriorityEngine", "✅ [SYNC CYCLE] Prediction logs updated successfully")
                            } catch (e: Exception) {
                                Log.e("PriorityEngine", "❌ [SYNC CYCLE] Sync failed: ${e.message}")
                            }
                        }
                        lastSyncTime = currentTime
                    }

                } catch (e: Exception) {
                    Log.e("PriorityEngine", "Pipeline error: ${e.message}")
                }
                
                delay(POLLING_INTERVAL)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Priority Engine",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NetAdaptive AI Engine")
            .setContentText("Optimizing network priorities...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        job?.cancel()
        serviceScope.cancel()
        forecastingManager.close()
        priorityEngine.close()
        super.onDestroy()
    }
}
