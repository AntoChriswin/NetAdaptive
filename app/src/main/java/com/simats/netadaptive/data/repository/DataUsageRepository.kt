package com.simats.netadaptive.data.repository

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import java.util.*

object DataUsageRepository {
    
    data class MonthlyUsage(
        val totalWiFiBytes: Long,
        val totalMobileBytes: Long,
        val dailyUsage: List<DailyUsagePoint>,
        val lastMonthTotalBytes: Long
    )

    data class DailyUsagePoint(
        val dayOfMonth: Int,
        val wifiBytes: Long,
        val mobileBytes: Long,
        val date: Date
    )

    fun getMonthlyUsage(context: Context): MonthlyUsage {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.timeInMillis
        val now = System.currentTimeMillis()
        
        // Last month
        val lastMonthCalendar = calendar.clone() as Calendar
        lastMonthCalendar.add(Calendar.MONTH, -1)
        val lastMonthStart = lastMonthCalendar.timeInMillis
        val lastMonthEnd = monthStart - 1

        val wifiTotal = queryTotal(nsm, ConnectivityManager.TYPE_WIFI, monthStart, now)
        val mobileTotal = queryTotal(nsm, ConnectivityManager.TYPE_MOBILE, monthStart, now)
        
        val lastMonthWiFi = queryTotal(nsm, ConnectivityManager.TYPE_WIFI, lastMonthStart, lastMonthEnd)
        val lastMonthMobile = queryTotal(nsm, ConnectivityManager.TYPE_MOBILE, lastMonthStart, lastMonthEnd)
        
        val dailyPoints = mutableListOf<DailyUsagePoint>()
        val dayIter = calendar.clone() as Calendar
        while (dayIter.timeInMillis <= now) {
            val startOfDay = dayIter.timeInMillis
            val endOfDay = dayIter.apply { 
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis
            
            val dWifi = queryTotal(nsm, ConnectivityManager.TYPE_WIFI, startOfDay, endOfDay)
            val dMobile = queryTotal(nsm, ConnectivityManager.TYPE_MOBILE, startOfDay, endOfDay)
            
            dailyPoints.add(DailyUsagePoint(
                dayOfMonth = dayIter.get(Calendar.DAY_OF_MONTH),
                wifiBytes = dWifi,
                mobileBytes = dMobile,
                date = dayIter.time
            ))
            
            dayIter.add(Calendar.DAY_OF_MONTH, 1)
            dayIter.set(Calendar.HOUR_OF_DAY, 0)
        }

        return MonthlyUsage(
            totalWiFiBytes = wifiTotal,
            totalMobileBytes = mobileTotal,
            dailyUsage = dailyPoints,
            lastMonthTotalBytes = lastMonthWiFi + lastMonthMobile
        )
    }

    data class HourlyUsagePoint(
        val hour: Int,
        val fgBytes: Long,
        val bgBytes: Long
    )

    fun getTodayHourlyUsage(context: Context): List<HourlyUsagePoint> {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val points = mutableListOf<HourlyUsagePoint>()
        val iter = calendar.clone() as Calendar

        while (iter.timeInMillis <= now) {
            val start = iter.timeInMillis
            val end = iter.apply { add(Calendar.HOUR_OF_DAY, 1) }.timeInMillis - 1
            
            // Querying hourly is expensive, but for today it's manageable.
            // Simplified: we'll query for the device total as a proxy for the trend.
            // In a real app, we'd query per UID if we wanted accurate FG/BG split here,
            // but for a dashboard trend, device totals are often enough.
            
            // To get FG/BG split for the chart, we actually need queryDetails or querySummary
            // that includes STATE.
            
            var fg = 0L
            var bg = 0L
            
            try {
                // WiFi
                nsm.querySummary(ConnectivityManager.TYPE_WIFI, null, start, end).use { stats ->
                    val bucket = NetworkStats.Bucket()
                    while (stats.hasNextBucket()) {
                        stats.getNextBucket(bucket)
                        if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) fg += (bucket.rxBytes + bucket.txBytes)
                        else bg += (bucket.rxBytes + bucket.txBytes)
                    }
                }
                // Mobile
                nsm.querySummary(ConnectivityManager.TYPE_MOBILE, null, start, end).use { stats ->
                    val bucket = NetworkStats.Bucket()
                    while (stats.hasNextBucket()) {
                        stats.getNextBucket(bucket)
                        if (bucket.state == NetworkStats.Bucket.STATE_FOREGROUND) fg += (bucket.rxBytes + bucket.txBytes)
                        else bg += (bucket.rxBytes + bucket.txBytes)
                    }
                }
            } catch (e: Exception) { }

            points.add(HourlyUsagePoint(iter.get(Calendar.HOUR_OF_DAY) - 1, fg, bg))
        }
        return points
    }

    private fun queryTotal(nsm: NetworkStatsManager, type: Int, start: Long, end: Long): Long {
        return try {
            val bucket = nsm.querySummaryForDevice(type, null, start, end)
            bucket.rxBytes + bucket.txBytes
        } catch (e: Exception) {
            0L
        }
    }
}
