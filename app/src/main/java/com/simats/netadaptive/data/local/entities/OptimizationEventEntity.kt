package com.simats.netadaptive.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "optimization_events")
data class OptimizationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val appName: String,
    val actionType: String,
    val value: Float,
    val unit: String
)
