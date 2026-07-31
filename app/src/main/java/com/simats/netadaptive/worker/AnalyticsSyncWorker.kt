package com.simats.netadaptive.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simats.netadaptive.data.repository.AnalyticsFirestoreRepository

/**
 * WorkManager worker that synchronizes analytics data with Firestore every 30 minutes.
 */
class AnalyticsSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = AnalyticsFirestoreRepository(context)

    override suspend fun doWork(): Result {
        Log.d("AnalyticsSync", "AnalyticsSyncWorker: Execution started")
        
        return try {
            repository.uploadAnalytics()
            Result.success()
        } catch (e: Exception) {
            Log.e("AnalyticsSync", "AnalyticsSyncWorker: Execution failed", e)
            Result.retry()
        }
    }
}
