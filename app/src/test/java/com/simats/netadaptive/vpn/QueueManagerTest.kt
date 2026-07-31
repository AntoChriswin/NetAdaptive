package com.simats.netadaptive.vpn

import com.simats.netadaptive.ml.AppTier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class QueueManagerTest {
    private val queueManager = QueueManager()

    @Test
    fun testEnqueueRespectsSizeLimit() = runBlocking {
        val tier = AppTier.HIGH // 512,000 bytes limit
        val largePacket = QueuedPacket(1001, ByteArray(600_000))
        val result = queueManager.enqueue(tier, largePacket)
        assertFalse(result)
    }

    @Test
    fun testCriticalTierNoQueue() = runBlocking {
        val result = queueManager.enqueue(AppTier.CRITICAL, QueuedPacket(1001, ByteArray(100)))
        assertFalse(result)
    }

    @Test
    fun testFifoOrder() = runBlocking {
        val tier = AppTier.LOW
        val p1 = QueuedPacket(1001, byteArrayOf(1))
        val p2 = QueuedPacket(1001, byteArrayOf(2))
        
        queueManager.enqueue(tier, p1)
        queueManager.enqueue(tier, p2)
        
        assertEquals(p1, queueManager.dequeue(tier))
        assertEquals(p2, queueManager.dequeue(tier))
    }

    @Test
    fun testDropExpired() = runBlocking {
        val tier = AppTier.HIGH // 500ms limit
        val oldPacket = QueuedPacket(1001, byteArrayOf(1), System.currentTimeMillis() - 1000)
        
        queueManager.enqueue(tier, oldPacket)
        queueManager.dropExpired()
        
        assertNull(queueManager.dequeue(tier))
    }
}
