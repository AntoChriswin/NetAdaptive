package com.simats.netadaptive.ml

import com.simats.netadaptive.data.model.NetworkMetrics
import java.util.*

class SequenceBuffer(private val windowSize: Int = 10) {
    private val buffer: Queue<NetworkMetrics> = LinkedList()

    fun addMetrics(metrics: NetworkMetrics) {
        if (buffer.size >= windowSize) {
            buffer.poll()
        }
        buffer.add(metrics)
    }

    fun getSequence(): List<NetworkMetrics>? {
        return if (buffer.size == windowSize) {
            buffer.toList()
        } else {
            null
        }
    }
    
    fun isReady(): Boolean = buffer.size == windowSize
}
