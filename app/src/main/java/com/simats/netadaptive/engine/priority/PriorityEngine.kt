package com.simats.netadaptive.engine.priority

import android.util.Log
import com.simats.netadaptive.core.constants.Config
import com.simats.netadaptive.data.repository.PriorityRepository
import com.simats.netadaptive.engine.gemini.GeminiDecision
import com.simats.netadaptive.engine.gemini.GeminiManager
import com.simats.netadaptive.engine.model.AppUsageWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PriorityEngine {
    
    private val geminiManager = GeminiManager()

    init {
        Log.d("PriorityEngine", "Priority Engine initialized")
    }

    suspend fun processWindow(window: AppUsageWindow) = withContext(Dispatchers.Default) {
        if (!PriorityRepository.isAutoMode.value) {
            Log.w("PriorityEngine", "█ AI ENGINE PAUSED: Manual mode is active █")
            return@withContext
        }

        Log.e("AI_INPUT", "█ WINDOW GENERATED: ${window.apps.size} active apps █")
        
        // 1. Try Gemini primary
        var decision: GeminiDecision? = null
        
        if (Config.USE_GEMINI_PRIORITY) {
            decision = geminiManager.decideTiersDynamically(window)
        }
        
        // 2. Fallback if Gemini was disabled
        if (decision == null) {
            Log.w("PriorityEngine", "█ AI INFERENCE DISABLED: Using local reasoning █")
            decision = geminiManager.performLocalFallback(window)
        }
        
        // Update Repository for UI visibility
        PriorityRepository.updateDecision(decision)
        
        Log.e("AI_OUTPUT", "█ DYNAMIC TIER ASSIGNMENT COMPLETE █")
        
        Log.e("NET_TIER", buildString {
            append("\nTIER Summary:\n")
            append("TIER1: ${decision.tiers["TIER1"]?.size ?: 0} apps\n")
            append("TIER2: ${decision.tiers["TIER2"]?.size ?: 0} apps\n")
            append("TIER3: ${decision.tiers["TIER3"]?.size ?: 0} apps\n")
            append("TIER4: ${decision.tiers["TIER4"]?.size ?: 0} apps\n")
        })
    }

    fun updateTiers() {
        // This is a hook for the LSTM cycle to trigger any additional tier-related updates
        Log.d("AI_POLICY", "PriorityEngine.updateTiers() called")
    }

    fun getCurrentTiers(): Map<String, com.simats.netadaptive.ml.AppTier> {
        val decision = PriorityRepository.latestDecision.value ?: return emptyMap()
        val result = mutableMapOf<String, com.simats.netadaptive.ml.AppTier>()
        decision.tiers.forEach { (tierKey, apps) ->
            val tier = when (tierKey) {
                "TIER1" -> com.simats.netadaptive.ml.AppTier.CRITICAL
                "TIER2" -> com.simats.netadaptive.ml.AppTier.HIGH
                "TIER3" -> com.simats.netadaptive.ml.AppTier.NORMAL
                "TIER4" -> com.simats.netadaptive.ml.AppTier.LOW
                else -> com.simats.netadaptive.ml.AppTier.NORMAL
            }
            apps.forEach { result[it] = tier }
        }
        return result
    }

    fun close() {
        // No resources to close currently
    }
}
