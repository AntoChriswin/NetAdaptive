package com.simats.netadaptive.vpn

import com.simats.netadaptive.ml.AppTier
import com.simats.netadaptive.vpn.models.AppRule
import com.simats.netadaptive.vpn.models.TrafficAction
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleEngineTest {
    private val ruleEngine = RuleEngine()

    @Test
    fun testCriticalAlwaysAllowed() {
        val rule = AppRule("pkg", AppTier.CRITICAL, 0.9f)
        assertEquals(TrafficAction.ALLOW, ruleEngine.evaluate(rule))
    }

    @Test
    fun testHighAlwaysMonitored() {
        val rule = AppRule("pkg", AppTier.HIGH, 0.1f)
        assertEquals(TrafficAction.ALLOW_WITH_MONITORING, ruleEngine.evaluate(rule))
    }

    @Test
    fun testNormalStressDecision() {
        val lowStress = AppRule("pkg", AppTier.NORMAL, 0.5f)
        val highStress = AppRule("pkg", AppTier.NORMAL, 0.8f)
        assertEquals(TrafficAction.ALLOW, ruleEngine.evaluate(lowStress))
        assertEquals(TrafficAction.DELAY, ruleEngine.evaluate(highStress))
    }

    @Test
    fun testLowStressDecision() {
        val lowStress = AppRule("pkg", AppTier.LOW, 0.3f)
        val highStress = AppRule("pkg", AppTier.LOW, 0.7f)
        assertEquals(TrafficAction.DELAY, ruleEngine.evaluate(lowStress))
        assertEquals(TrafficAction.RESTRICT, ruleEngine.evaluate(highStress))
    }
}
