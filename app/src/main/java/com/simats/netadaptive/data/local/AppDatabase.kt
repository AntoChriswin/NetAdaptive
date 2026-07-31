package com.simats.netadaptive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.simats.netadaptive.data.local.dao.AnalyticsDao
import com.simats.netadaptive.data.local.entities.AnalyticsDailyEntity
import com.simats.netadaptive.data.local.entities.OptimizationEventEntity

@Database(
    entities = [AnalyticsDailyEntity::class, OptimizationEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analyticsDao(): AnalyticsDao
}
