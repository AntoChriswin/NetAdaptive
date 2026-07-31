package com.simats.netadaptive.engine.monitoring

import android.content.Context
import android.util.Log
import com.simats.netadaptive.data.repository.AppUsageRepository
import com.simats.netadaptive.engine.model.AppSessionMetrics
import com.simats.netadaptive.engine.model.AppState
import com.simats.netadaptive.engine.model.AppUsageWindow
import com.simats.netadaptive.engine.model.AppWindowEntry

class AppUsageTracker(private val context: Context) {
    
    private val activeSessions = mutableMapOf<String, AppSessionMetrics>()
    private val windowBaselines = mutableMapOf<String, WindowBaseline>()
    private var windowStartMs = System.currentTimeMillis()

    data class WindowBaseline(
        val usageBytes: Long,
        val fgDurationMs: Long,
        val bgDurationMs: Long
    )

    fun updateMetrics() {
        val dashboardApps = AppUsageRepository.appsUsage.value
        Log.d("SHARED_TRACKER_SIZE", dashboardApps.size.toString())
        Log.d("DASHBOARD_COUNT", dashboardApps.size.toString())
        
        for (appData in dashboardApps) {
            val pkg = appData.packageName
            val isForeground = appData.status == "Foreground"
            
            var session = activeSessions[pkg]
            if (session == null) {
                session = AppSessionMetrics(
                    appName = appData.name,
                    packageName = pkg,
                    uid = appData.uid,
                    sessionUsageMB = appData.usageBytes.toFloat() / (1024f * 1024f),
                    state = if (isForeground) AppState.FOREGROUND else AppState.BACKGROUND
                )
                activeSessions[pkg] = session
            } else {
                session.sessionUsageMB = appData.usageBytes.toFloat() / (1024f * 1024f)
                session.state = if (isForeground) AppState.FOREGROUND else AppState.BACKGROUND
                session.currentBandwidthMBps = appData.currentSpeedBytes.toFloat() / (1024f * 1024f)
                
                session.foregroundDurationMs = AppUsageRepository.getForegroundMs(appData.uid)
                session.backgroundDurationMs = AppUsageRepository.getBackgroundMs(appData.uid)
                session.totalActiveDurationMs = session.foregroundDurationMs + session.backgroundDurationMs
            }
        }
        
        Log.d("AI_COUNT", activeSessions.size.toString())
    }

    fun generateWindow(predictedLatency: Float, predictedPacketLoss: Float): AppUsageWindow {
        val currentTime = System.currentTimeMillis()
        val windowDurationMs = (currentTime - windowStartMs).coerceAtLeast(1)
        val windowEntries = mutableListOf<AppWindowEntry>()
        
        Log.d("RAW_APP_COLLECTION", "Generating window from SHARED data source. Apps: ${activeSessions.size}")

        for (session in activeSessions.values) {
            val baseline = windowBaselines[session.packageName]
            val currentUsageBytes = (session.sessionUsageMB * 1024 * 1024).toLong()
            
            val usageInWindow = if (baseline != null) {
                (currentUsageBytes - baseline.usageBytes).coerceAtLeast(0L).toFloat() / (1024f * 1024f)
            } else {
                session.sessionUsageMB
            }
            
            val fgInWindow = if (baseline != null) session.foregroundDurationMs - baseline.fgDurationMs else session.foregroundDurationMs
            val bgInWindow = if (baseline != null) session.backgroundDurationMs - baseline.bgDurationMs else session.backgroundDurationMs
            
            val interaction = if (windowDurationMs > 0) fgInWindow.toFloat() / windowDurationMs.toFloat() else 0f
            val avgBw = if (windowDurationMs > 0) (usageInWindow / (windowDurationMs / 1000f)) else 0f

            // Relaxed OR filtering
            if (fgInWindow > 0 || usageInWindow > 0 || avgBw > 0.01f || interaction > 0) {
                windowEntries.add(AppWindowEntry(
                    packageName = session.packageName,
                    appName = session.appName,
                    usageMB = usageInWindow,
                    foregroundMs = fgInWindow,
                    backgroundMs = bgInWindow,
                    avgBandwidth = avgBw,
                    peakBandwidth = session.peakBandwidthMBps,
                    interactionScore = interaction.coerceIn(0f, 1f),
                    networkContribution = 0f, // Simplified
                    state = session.state
                ))
            }

            // Save baseline for next window
            windowBaselines[session.packageName] = WindowBaseline(
                currentUsageBytes, session.foregroundDurationMs, session.backgroundDurationMs
            )
        }

        val finalApps = windowEntries.sortedByDescending { it.usageMB + it.interactionScore * 100 }.take(10)
        
        val window = AppUsageWindow(
            windowStartMs = windowStartMs,
            windowEndMs = currentTime,
            predictedLatency = predictedLatency,
            predictedPacketLoss = predictedPacketLoss,
            apps = finalApps
        )
        
        windowStartMs = currentTime
        Log.e("APP_WINDOW", "█ WINDOW GENERATED: ${finalApps.size} apps detected █")
        return window
    }
}
