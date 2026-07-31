package com.simats.netadaptive.vpn

import com.simats.netadaptive.ml.AppTier
import com.simats.netadaptive.vpn.models.AppRule
import com.simats.netadaptive.vpn.models.TierPolicy
import com.simats.netadaptive.vpn.models.TrafficAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor() {
    /**
     * ENFORCEMENT MODE
     */
    fun evaluate(rule: AppRule): TrafficAction {
        val policy = TierPolicy.defaults[rule.tier]
            ?: return TrafficAction.ALLOW

        return when (rule.tier) {
            AppTier.CRITICAL -> TrafficAction.ALLOW

            AppTier.HIGH     -> TrafficAction.ALLOW_WITH_MONITORING

            AppTier.NORMAL   ->
                if (rule.stressScore >= 0.50f)
                    TrafficAction.DELAY
                else
                    TrafficAction.ALLOW

            AppTier.LOW      ->
                // FORCE RESTRICT for testing: ignore score, just block if in LOW
                TrafficAction.RESTRICT
        }
    }
}
