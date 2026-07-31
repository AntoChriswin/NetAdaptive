package com.simats.netadaptive.engine.monitoring

import android.net.TrafficStats
import android.util.Log

class SessionUsageTracker {
    private val baselineMap = mutableMapOf<Int, Long>()

    /**
     * sessionUsage = (currentRx + currentTx) - (baselineRx + baselineTx)
     * Baseline captured: When monitoring service starts (first time we see the UID)
     */
    fun getSessionUsage(uid: Int, appName: String): Long {
        val rx = TrafficStats.getUidRxBytes(uid)
        val tx = TrafficStats.getUidTxBytes(uid)
        
        if (rx == TrafficStats.UNSUPPORTED.toLong() || tx == TrafficStats.UNSUPPORTED.toLong()) {
            return 0L
        }
        
        val currentTraffic = rx + tx
        val baseline = baselineMap.getOrPut(uid) { currentTraffic }
        
        val delta = currentTraffic - baseline
        
        // Log as per STEP 3
        if (delta > 0) {
            Log.d("NET_SESSION", "\napp=$appName\nbaseline=$baseline\ncurrent=$currentTraffic\ndelta=$delta")
        }
        
        return if (delta < 0) 0L else delta
    }
    
    fun resetBaselines() {
        baselineMap.clear()
    }
}
