package com.simats.netadaptive.data.repository

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.data.model.PriorityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

object AppUsageRepository {
    private const val TAG = "AppUsageRepository"
    private val _appsUsage = MutableStateFlow<List<AppUsageData>>(emptyList())
    val appsUsage = _appsUsage.asStateFlow()

    // Persistent storage for the session
    private val sessionBaselineMap = mutableMapOf<Int, Long>()
    private val maxSessionUsageMap = mutableMapOf<Int, Long>()
    
    // Duration and interaction tracking for AI
    private val foregroundDurationMap = mutableMapOf<Int, Long>()
    private val backgroundDurationMap = mutableMapOf<Int, Long>()
    private val lastSeenTimestampMap = mutableMapOf<Int, Long>()
    
    // For bandwidth (Speed) calculation
    private val lastUsageMap = mutableMapOf<Int, Long>()
    private val lastTimeMap = mutableMapOf<Int, Long>()

    // Cache to hold the last valid list of apps
    private var cachedAppList = mutableMapOf<Int, AppUsageData>()

    private var lastKnownForegroundPackage: String? = null

    fun updateUsage(context: Context) {
        val pm = context.packageManager
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val endTime = System.currentTimeMillis()
        val startTime = 1L 

        // 1. Query traffic
        val uidUsageMap = mutableMapOf<Int, Long>()
        val uidFgUsageMap = mutableMapOf<Int, Long>()
        val uidWifiUsageMap = mutableMapOf<Int, Long>()
        val uidMobileUsageMap = mutableMapOf<Int, Long>()
        try {
            nsm.querySummary(ConnectivityManager.TYPE_WIFI, null, startTime, endTime).use { wifiStats ->
                val bucket = NetworkStats.Bucket()
                while (wifiStats.hasNextBucket()) {
                    wifiStats.getNextBucket(bucket)
                    val bytes = bucket.rxBytes + bucket.txBytes
                    uidUsageMap[bucket.uid] = (uidUsageMap[bucket.uid] ?: 0L) + bytes
                    uidWifiUsageMap[bucket.uid] = (uidWifiUsageMap[bucket.uid] ?: 0L) + bytes
                    if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) {
                        uidFgUsageMap[bucket.uid] = (uidFgUsageMap[bucket.uid] ?: 0L) + bytes
                    }
                }
            }
            nsm.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime).use { mobileStats ->
                val bucket = NetworkStats.Bucket()
                while (mobileStats.hasNextBucket()) {
                    mobileStats.getNextBucket(bucket)
                    val bytes = bucket.rxBytes + bucket.txBytes
                    uidUsageMap[bucket.uid] = (uidUsageMap[bucket.uid] ?: 0L) + bytes
                    uidMobileUsageMap[bucket.uid] = (uidMobileUsageMap[bucket.uid] ?: 0L) + bytes
                    if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) {
                        uidFgUsageMap[bucket.uid] = (uidFgUsageMap[bucket.uid] ?: 0L) + bytes
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Query failed: ${e.message}")
            return
        }

        // 2. Accurate Foreground Detection via UsageEvents
        val eventStartTime = endTime - 60000 // Look at last minute of events
        val events = usm.queryEvents(eventStartTime, endTime)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastKnownForegroundPackage = event.packageName
            }
        }

        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        apps.forEach { app ->
            val uid = app.uid
            val totalBytes = uidUsageMap[uid] ?: 0L
            
            if (!sessionBaselineMap.containsKey(uid)) {
                sessionBaselineMap[uid] = totalBytes
            }
            
            val currentCalculated = if (totalBytes >= (sessionBaselineMap[uid] ?: 0L)) {
                totalBytes - (sessionBaselineMap[uid] ?: 0L)
            } else 0L
            
            val sessionUsage = Math.max(maxSessionUsageMap[uid] ?: 0L, currentCalculated)
            maxSessionUsageMap[uid] = sessionUsage

            val currentTime = System.currentTimeMillis()
            val previousTotal = lastUsageMap[uid] ?: totalBytes
            val previousTime = lastTimeMap[uid] ?: (currentTime - 1000)
            
            val deltaBytes = if (totalBytes > previousTotal) totalBytes - previousTotal else 0L
            val timeInterval = (currentTime - previousTime) / 1000.0f
            val bandwidth = if (timeInterval > 0 && deltaBytes > 0) (deltaBytes / timeInterval).toLong() else 0L
            
            lastUsageMap[uid] = totalBytes
            lastTimeMap[uid] = currentTime

            val packageName = app.packageName
            val isForeground = packageName == lastKnownForegroundPackage
            
            // AI Duration Tracking
            val lastSeen = lastSeenTimestampMap[uid] ?: currentTime
            val durationDelta = currentTime - lastSeen
            if (isForeground) {
                foregroundDurationMap[uid] = (foregroundDurationMap[uid] ?: 0L) + durationDelta
            } else {
                backgroundDurationMap[uid] = (backgroundDurationMap[uid] ?: 0L) + durationDelta
            }
            lastSeenTimestampMap[uid] = currentTime

            val priorityInfo = getAppPriorityInfo(app, pm)
            val fgBytes = uidFgUsageMap[uid] ?: 0L
            val bgBytes = totalBytes - fgBytes
            val wifiBytes = uidWifiUsageMap[uid] ?: 0L
            val mobileBytes = uidMobileUsageMap[uid] ?: 0L

            if (sessionUsage > 0 || isForeground || totalBytes > 1024 * 10) { // Only if usage > 10KB
                cachedAppList[uid] = AppUsageData(
                    packageName = packageName,
                    name = pm.getApplicationLabel(app).toString(),
                    usageBytes = sessionUsage,
                    usageDisplay = formatBytes(sessionUsage),
                    currentSpeed = formatSpeed(bandwidth),
                    currentSpeedBytes = bandwidth,
                    uid = uid,
                    status = if (isForeground) "Foreground" else "Background",
                    category = priorityInfo.first,
                    priority = priorityInfo.second,
                    priorityScore = when(priorityInfo.second) {
                        PriorityLevel.HIGH -> 1
                        PriorityLevel.MEDIUM -> 2
                        PriorityLevel.LOW -> 3
                    },
                    fgUsageBytes = fgBytes,
                    bgUsageBytes = bgBytes,
                    wifiUsageBytes = wifiBytes,
                    mobileUsageBytes = mobileBytes
                )
            }
        }

        val newList = cachedAppList.values.toList().sortedWith(
            compareByDescending<AppUsageData> { it.status == "Foreground" }
            .thenByDescending { it.currentSpeedBytes }
            .thenByDescending { it.usageBytes }
        )
        
        if (newList.isNotEmpty()) {
            _appsUsage.value = newList
            Log.d("DASHBOARD_COUNT", newList.size.toString())
            Log.d("CURRENT_FG_APP", lastKnownForegroundPackage ?: "None")
        } else if (lastKnownForegroundPackage != null) {
            // Even if we have no usage stats, we know which app is foreground
            _appsUsage.value = listOf(AppUsageData(
                packageName = lastKnownForegroundPackage!!,
                name = lastKnownForegroundPackage!!.split(".").last().replaceFirstChar { it.uppercase() },
                usageBytes = 0,
                usageDisplay = "0 B",
                status = "Foreground",
                uid = 0
            ))
        }
    }

    fun getForegroundMs(uid: Int) = foregroundDurationMap[uid] ?: 0L
    fun getBackgroundMs(uid: Int) = backgroundDurationMap[uid] ?: 0L

    private fun getAppPriorityInfo(app: ApplicationInfo, pm: PackageManager): Pair<String, PriorityLevel> {
        val pkg = app.packageName.lowercase()
        val label = pm.getApplicationLabel(app).toString().lowercase()
        
        if (pkg.contains("meet") || pkg.contains("zoom") || pkg.contains("teams") || pkg.contains("webex") ||
            pkg.contains("skype") || pkg.contains("discord") || label.contains("meeting") ||
            pkg.contains("pubg") || pkg.contains("freefire") || pkg.contains("roblox") || pkg.contains("games") ||
            pkg.contains("maps") || pkg.contains("navigation") || label.contains("map") ||
            pkg.contains("classroom") || pkg.contains("edx") || pkg.contains("coursera") || label.contains("academy")) {
            return "Real-time" to PriorityLevel.HIGH
        }

        if (pkg.contains("chrome") || pkg.contains("firefox") || pkg.contains("browser") ||
            pkg.contains("whatsapp") || pkg.contains("messenger") || pkg.contains("telegram") ||
            pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("tiktok") || pkg.contains("twitter") ||
            pkg.contains("youtube") || pkg.contains("netflix") || label.contains("social")) {
            return "Communication" to PriorityLevel.MEDIUM
        }

        if (pkg.contains("update") || pkg.contains("download") || pkg.contains("sync") ||
            pkg.contains("backup") || pkg.contains("cloud") || pkg.contains("drive") ||
            pkg.contains("vending") || (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
            return "Background Sync" to PriorityLevel.LOW
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return when (app.category) {
                ApplicationInfo.CATEGORY_GAME -> "Gaming" to PriorityLevel.HIGH
                ApplicationInfo.CATEGORY_VIDEO -> "Streaming" to PriorityLevel.MEDIUM
                ApplicationInfo.CATEGORY_SOCIAL -> "Social" to PriorityLevel.MEDIUM
                ApplicationInfo.CATEGORY_MAPS -> "Navigation" to PriorityLevel.HIGH
                else -> "Productivity" to PriorityLevel.MEDIUM
            }
        }
        return "Utility" to PriorityLevel.LOW
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return "${formatBytes(bytesPerSec)}/s"
    }
}
