package com.simats.netadaptive.vpn

import android.util.Log
import com.simats.netadaptive.vpn.models.AppRule
import com.simats.netadaptive.vpn.models.TrafficAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrafficController @Inject constructor(
    private val queueManager: QueueManager,
    private val forwarder: PacketForwarder,
    private val metricsManager: MetricsManager
) {
    /**
     * REAL ENFORCEMENT MODE: Applies delays and restrictions.
     */
    suspend fun handle(
        packet: PacketInterceptor.ParsedPacket,
        rule: AppRule,
        action: TrafficAction
    ) {
        // DNS Guard: Always allow DNS traffic
        if (packet.dstPort == 53 || packet.srcPort == 53) {
            forwarder.forward(packet.rawBytes)
            return
        }

        metricsManager.recordAction(action, rule.tier)

        when (action) {
            TrafficAction.ALLOW, TrafficAction.ALLOW_WITH_MONITORING -> {
                forwarder.forward(packet.rawBytes)
            }

            TrafficAction.DELAY -> {
                val queued = queueManager.enqueue(
                    rule.tier,
                    QueuedPacket(uid = packet.uid, rawBytes = packet.rawBytes)
                )
                if (!queued) {
                    forwarder.forward(packet.rawBytes)
                } else {
                    Log.d("VPN_ENFORCE", "DELAYING: Queued packet for ${rule.packageName}")
                }
            }

            TrafficAction.RESTRICT -> {
                // DROP the packet to simulate internet loss for this app
                Log.e("VPN_ENFORCE", "RESTRICTING: Dropping packet from ${rule.packageName} (Tier: ${rule.tier})")
                // We do not call forwarder.forward() here.
            }
        }
    }
}
