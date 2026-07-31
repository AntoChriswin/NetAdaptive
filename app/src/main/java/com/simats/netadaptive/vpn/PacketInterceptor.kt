package com.simats.netadaptive.vpn

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PacketInterceptor @Inject constructor() {

    data class ParsedPacket(
        val rawBytes: ByteArray,
        val uid: Int,         // -1 until resolved
        val srcIp: String,
        val dstIp: String,
        val srcPort: Int,
        val dstPort: Int,
        val protocol: Int     // 6=TCP, 17=UDP
    )

    fun intercept(rawBytes: ByteArray): ParsedPacket {
        if (rawBytes.isEmpty()) return ParsedPacket(rawBytes, -1, "", "", 0, 0, 0)
        val version = (rawBytes[0].toInt() shr 4) and 0xF
        return when (version) {
            4    -> parseIpv4(rawBytes)
            6    -> parseIpv6(rawBytes)
            else -> ParsedPacket(rawBytes, -1, "", "", 0, 0, 0)
        }
    }

    private fun parseIpv4(b: ByteArray): ParsedPacket {
        val ihl = (b[0].toInt() and 0xF) * 4
        val proto = b[9].toInt() and 0xFF
        val src = "${b[12].toUByte()}.${b[13].toUByte()}.${b[14].toUByte()}.${b[15].toUByte()}"
        val dst = "${b[16].toUByte()}.${b[17].toUByte()}.${b[18].toUByte()}.${b[19].toUByte()}"
        val (sp, dp) = extractPorts(b, ihl, proto)
        return ParsedPacket(b, -1, src, dst, sp, dp, proto)
    }

    private fun parseIpv6(b: ByteArray): ParsedPacket {
        val proto = b[6].toInt() and 0xFF
        val src = formatIpv6(b, 8)
        val dst = formatIpv6(b, 24)
        val (sp, dp) = extractPorts(b, 40, proto)
        return ParsedPacket(b, -1, src, dst, sp, dp, proto)
    }

    private fun extractPorts(b: ByteArray, offset: Int, proto: Int): Pair<Int, Int> {
        if (proto != 6 && proto != 17) return 0 to 0
        if (b.size < offset + 4) return 0 to 0
        val sp = ((b[offset].toInt() and 0xFF) shl 8) or (b[offset + 1].toInt() and 0xFF)
        val dp = ((b[offset + 2].toInt() and 0xFF) shl 8) or (b[offset + 3].toInt() and 0xFF)
        return sp to dp
    }

    private fun formatIpv6(b: ByteArray, start: Int): String =
        (0 until 8).joinToString(":") {
            val hi = b[start + it * 2].toInt() and 0xFF
            val lo = b[start + it * 2 + 1].toInt() and 0xFF
            "%02x%02x".format(hi, lo)
        }
}
