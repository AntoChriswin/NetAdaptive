package com.simats.netadaptive.viewmodel.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.data.local.dao.AnalyticsDao
import com.simats.netadaptive.data.local.entities.AnalyticsDailyEntity
import com.simats.netadaptive.data.repository.AppUsageRepository
import com.simats.netadaptive.data.repository.DataUsageRepository
import com.simats.netadaptive.ml.QualityScoreCalculator
import com.simats.netadaptive.vpn.MetricsManager
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    application: Application,
    private val analyticsDao: AnalyticsDao,
    private val metricsManager: MetricsManager
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val latestMetrics = PredictionRepository.latestMetrics
    val latestPrediction = PredictionRepository.latestPrediction
    val vpnMetrics = metricsManager.metrics
    val appsUsage = AppUsageRepository.appsUsage

    // Data Usage specific flows
    private val _monthlyUsage = MutableStateFlow<DataUsageRepository.MonthlyUsage?>(null)
    val monthlyUsage = _monthlyUsage.asStateFlow()

    private val _hourlyUsage = MutableStateFlow<List<DataUsageRepository.HourlyUsagePoint>>(emptyList())
    val hourlyUsage = _hourlyUsage.asStateFlow()

    val todaysUsage = monthlyUsage.map { usage ->
        usage?.dailyUsage?.lastOrNull()?.let { it.wifiBytes + it.mobileBytes } ?: 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val weeklyAnalytics = analyticsDao.getWeeklyAnalytics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentOptimizations = analyticsDao.getRecentOptimizationEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for calculated values
    private val _dataSaved = MutableStateFlow("0 B")
    val dataSaved = _dataSaved.asStateFlow()

    private val _spikesAvoided = MutableStateFlow(0)
    val spikesAvoided = _spikesAvoided.asStateFlow()

    private val _uptimePercent = MutableStateFlow("0.0%")
    val uptimePercent = _uptimePercent.asStateFlow()

    private val _totalOptimizations = MutableStateFlow(0)
    val totalOptimizations = _totalOptimizations.asStateFlow()

    private val _qualityScore = MutableStateFlow(0)
    val qualityScore = _qualityScore.asStateFlow()

    val totalForegroundBytes = appsUsage.map { list ->
        list.sumOf { it.fgUsageBytes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalBackgroundBytes = appsUsage.map { list ->
        list.sumOf { it.bgUsageBytes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val mostOptimizedApp = appsUsage.map { list ->
        list.filter { it.isThrottled || it.isDelayed }.maxByOrNull { it.usageBytes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        startDataLoggingLoop()
        observeMetrics()
    }

    private fun observeMetrics() {
        viewModelScope.launch {
            latestMetrics.collect { metrics ->
                metrics?.let {
                    _qualityScore.value = QualityScoreCalculator.calculate(it.rssi, it.latency, it.packetLoss)
                }
            }
        }
    }

    private fun startDataLoggingLoop() {
        viewModelScope.launch {
            while (true) {
                logDailyStats()
                updateCalculatedStats()
                refreshDataUsage()
                delay(10000) // Update dashboard every 10 seconds
            }
        }
    }

    private fun refreshDataUsage() {
        _monthlyUsage.value = DataUsageRepository.getMonthlyUsage(context)
        _hourlyUsage.value = DataUsageRepository.getTodayHourlyUsage(context)
    }

    private suspend fun logDailyStats() {
        val today = dateFormat.format(Date())
        val currentVpnMetrics = vpnMetrics.value
        val networkMetrics = latestMetrics.value
        
        val existing = analyticsDao.getAnalyticsForDate(today)
        
        // Simple heuristic: 1KB per blocked/delayed packet as "saved" or "optimized"
        val calculatedDataSaved = (currentVpnMetrics.blockedPackets + currentVpnMetrics.delayedPackets) * 1024L
        
        val newStats = AnalyticsDailyEntity(
            date = today,
            totalDataSavedBytes = calculatedDataSaved,
            spikesAvoided = (existing?.spikesAvoided ?: 0) + if ((networkMetrics?.latency ?: 0f) > 150f) 1 else 0,
            avgQualityScore = _qualityScore.value.toFloat(),
            uptimeMs = (existing?.uptimeMs ?: 0) + 10000,
            totalPacketsProcessed = currentVpnMetrics.forwardedPackets,
            totalPacketsOptimized = currentVpnMetrics.delayedPackets,
            totalPacketsDropped = currentVpnMetrics.blockedPackets,
            dnsRequestsHandled = (existing?.dnsRequestsHandled ?: 0)
        )
        
        analyticsDao.insertDailyAnalytics(newStats)
        _totalOptimizations.value = analyticsDao.getTotalOptimizationsCount()
    }

    private fun updateCalculatedStats() {
        val list = weeklyAnalytics.value
        if (list.isNotEmpty()) {
            val latest = list.first()
            _dataSaved.value = formatBytes(latest.totalDataSavedBytes)
            _spikesAvoided.value = list.sumOf { it.spikesAvoided }
            
            // Uptime calculation (assuming 24h total possible for current day)
            val uptimePercentVal = (latest.uptimeMs.toFloat() / (24 * 60 * 60 * 1000f)) * 100
            _uptimePercent.value = "%.1f%%".format(minOf(100f, uptimePercentVal))
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.toDouble())).toInt()
        return "%.1f %s".format(bytes / Math.pow(1024.toDouble(), digitGroups.toDouble()), units[digitGroups])
    }
}
