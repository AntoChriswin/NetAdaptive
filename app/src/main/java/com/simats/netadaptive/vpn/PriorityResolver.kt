package com.simats.netadaptive.vpn

import android.content.Context
import android.util.Log
import com.simats.netadaptive.ml.AppTier
import com.simats.netadaptive.ml.StressScoreProvider
import com.simats.netadaptive.vpn.models.AppRule
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriorityResolver @Inject constructor(
    private val uidMapper: AppUidMapper,
    private val stressScoreProvider: StressScoreProvider,
    @ApplicationContext private val context: Context
) {
    fun resolve(uid: Int): AppRule {
        val pkg   = uidMapper.resolvePackage(uid)
        val tier  = uidMapper.getTier(pkg)
        val stress = stressScoreProvider.getCurrentScore()
        return AppRule(packageName = pkg, tier = tier, stressScore = stress)
    }

    fun onTiersUpdated(newTiers: Map<String, AppTier>) {
        uidMapper.updateTierMapping(newTiers)
        // Refresh VPN to apply new Split-Tunneling rules
        try {
            context.startService(NetAdaptiveVpnService.updateIntent(context))
        } catch (e: Exception) {
            Log.e("VPN_RESOLVER", "Failed to refresh VPN tunnel", e)
        }
    }
}
