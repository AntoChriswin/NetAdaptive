package com.simats.netadaptive.vpn.models

import com.simats.netadaptive.ml.AppTier

data class AppRule(
    val packageName: String,
    val tier: AppTier,
    val stressScore: Float        // 0.0–1.0 from existing StressScoreProvider
)
