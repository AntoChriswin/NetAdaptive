package com.simats.netadaptive.data.repository

import android.util.Log
import com.simats.netadaptive.engine.gemini.GeminiDecision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PriorityRepository {
    private val _latestDecision = MutableStateFlow<GeminiDecision?>(null)
    val latestDecision = _latestDecision.asStateFlow()

    private val _isAutoMode = MutableStateFlow(true)
    val isAutoMode = _isAutoMode.asStateFlow()

    private val _manualDecision = MutableStateFlow<GeminiDecision?>(null)
    val manualDecision = _manualDecision.asStateFlow()

    fun updateDecision(decision: GeminiDecision) {
        if (_isAutoMode.value) {
            Log.d("PriorityRepository", "Updating AI decision: ${decision.tiers.values.flatten().size} apps assigned")
            _latestDecision.value = decision
        } else {
            Log.d("PriorityRepository", "AI decision ignored - Manual mode active")
        }
    }

    fun setAutoMode(enabled: Boolean) {
        Log.e("PriorityRepository", "Mode changed: ${if (enabled) "AUTO" else "MANUAL"}")
        _isAutoMode.value = enabled
    }

    fun saveManualDecision(decision: GeminiDecision) {
        Log.e("PriorityRepository", "Manual decision saved: ${decision.tiers.values.flatten().size} apps")
        _manualDecision.value = decision
        _latestDecision.value = decision
        _isAutoMode.value = false
    }
}
