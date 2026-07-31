package com.simats.netadaptive.vpn

import com.simats.netadaptive.ml.AppTier
import com.simats.netadaptive.ml.StressScoreProvider
import com.simats.netadaptive.vpn.models.TrafficAction
import com.simats.netadaptive.vpn.models.VpnMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsManager @Inject constructor(
    private val queueManager: QueueManager,
    private val stressScoreProvider: StressScoreProvider
) {
    private val _metrics = MutableStateFlow(VpnMetrics())
    val metrics: StateFlow<VpnMetrics> = _metrics.asStateFlow()

    private val forwarded  = AtomicLong(0)
    private val delayed    = AtomicLong(0)
    private val blocked    = AtomicLong(0)
    private val tierCounts = ConcurrentHashMap<AppTier, AtomicLong>()
        .apply { AppTier.entries.forEach { put(it, AtomicLong(0)) } }

    fun recordAction(action: TrafficAction, tier: AppTier) {
        when (action) {
            TrafficAction.ALLOW,
            TrafficAction.ALLOW_WITH_MONITORING -> forwarded.incrementAndGet()
            TrafficAction.DELAY                 -> delayed.incrementAndGet()
            TrafficAction.RESTRICT              -> blocked.incrementAndGet()
        }
        tierCounts[tier]?.incrementAndGet()
    }

    fun publish() {
        _metrics.value = VpnMetrics(
            forwardedPackets  = forwarded.get(),
            delayedPackets    = delayed.get(),
            blockedPackets    = blocked.get(),
            activeQueues      = queueManager.queueCount().count { it.value > 0 },
            currentStressScore = stressScoreProvider.getCurrentScore(),
            appTierUsage       = tierCounts.mapValues { it.value.get() }
        )
    }
}
