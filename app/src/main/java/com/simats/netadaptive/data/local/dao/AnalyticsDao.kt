package com.simats.netadaptive.data.local.dao

import androidx.room.*
import com.simats.netadaptive.data.local.entities.AnalyticsDailyEntity
import com.simats.netadaptive.data.local.entities.OptimizationEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_daily ORDER BY date DESC LIMIT 7")
    fun getWeeklyAnalytics(): Flow<List<AnalyticsDailyEntity>>

    @Query("SELECT * FROM analytics_daily WHERE date = :date")
    suspend fun getAnalyticsForDate(date: String): AnalyticsDailyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyAnalytics(analytics: AnalyticsDailyEntity)

    @Insert
    suspend fun insertOptimizationEvent(event: OptimizationEventEntity)

    @Query("SELECT * FROM optimization_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentOptimizationEvents(): Flow<List<OptimizationEventEntity>>

    @Query("SELECT COUNT(*) FROM optimization_events")
    suspend fun getTotalOptimizationsCount(): Int
}
