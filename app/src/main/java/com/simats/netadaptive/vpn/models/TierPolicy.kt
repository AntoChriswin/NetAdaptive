package com.simats.netadaptive.vpn.models

import com.simats.netadaptive.ml.AppTier

data class TierPolicy(
    val tier: AppTier,
    val maxQueueSizeBytes: Int,     // 0 = no queue (CRITICAL)
    val maxQueueAgeMs: Long,        // max time a packet may sit in queue
    val stressThreshold: Float,     // 0.0–1.0 — NORMAL/LOW decision boundary
    val allowTemporaryBlock: Boolean
) {
    companion object {
        val defaults: Map<AppTier, TierPolicy> = mapOf(
            AppTier.CRITICAL to TierPolicy(
                AppTier.CRITICAL,
                maxQueueSizeBytes = 0,
                maxQueueAgeMs = 0L,
                stressThreshold = 0f,
                allowTemporaryBlock = false
            ),
            AppTier.HIGH to TierPolicy(
                AppTier.HIGH,
                maxQueueSizeBytes = 1_048_576, // 1MB
                maxQueueAgeMs = 1000L,
                stressThreshold = 0.90f, // Only delay if extremely stressed
                allowTemporaryBlock = false
            ),
            AppTier.NORMAL to TierPolicy(
                AppTier.NORMAL,
                maxQueueSizeBytes = 4_194_304, // 4MB
                maxQueueAgeMs = 3000L,
                stressThreshold = 0.65f,
                allowTemporaryBlock = false
            ),
            AppTier.LOW to TierPolicy(
                AppTier.LOW,
                maxQueueSizeBytes = 16_777_216, // 16MB
                maxQueueAgeMs = 15_000L,
                stressThreshold = 0.40f,
                allowTemporaryBlock = true
            )
        )
    }
}
