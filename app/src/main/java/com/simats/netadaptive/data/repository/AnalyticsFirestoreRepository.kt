package com.simats.netadaptive.data.repository

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.data.repository.PriorityRepository
import com.simats.netadaptive.data.model.AnalyticsSnapshot
import com.simats.netadaptive.data.model.AppAnalytics
import com.simats.netadaptive.data.model.AppUsage
import com.simats.netadaptive.data.model.DayAnalytics
import com.simats.netadaptive.data.model.PeakUsageDay
import com.simats.netadaptive.data.model.TopAppsUsage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of AnalyticsRepository that syncs with Cloud Firestore.
 */
class AnalyticsFirestoreRepository(private val context: Context) : AnalyticsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AnalyticsSync"

    override suspend fun uploadAnalytics() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e(TAG, "AnalyticsSync failed: User not authenticated")
            return
        }

        try {
            Log.d(TAG, "AnalyticsSync started for user: $uid")
            
            // 0. Sync Prediction Logs First (Prioritize most requested data)
            syncPredictionLogs(uid)
            
            // 1. Upload Current Summary Snapshot
            val snapshot = getCurrentAnalytics()
            firestore.collection("users")
                .document(uid)
                .collection("analytics")
                .document("current")
                .set(snapshot)
                .await()
                
            // 2. Upload Foreground/Background Analytics (users/{uid}/analytics/apps/fgbg-data)
            firestore.collection("users")
                .document(uid)
                .collection("analytics")
                .document("apps")
                .collection("fgbg-data")
                .document("fgbg-data")
                .set(snapshot)
                .await()

            // 3. Upload Detailed Per-App Usage
            updateAppUsageAnalytics(uid)
            
            // 3. Upload Top 10 Apps Usage (users/{uid}/analytics/apps/App_usage)
            uploadTopAppsUsage(uid)

            Log.d(TAG, "AnalyticsSync success: All data uploaded for users/$uid")
        } catch (e: Exception) {
            Log.e(TAG, "AnalyticsSync failed: ${e.message}", e)
        }
    }

    suspend fun syncPredictionLogs(uid: String) {
        val PERSISTENCE_TAG = "PredictionPersistence"
        try {
            val records = PredictionRepository.predictionHistory.value
            if (records.isEmpty()) {
                Log.d(PERSISTENCE_TAG, "No prediction records to sync")
                return
            }

            Log.d(PERSISTENCE_TAG, "UPLOAD START: recordCount = ${records.size}")

            // Sort chronologically (oldest first)
            val sortedRecords = records.sortedBy { it.predictedTime }
            
            val predictionData = sortedRecords.mapIndexed { index, record ->
                Log.d(PERSISTENCE_TAG, "UPLOAD RECORD: index = $index, latency = ${record.predictedLatencyMs}, loss = ${record.predictedPacketLossPercent}, predictedTime = ${record.predictedTime}")
                mapOf(
                    "predictedLatencyMs" to record.predictedLatencyMs,
                    "predictedPacketLossPercent" to record.predictedPacketLossPercent,
                    "predictedTime" to Timestamp(Date(record.predictedTime))
                )
            }

            val predDocRef = firestore.collection("users")
                .document(uid)
                .collection("analytics")
                .document("apps")
                .collection("Predtiction_log")
                .document("prediction")

            // We use a Map to represent the indexes 0, 1, 2... as fields within the "prediction" document
            // This ensures a clean replacement of the whole snapshot.
            val uploadMap = mutableMapOf<String, Any>()
            predictionData.forEachIndexed { index, data ->
                uploadMap[index.toString()] = data
            }

            // Replace the entire "prediction" document to ensure no stale indexes remain
            predDocRef.set(uploadMap).await()
            
            Log.d(PERSISTENCE_TAG, "UPLOAD SUCCESS: Successfully synced ${predictionData.size} prediction records to Firestore")
        } catch (e: Exception) {
            Log.e(PERSISTENCE_TAG, "UPLOAD FAILED: Error syncing prediction logs: ${e.message}", e)
        }
    }

    private suspend fun uploadTopAppsUsage(uid: String) {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val pm = context.packageManager
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageMap = mutableMapOf<Int, AppUsageStats>()
        
        fun query(type: Int) {
            try {
                nsm.querySummary(type, null, startTime, endTime).use { stats ->
                    val bucket = NetworkStats.Bucket()
                    while (stats.hasNextBucket()) {
                        stats.getNextBucket(bucket)
                        val bytes = bucket.rxBytes + bucket.txBytes
                        val current = usageMap[bucket.uid] ?: AppUsageStats()
                        current.total += bytes
                        if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) {
                            current.foreground += bytes
                        }
                        usageMap[bucket.uid] = current
                    }
                }
            } catch (e: Exception) { }
        }
        
        query(ConnectivityManager.TYPE_WIFI)
        query(ConnectivityManager.TYPE_MOBILE)

        val topAppsList = mutableListOf<AppUsage>()
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (app in installedApps) {
            val stats = usageMap[app.uid] ?: continue
            val totalMB = stats.total / (1024.0 * 1024.0)
            if (totalMB <= 0) continue

            val packageName = app.packageName
            if (!isPackageUserFacing(packageName, pm)) continue

            val appLabel = pm.getApplicationLabel(app).toString()
            if (appLabel.isBlank()) continue

            val fgMB = stats.foreground / (1024.0 * 1024.0)
            
            topAppsList.add(AppUsage(
                appName = appLabel,
                packageName = packageName,
                totalUsageMB = totalMB,
                foregroundUsageMB = fgMB,
                backgroundUsageMB = totalMB - fgMB,
                tier = "", // Tier populated by Priority Engine elsewhere if needed
                lastUpdated = Timestamp.now()
            ))
        }

        // Sort by total usage and take top 10
        val finalTop10 = topAppsList
            .sortedByDescending { it.totalUsageMB }
            .take(10)

        if (finalTop10.isNotEmpty()) {
            val topAppsData = TopAppsUsage(
                lastUpdated = Timestamp.now(),
                topApps = finalTop10
            )

            // Path: users/{uid}/analytics/apps/App_usage
            // Interpreting as: analytics (coll) -> apps (doc) -> App_usage (coll) -> App_usage (doc)
            firestore.collection("users")
                .document(uid)
                .collection("analytics")
                .document("apps")
                .collection("App_usage")
                .document("App_usage")
                .set(topAppsData)
                .await()
            
            Log.d(TAG, "Successfully uploaded top 10 apps to App_usage document")
        }
    }

    private suspend fun updateAppUsageAnalytics(uid: String) {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val pm = context.packageManager
        
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val appsDocRef = firestore.collection("users").document(uid).collection("analytics").document("apps")
        
        // 1. Daily Reset Logic
        val doc = appsDocRef.get().await()
        if (doc.exists()) {
            val lastDate = doc.getString("date")
            if (lastDate != dateString) {
                // Clear previous day's app analytics sub-collection
                deleteSubCollection(appsDocRef.collection("applications"))
                appsDocRef.set(DayAnalytics(date = dateString, lastUpdated = Timestamp.now())).await()
            }
        } else {
            appsDocRef.set(DayAnalytics(date = dateString, lastUpdated = Timestamp.now())).await()
        }

        // 2. Data Collection (Today's Usage)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageMap = mutableMapOf<Int, AppUsageStats>()
        
        fun query(type: Int) {
            try {
                nsm.querySummary(type, null, startTime, endTime).use { stats ->
                    val bucket = NetworkStats.Bucket()
                    while (stats.hasNextBucket()) {
                        stats.getNextBucket(bucket)
                        val bytes = bucket.rxBytes + bucket.txBytes
                        val current = usageMap[bucket.uid] ?: AppUsageStats()
                        current.total += bytes
                        if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) {
                            current.foreground += bytes
                        }
                        usageMap[bucket.uid] = current
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying stats for type $type", e)
            }
        }
        
        query(ConnectivityManager.TYPE_WIFI)
        query(ConnectivityManager.TYPE_MOBILE)

        // 3. Map to AppAnalytics models with System Filtering
        val appsList = mutableListOf<AppAnalytics>()
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (app in installedApps) {
            val packageName = app.packageName
            val stats = usageMap[app.uid]
            
            // Skip if no usage
            if (stats == null || stats.total <= 0) continue

            // Filtering Rules
            if (!isPackageUserFacing(packageName, pm)) continue

            // Ensure it has a valid label
            val appLabel = pm.getApplicationLabel(app).toString()
            if (appLabel.isBlank()) continue

            val totalMB = stats.total / (1024.0 * 1024.0)
            val fgMB = stats.foreground / (1024.0 * 1024.0)
            
            appsList.add(AppAnalytics(
                appPackageName = packageName,
                appName = appLabel,
                totalUsageMB = totalMB,
                foregroundUsageMB = fgMB,
                backgroundUsageMB = totalMB - fgMB,
                lastUpdated = Timestamp.now()
            ))
        }

        // 4. Tier Assignment Logic (From Priority Engine)
        val latestDecision = PriorityRepository.latestDecision.value
        val appTiers = mutableMapOf<String, String>()
        latestDecision?.tiers?.forEach { (tierKey, packageNames) ->
            val tierName = when (tierKey) {
                "TIER1" -> "Tier 1 (Critical)"
                "TIER2" -> "Tier 2 (High)"
                "TIER3" -> "Tier 3 (Normal)"
                "TIER4" -> "Tier 4 (Low)"
                else -> ""
            }
            if (tierName.isNotEmpty()) {
                packageNames.forEach { pkg -> appTiers[pkg] = tierName }
            }
        }

        val updatedApps = appsList.map { app ->
            app.copy(tier = appTiers[app.appPackageName] ?: "")
        }

        // 5. Firestore Write Logic (Merge/Update)
        appsDocRef.update("lastUpdated", Timestamp.now()).await()
        val appColl = appsDocRef.collection("applications")
        
        var batch = firestore.batch()
        var count = 0
        for (app in updatedApps) {
            batch.set(appColl.document(app.appPackageName), app)
            count++
            if (count >= 450) {
                batch.commit().await()
                batch = firestore.batch()
                count = 0
            }
        }
        if (count > 0) batch.commit().await()
    }

    private suspend fun deleteSubCollection(collectionRef: com.google.firebase.firestore.CollectionReference) {
        val snapshot = collectionRef.get().await()
        var batch = firestore.batch()
        var count = 0
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
            count++
            if (count >= 450) {
                batch.commit().await()
                batch = firestore.batch()
                count = 0
            }
        }
        if (count > 0) batch.commit().await()
    }

    private data class AppUsageStats(var total: Long = 0, var foreground: Long = 0)


    override suspend fun getCurrentAnalytics(): AnalyticsSnapshot {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val pm = context.packageManager
        
        // 1. Get Network Usage Data
        val monthlyUsage = DataUsageRepository.getMonthlyUsage(context)
        val dailyUsageList = monthlyUsage.dailyUsage

        // Daily Usage (last entry)
        val today = dailyUsageList.lastOrNull()
        val dailyWifiMB = if (today != null) today.wifiBytes / (1024.0 * 1024.0) else 0.0
        val dailyMobileMB = if (today != null) today.mobileBytes / (1024.0 * 1024.0) else 0.0
        val dailyTotalMB = dailyWifiMB + dailyMobileMB

        // Weekly Usage (sum of last 7 entries)
        val last7Days = dailyUsageList.takeLast(7)
        val weeklyWifiMB = last7Days.sumOf { it.wifiBytes } / (1024.0 * 1024.0)
        val weeklyMobileMB = last7Days.sumOf { it.mobileBytes } / (1024.0 * 1024.0)
        val weeklyTotalMB = weeklyWifiMB + weeklyMobileMB

        // 2. Calculate Overall Foreground vs Background Ratio for Today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        var totalFgBytes = 0L
        var totalBgBytes = 0L

        fun queryGlobalStats(type: Int) {
            try {
                nsm.querySummary(type, null, startTime, endTime).use { stats ->
                    val bucket = NetworkStats.Bucket()
                    while (stats.hasNextBucket()) {
                        stats.getNextBucket(bucket)
                        
                        // Apply user-facing filter to ensure we match "meaningful user applications" requirement
                        val packageName = pm.getNameForUid(bucket.uid) ?: continue
                        if (!isPackageUserFacing(packageName, pm)) continue

                        val bytes = bucket.rxBytes + bucket.txBytes
                        if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) {
                            totalFgBytes += bytes
                        } else {
                            totalBgBytes += bytes
                        }
                    }
                }
            } catch (e: Exception) { }
        }

        queryGlobalStats(ConnectivityManager.TYPE_WIFI)
        queryGlobalStats(ConnectivityManager.TYPE_MOBILE)

        val fgMB = totalFgBytes / (1024.0 * 1024.0)
        val bgMB = totalBgBytes / (1024.0 * 1024.0)
        val networkTotalMB = fgMB + bgMB
        
        val fgPercent = if (networkTotalMB > 0) (fgMB / networkTotalMB) * 100.0 else 0.0
        val bgPercent = if (networkTotalMB > 0) (bgMB / networkTotalMB) * 100.0 else 0.0

        // 3. Top 5 Peak Days
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val top5PeakDays = dailyUsageList
            .sortedByDescending { it.wifiBytes + it.mobileBytes }
            .take(5)
            .map { 
                PeakUsageDay(
                    date = dateFormat.format(it.date),
                    usageMB = (it.wifiBytes + it.mobileBytes) / (1024 * 1024)
                )
            }

        // 4. Get Network Metrics (Latency/Packet Loss)
        val metricsHistory = PredictionRepository.metricsHistory.value
        val avgLatency = if (metricsHistory.isNotEmpty()) metricsHistory.map { it.latency }.average() else 0.0
        val avgPacketLoss = if (metricsHistory.isNotEmpty()) metricsHistory.map { it.packetLoss }.average() else 0.0

        return AnalyticsSnapshot(
            dailyUsageMB = dailyTotalMB,
            dailyWifiUsageMB = dailyWifiMB,
            dailyMobileUsageMB = dailyMobileMB,
            weeklyUsageMB = weeklyTotalMB,
            weeklyWifiUsageMB = weeklyWifiMB,
            weeklyMobileUsageMB = weeklyMobileMB,
            totalForegroundUsageMB = fgMB,
            totalBackgroundUsageMB = bgMB,
            foregroundUsagePercentage = fgPercent,
            backgroundUsagePercentage = bgPercent,
            top5PeakDays = top5PeakDays,
            avgLatencyMs = avgLatency,
            avgPacketLossPercent = avgPacketLoss
        )
    }

    private fun isPackageUserFacing(packageName: String, pm: PackageManager): Boolean {
        val whitelist = setOf(
            "com.android.chrome",
            "com.google.android.youtube",
            "com.google.android.apps.messaging"
        )
        if (whitelist.contains(packageName)) return true
        
        if (packageName == "android" || packageName.startsWith("android.") ||
            packageName.startsWith("com.android.systemui") ||
            packageName == "com.google.android.gms" || packageName == "com.google.android.gsf") return false
        
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            val launchIntent = pm.getLaunchIntentForPackage(packageName)

            if (packageName.startsWith("com.android.") && launchIntent == null) return false
            if ((isSystem || isUpdatedSystem) && launchIntent == null) return false
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
