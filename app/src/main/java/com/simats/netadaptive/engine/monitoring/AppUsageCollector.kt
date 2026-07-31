package com.simats.netadaptive.engine.monitoring

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.util.Log

data class AppMonitoringInfo(
    val name: String,
    val packageName: String,
    val uid: Int,
    val isForeground: Boolean,
    val sessionUsage: Long,
    val bandwidth: Long, // bytes/sec
    val lastActiveTime: Long,
    val rxBytes: Long,
    val txBytes: Long,
    val isUnsupported: Boolean = false
)

class AppUsageCollector(private val context: Context, private val sessionTracker: SessionUsageTracker) {
    private val pm = context.packageManager
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val lastTrafficMap = mutableMapOf<Int, Long>()
    private val lastTimeMap = mutableMapOf<Int, Long>()
    private val ownPackage = context.packageName

    private val systemPrefixes = listOf(
        "com.android.",
        "android.",
        "com.coloros.",
        "com.oplus.",
        "com.qti.",
        "com.qualcomm.",
        "com.google.android.permissioncontroller",
        "com.google.android.packageinstaller",
        "com.android.systemui",
        "com.android.internal"
    )

    fun collectActiveApps(): Pair<List<AppMonitoringInfo>, List<String>> {
        // Check Permission
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
            Log.e("NET_USAGE", "❌ PERMISSION MISSING: Usage Stats permission not granted.")
            return emptyList<AppMonitoringInfo>() to emptyList<String>()
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60000 // Last minute for events
        val foregroundPackage = getForegroundPackage(startTime, endTime)

        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val validUserApps = mutableListOf<String>()
        val activeAppsForTiers = mutableListOf<AppMonitoringInfo>()
        val inactiveAppsForTiers = mutableListOf<String>()

        for (app in apps) {
            val packageName = app.packageName
            val label = pm.getApplicationLabel(app).toString()
            val uid = app.uid

            // STEP 1 - Exclude NetAdaptive
            if (packageName == ownPackage) {
                Log.d("SELF_EXCLUDED", "NetAdaptive")
                continue
            }

            // --- SYSTEM FILTERING LOGIC ---
            
            // 1. UID Check
            if (uid < 10000) {
                Log.d("SYSTEM_SKIPPED", "\napp=$label\npackage=$packageName\nuid=$uid\nreason=System UID")
                continue
            }

            // 2. Package Prefix Check
            if (systemPrefixes.any { packageName.startsWith(it) }) {
                Log.d("SYSTEM_SKIPPED", "\napp=$label\npackage=$packageName\nuid=$uid\nreason=System Prefix")
                continue
            }

            // 3. System Flag Check with Launcher Intent exception
            val isSystemFlag = (app.flags and ApplicationInfo.FLAG_SYSTEM != 0) || 
                               (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
            
            val hasLauncher = pm.getLaunchIntentForPackage(packageName) != null
            
            if (isSystemFlag && !hasLauncher) {
                Log.d("SYSTEM_SKIPPED", "\napp=$label\npackage=$packageName\nuid=$uid\nreason=System Flag No Launcher")
                continue
            }

            // If we reached here, it's accepted as a user-facing app
            Log.d("USER_APP_ACCEPTED", "\napp=$label\npackage=$packageName\nuid=$uid")
            validUserApps.add(label)

            // --- MONITORING LOGIC ---

            val rx = TrafficStats.getUidRxBytes(uid)
            val tx = TrafficStats.getUidTxBytes(uid)
            val isSupported = (rx != TrafficStats.UNSUPPORTED.toLong() || tx != TrafficStats.UNSUPPORTED.toLong())
            
            if (!isSupported) continue

            val isForeground = packageName == foregroundPackage
            val sessionUsage = sessionTracker.getSessionUsage(uid, label)
            val currentTraffic = (if (rx >= 0) rx else 0L) + (if (tx >= 0) tx else 0L)

            // Calculate bandwidth
            val previousTraffic = lastTrafficMap[uid] ?: currentTraffic
            val currentTime = System.currentTimeMillis()
            val previousTime = lastTimeMap[uid] ?: (currentTime - 1000)
            val deltaBytes = if (currentTraffic > previousTraffic) currentTraffic - previousTraffic else 0L
            val deltaTimeMs = currentTime - previousTime
            val bandwidth = if (deltaTimeMs > 0) (deltaBytes * 1000 / deltaTimeMs) else 0L
            
            lastTrafficMap[uid] = currentTraffic
            lastTimeMap[uid] = currentTime

            if (isForeground || sessionUsage > 1024 * 1024 || bandwidth > 0) {
                activeAppsForTiers.add(
                    AppMonitoringInfo(
                        name = label,
                        packageName = packageName,
                        uid = uid,
                        isForeground = isForeground,
                        sessionUsage = sessionUsage,
                        bandwidth = bandwidth,
                        lastActiveTime = if (isForeground) endTime else 0L,
                        rxBytes = rx,
                        txBytes = tx,
                        isUnsupported = false
                    )
                )
            } else {
                inactiveAppsForTiers.add(label)
            }
        }

        // Final User Apps Log
        Log.d("VALID_USER_APPS", "\n" + validUserApps.take(15).joinToString("\n"))

        // Active app detailed logs
        activeAppsForTiers.forEach { app ->
            Log.d("NET_USAGE", "\n${app.name}\nforeground=${app.isForeground}\nsession=${formatBytes(app.sessionUsage)}\nbandwidth=${formatBytes(app.bandwidth)}/s")
        }

        val sortedActive = activeAppsForTiers.sortedByDescending { it.isForeground || it.bandwidth > 0 }.take(10)
        
        Log.d("NET_ACTIVE", "\n" + sortedActive.joinToString("\n") { it.name })
        Log.d("NET_INACTIVE", "\n" + inactiveAppsForTiers.take(10).joinToString("\n"))

        return sortedActive to inactiveAppsForTiers
    }

    private fun getForegroundPackage(startTime: Long, endTime: Long): String? {
        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundApp: String? = null
        
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundApp = event.packageName
            }
        }
        return lastForegroundApp
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1] + ""
        return String.format(java.util.Locale.US, "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }
}
