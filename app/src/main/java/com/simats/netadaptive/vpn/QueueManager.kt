package com.simats.netadaptive.vpn

import com.simats.netadaptive.ml.AppTier
import com.simats.netadaptive.vpn.models.TierPolicy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class QueuedPacket(
    val uid: Int,
    val rawBytes: ByteArray,
    val enqueuedAtMs: Long = System.currentTimeMillis()
)

@Singleton
class QueueManager @Inject constructor() {

    private val queues: Map<AppTier, ArrayDeque<QueuedPacket>> = mapOf(
        AppTier.CRITICAL to ArrayDeque(),
        AppTier.HIGH     to ArrayDeque(),
        AppTier.NORMAL   to ArrayDeque(),
        AppTier.LOW      to ArrayDeque()
    )
    private val locks = AppTier.entries.associateWith { Mutex() }

    suspend fun enqueue(tier: AppTier, packet: QueuedPacket): Boolean {
        val policy = TierPolicy.defaults[tier] ?: return false
        if (policy.maxQueueSizeBytes == 0) return false
        
        locks[tier]!!.withLock {
            val queue = queues[tier]!!
            val totalBytes = queue.sumOf { it.rawBytes.size }
            if (totalBytes + packet.rawBytes.size > policy.maxQueueSizeBytes)
                return false
            queue.addLast(packet)
            return true
        }
    }

    suspend fun dequeue(tier: AppTier): QueuedPacket? =
        locks[tier]!!.withLock { queues[tier]?.removeFirstOrNull() }

    suspend fun flush(tier: AppTier): List<QueuedPacket> =
        locks[tier]!!.withLock {
            val all = queues[tier]!!.toList()
            queues[tier]!!.clear()
            all
        }

    suspend fun dropExpired() {
        val nowMs = System.currentTimeMillis()
        for (tier in AppTier.entries) {
            val policy = TierPolicy.defaults[tier] ?: continue
            if (policy.maxQueueAgeMs == 0L) continue
            locks[tier]!!.withLock {
                queues[tier]!!.removeAll { nowMs - it.enqueuedAtMs > policy.maxQueueAgeMs }
            }
        }
    }

    fun queueCount(): Map<AppTier, Int> =
        queues.mapValues { it.value.size }
}
