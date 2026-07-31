package com.simats.netadaptive.engine.priority

import android.util.Log

data class AppPriorityState(
    val packageName: String,
    val appName: String,
    var currentTier: String,
    var retentionCounter: Int = 0,
    var lastSessionUsage: Long = 0,
    var lastForeground: Boolean = false,
    var lastSeenTime: Long = System.currentTimeMillis()
)

class PriorityStateManager {
    private val appStates = mutableMapOf<String, AppPriorityState>()

    private val retentionLimits = mapOf(
        "TIER1" to 3,
        "TIER2" to 2,
        "TIER3" to 2,
        "TIER4" to 1
    )

    fun getStoredState(packageName: String): AppPriorityState? = appStates[packageName]

    fun updateAppState(packageName: String, appName: String, newTier: String, isForeground: Boolean, sessionUsage: Long) {
        val existing = appStates[packageName]
        if (existing != null) {
            val previousTier = existing.currentTier
            
            // Promotion/Demotion Logs (Step 5)
            if (previousTier != newTier) {
                if (isPromotion(previousTier, newTier)) {
                    Log.d("TIER_PROMOTION", "\n$appName\n$previousTier -> $newTier")
                } else {
                    Log.d("TIER_DEMOTION", "\n$appName\n$previousTier -> $newTier")
                }
            }

            // TIER_MEMORY Log (Step 5)
            Log.d("TIER_MEMORY", "\n$appName\nprevious=$previousTier\ncurrent=$newTier\nretained=true")

            existing.currentTier = newTier
            existing.lastForeground = isForeground
            existing.lastSessionUsage = sessionUsage
            existing.retentionCounter = 0
            existing.lastSeenTime = System.currentTimeMillis()
        } else {
            appStates[packageName] = AppPriorityState(
                packageName = packageName,
                appName = appName,
                currentTier = newTier,
                lastSessionUsage = sessionUsage,
                lastForeground = isForeground
            )
        }
    }

    private fun isPromotion(old: String, new: String): Boolean {
        val oldVal = old.filter { it.isDigit() }.toIntOrNull() ?: 5
        val newVal = new.filter { it.isDigit() }.toIntOrNull() ?: 5
        return newVal < oldVal
    }

    /**
     * Handles retention logic for apps not detected in the current cycle.
     * Returns a list of apps that are still retained via Grace Window.
     */
    fun processGraceWindow(activePackageNames: Set<String>): List<AppPriorityState> {
        val retainedApps = mutableListOf<AppPriorityState>()
        val iterator = appStates.entries.iterator()
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val packageName = entry.key
            val state = entry.value

            if (packageName !in activePackageNames) {
                state.retentionCounter++
                val limit = retentionLimits[state.currentTier] ?: 1

                if (state.retentionCounter >= limit) {
                    Log.d("TIER_REMOVED", state.appName)
                    iterator.remove()
                } else {
                    Log.d("TIER_MEMORY", "\n${state.appName}\nprevious=${state.currentTier}\ncurrent=${state.currentTier}\nretained=true (Grace Window)")
                    retainedApps.add(state)
                }
            }
        }
        return retainedApps
    }

    fun getAllActiveTiers(): Map<String, List<String>> {
        val tiers = mutableMapOf<String, MutableList<String>>()
        appStates.values.forEach { state ->
            tiers.getOrPut(state.currentTier) { mutableListOf() }.add(state.appName)
        }
        return tiers
    }
}
