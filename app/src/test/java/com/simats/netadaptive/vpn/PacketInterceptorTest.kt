package com.simats.netadaptive.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

class PacketInterceptorTest {
    private val interceptor = PacketInterceptor()

    @Test
    fun testIpv4Parsing() {
        // Minimal IPv4 TCP packet
        val packet = ByteArray(40)
        packet[0] = 0x45.toByte() // Version 4, IHL 5
        packet[9] = 6.toByte()    // TCP
        // Source IP: 192.168.1.1
        packet[12] = 192.toByte(); packet[13] = 168.toByte(); packet[14] = 1.toByte(); packet[15] = 1.toByte()
        // Dest IP: 8.8.8.8
        packet[16] = 8.toByte(); packet[17] = 8.toByte(); packet[18] = 8.toByte(); packet[19] = 8.toByte()
        // Source Port: 12345 (0x3039)
        packet[20] = 0x30.toByte(); packet[21] = 0x39.toByte()
        // Dest Port: 443 (0x01BB)
        packet[22] = 0x01.toByte(); packet[23] = 0xBB.toByte()

        val result = interceptor.intercept(packet)
        assertEquals("192.168.1.1", result.srcIp)
        assertEquals("8.8.8.8", result.dstIp)
        assertEquals(12345, result.srcPort)
        assertEquals(443, result.dstPort)
        assertEquals(6, result.protocol)
    }

    @Test
    fun testEmptyPacket() {
        val result = interceptor.intercept(ByteArray(0))
        assertEquals(-1, result.uid)
    }
}
