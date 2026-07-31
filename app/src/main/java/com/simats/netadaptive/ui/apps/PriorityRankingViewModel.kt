package com.simats.netadaptive.ui.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.netadaptive.data.repository.AppUsageRepository
import com.simats.netadaptive.data.repository.PriorityRepository
import com.simats.netadaptive.engine.gemini.GeminiDecision
import com.simats.netadaptive.ml.AppTier
import com.simats.netadaptive.vpn.AppUidMapper
import com.simats.netadaptive.vpn.PriorityResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriorityRankingViewModel @Inject constructor(
    private val vpnPriorityResolver: PriorityResolver,
    private val uidMapper: AppUidMapper
) : ViewModel() {

    val isAutoMode = PriorityRepository.isAutoMode

    private val _pendingTiers = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val pendingTiers = _pendingTiers.asStateFlow()

    init {
        // Continuous sync with AI results while in Auto mode
        viewModelScope.launch {
            PriorityRepository.latestDecision.collect { decision ->
                if (decision != null && PriorityRepository.isAutoMode.value) {
                    Log.d("PriorityRanking", "Auto mode: Syncing UI with new AI decision")
                    _pendingTiers.value = decision.tiers
                }
            }
        }
    }

    val tieredApps = combine(
        AppUsageRepository.appsUsage,
        _pendingTiers
    ) { usageApps, tiers ->
        val appMap = usageApps.associateBy { it.name }
        
        var globalRank = 1
        tiers.mapValues { (tier, appNames) ->
            appNames.mapNotNull { appName ->
                val usageData = appMap[appName]
                if (usageData != null) {
                    PriorityApp(
                        name = usageData.name,
                        usage = "${usageData.usageDisplay} used today",
                        packageName = usageData.packageName,
                        rank = globalRank++
                    )
                } else {
                    // Even if usage data is missing (not active today), we still show it in manual mode
                    PriorityApp(
                        name = appName,
                        usage = "0 B used today",
                        packageName = "",
                        rank = globalRank++
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val availableApps = AppUsageRepository.appsUsage.map { apps ->
        apps.map { it.name }.sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAutoMode(enabled: Boolean) {
        Log.e("PriorityRanking", "User toggled mode to: ${if (enabled) "AUTO" else "MANUAL"}")
        PriorityRepository.setAutoMode(enabled)
        if (enabled) {
            // Immediately force UI sync with latest AI data when switching back to Auto
            PriorityRepository.latestDecision.value?.let {
                Log.d("PriorityRanking", "Forced UI sync with AI data")
                _pendingTiers.value = it.tiers
            }
        }
    }

    fun addAppToTier(appName: String, tier: String) {
        Log.d("PriorityRanking", "Adding $appName to $tier")
        val current = _pendingTiers.value.toMutableMap()
        
        // Remove from any other tier first
        current.forEach { (key, list) ->
            current[key] = list.filter { it != appName }
        }
        
        val list = current[tier]?.toMutableList() ?: mutableListOf()
        if (!list.contains(appName)) {
            list.add(appName)
            current[tier] = list
            _pendingTiers.value = current
            Log.e("PriorityRanking", "Successfully added $appName to $tier. Total in $tier: ${list.size}")
        }
    }

    fun saveChanges() {
        val decision = GeminiDecision(
            tiers = _pendingTiers.value,
            reasons = _pendingTiers.value.values.flatten().associateWith { "Manually assigned by user" }
        )
        Log.e("PriorityRanking", "Saving manual priority ranking. Switching to MANUAL mode.")
        PriorityRepository.saveManualDecision(decision)

        // Sync with VPN
        val appUsageList = AppUsageRepository.appsUsage.value
        val newTiers = mutableMapOf<String, AppTier>()
        _pendingTiers.value.forEach { (tierKey, appNames) ->
            val tier = when (tierKey) {
                "TIER1" -> AppTier.CRITICAL
                "TIER2" -> AppTier.HIGH
                "TIER3" -> AppTier.NORMAL
                "TIER4" -> AppTier.LOW
                else -> AppTier.NORMAL
            }
            appNames.forEach { name ->
            val pkg = appUsageList.find { it.name == name }?.packageName 
            if (pkg != null) {
                newTiers[pkg] = tier
                Log.d("PRIORITY_SAVE", "Mapped $name to $pkg in $tier")
            } else {
                Log.e("PRIORITY_SAVE", "Could not find package ID for $name")
            }
        }
        }
        vpnPriorityResolver.onTiersUpdated(newTiers)
        uidMapper.invalidateCache()
    }
}

data class PriorityApp(
    val name: String,
    val usage: String,
    val packageName: String,
    val rank: Int
)
