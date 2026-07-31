package com.simats.netadaptive.services.network

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import com.simats.netadaptive.R
import com.simats.netadaptive.ml.NetworkForecastingManager
import com.simats.netadaptive.utils.JsonLogger
import com.simats.netadaptive.utils.NetworkUtils
import kotlinx.coroutines.*

class NetworkMonitoringService : Service() {

    private val TAG = "NETADAPTIVE_SERVICE"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var networkUtils: NetworkUtils
    private lateinit var forecastingManager: NetworkForecastingManager
    private var monitoringJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "NetworkMonitoringChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        networkUtils = NetworkUtils(this)
        forecastingManager = NetworkForecastingManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        Log.d(TAG, "Starting monitoring loop")
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    val metrics = networkUtils.collectMetrics()
                    Log.d(TAG, "Collected metrics: RSSI=${metrics.rssi}, Latency=${metrics.latency}")
                    
                    // Update repositories for UI
                    com.simats.netadaptive.data.PredictionRepository.update(metrics)
                    com.simats.netadaptive.data.repository.AppUsageRepository.updateUsage(this@NetworkMonitoringService)
                    
                    // Add data point to the new forecasting manager
                    forecastingManager.onNetworkDataCollected(
                        rssi = metrics.rssi.toFloat(),
                        latency = metrics.latency,
                        packetLoss = metrics.packetLoss,
                        jitter = metrics.jitter,
                        downloadSpeed = metrics.downloadSpeed,
                        uploadSpeed = metrics.uploadSpeed,
                        networkType = encodeNetworkType(metrics.networkType),
                        frequencyBand = encodeFrequencyBand(metrics.frequencyBand)
                    )
                    
                    // Note: We are no longer using the old PredictionEngine as per clean slate requirement
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                }
                delay(1000)
            }
        }
    }

    private fun encodeNetworkType(type: String): Int {
        return when (type) {
            "WiFi" -> 1
            "Cellular" -> 2
            else -> 0
        }
    }

    private fun encodeFrequencyBand(band: String): Int {
        return when (band) {
            "2.4GHz" -> 1
            "5GHz" -> 2
            "6GHz" -> 3
            else -> 0
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NetAdaptive AI Monitoring")
            .setContentText("Collecting live network data and predicting quality...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        forecastingManager.close()
        super.onDestroy()
    }
}
