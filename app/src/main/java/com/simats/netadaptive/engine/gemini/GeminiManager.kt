package com.simats.netadaptive.engine.gemini

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.simats.netadaptive.core.constants.Config
import com.simats.netadaptive.engine.model.AppUsageWindow
import com.simats.netadaptive.engine.model.AppState
import org.json.JSONArray
import org.json.JSONObject

data class GeminiDecision(
    val tiers: Map<String, List<String>>,
    val reasons: Map<String, String>
)

class GeminiManager {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = Config.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.1f
            topK = 32
            topP = 1f
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text("You are a Network Priority Manager for an Android device. " +
                 "Your task is to categorize apps into 4 priority Tiers based on provided network metrics and app activity. " +
                 "TIER1 (Critical): Max 3 apps. Foreground, high-interaction, or critical streaming/gaming. " +
                 "TIER2 (High): Max 3 apps. Active usage, communication, or browsers. " +
                 "TIER3 (Normal): Max 5 apps. Standard background activity. " +
                 "TIER4 (Low): Max 3 apps. Background sync, updates, or low-priority tasks. " +
                 "Return your response ONLY as a valid JSON object with 'tiers' (mapping tier names to lists of app names) " +
                 "and 'reasons' (mapping app names to short justifications).")
        }
    )

    suspend fun decideTiersDynamically(window: AppUsageWindow): GeminiDecision {
        val payload = buildPayload(window)
        Log.d("GEMINI_PHASE2", "Sending Request to Gemini: $payload")

        return try {
            val response = generativeModel.generateContent(payload)
            val responseText = response.text ?: throw Exception("Empty response from Gemini")
            Log.d("GEMINI_PHASE2", "Gemini Response: $responseText")

            parseGeminiResponse(responseText)
        } catch (e: Exception) {
            Log.e("GEMINI_PHASE2", "Gemini API failed: ${e.message}. Falling back to local reasoning.", e)
            performLocalFallback(window)
        }
    }

    private fun buildPayload(window: AppUsageWindow): String {
        return JSONObject().apply {
            put("predictedLatency", window.predictedLatency)
            put("predictedPacketLoss", window.predictedPacketLoss)
            val appsArray = JSONArray()
            window.apps.forEach { app ->
                appsArray.put(JSONObject().apply {
                    put("app", app.appName)
                    put("usageMB", app.usageMB)
                    put("foregroundMs", app.foregroundMs)
                    put("avgBandwidth", app.avgBandwidth)
                    put("interactionScore", app.interactionScore)
                    put("state", app.state.name)
                })
            }
            put("apps", appsArray)
        }.toString()
    }

    private fun parseGeminiResponse(json: String): GeminiDecision {
        val obj = JSONObject(json)
        val tiersObj = obj.getJSONObject("tiers")
        val reasonsObj = obj.optJSONObject("reasons") ?: JSONObject()

        val tiers = mutableMapOf<String, List<String>>()
        val tierKeys = listOf("TIER1", "TIER2", "TIER3", "TIER4")
        
        tierKeys.forEach { key ->
            val appList = mutableListOf<String>()
            if (tiersObj.has(key)) {
                val array = tiersObj.getJSONArray(key)
                for (i in 0 until array.length()) {
                    appList.add(array.getString(i))
                }
            }
            tiers[key] = appList
        }

        val reasons = mutableMapOf<String, String>()
        reasonsObj.keys().forEach { appName ->
            reasons[appName] = reasonsObj.getString(appName)
        }

        return GeminiDecision(tiers, reasons)
    }

    fun performLocalFallback(window: AppUsageWindow): GeminiDecision {
        // Implementation of your Phase 1 local rules
        val tiers = mutableMapOf(
            "TIER1" to mutableListOf<String>(),
            "TIER2" to mutableListOf<String>(),
            "TIER3" to mutableListOf<String>(),
            "TIER4" to mutableListOf<String>()
        )
        val reasons = mutableMapOf<String, String>()

        window.apps.forEach { app ->
            val score = (app.interactionScore * 0.4f) + (app.usageMB / 100f).coerceAtMost(0.6f)
            val tier = when {
                app.state == AppState.FOREGROUND && score > 0.5f -> "TIER1"
                app.usageMB > 5f || app.state == AppState.FOREGROUND -> "TIER2"
                app.usageMB < 1f && app.state == AppState.BACKGROUND -> "TIER4"
                else -> "TIER3"
            }
            
            val limit = when(tier) { "TIER1" -> 3; "TIER2" -> 3; "TIER3" -> 5; else -> 3 }
            if (tiers[tier]!!.size < limit) {
                tiers[tier]!!.add(app.appName)
                reasons[app.appName] = "Local fallback: based on interaction/usage score"
            }
        }
        return GeminiDecision(tiers, reasons)
    }
}
