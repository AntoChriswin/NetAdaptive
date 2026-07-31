package com.simats.netadaptive.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.services.network.NetworkMonitoringService
import kotlinx.coroutines.flow.StateFlow

class NetworkPredictionViewModel(application: Application) : AndroidViewModel(application) {

    val latestMetrics = PredictionRepository.latestMetrics
    val latestPrediction = PredictionRepository.latestPrediction

    fun startMonitoring() {
        // Only start the new consolidated Priority Engine service
        val engineIntent = Intent(getApplication(), com.simats.netadaptive.engine.service.NetworkMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(engineIntent)
        } else {
            getApplication<Application>().startService(engineIntent)
        }
    }

    fun stopMonitoring() {
        val engineIntent = Intent(getApplication(), com.simats.netadaptive.engine.service.NetworkMonitorService::class.java)
        getApplication<Application>().stopService(engineIntent)
    }
}
