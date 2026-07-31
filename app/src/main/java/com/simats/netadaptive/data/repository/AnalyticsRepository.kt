package com.simats.netadaptive.data.repository

import com.simats.netadaptive.data.model.AnalyticsSnapshot

/**
 * Interface for Analytics operations.
 */
interface AnalyticsRepository {
    /**
     * Gathers local data and uploads to Firestore.
     */
    suspend fun uploadAnalytics()

    /**
     * Retrieves the current analytics snapshot from local sources.
     */
    suspend fun getCurrentAnalytics(): AnalyticsSnapshot
}
