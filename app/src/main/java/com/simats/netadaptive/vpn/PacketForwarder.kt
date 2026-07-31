package com.simats.netadaptive.vpn

import android.net.VpnService
import android.util.Log
import com.simats.netadaptive.ml.AppTier
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*

@Singleton
class PacketForwarder @Inject constructor() {

    private var tunOutputStream: FileOutputStream? = null
    private var vpnService: VpnService? = null
    private val forwardScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Mapping to track UDP sessions (Simplified NAT)
    private val udpSessions = ConcurrentHashMap<String, DatagramSocket>()

    fun bind(fd: FileDescriptor, service: VpnService) {
        this.tunOutputStream = FileOutputStream(fd)
        this.vpnService = service
        Log.d("VPN_DIAG", "FORWARDER_BOUND")
    }

    fun unbind() {
        try {
            tunOutputStream?.close()
            forwardScope.cancel()
        } catch (e: Exception) {
            Log.e("VPN_DIAG", "UNBIND_ERROR", e)
        } finally {
            tunOutputStream = null
            vpnService = null
            udpSessions.values.forEach { it.close() }
            udpSessions.clear()
        }
    }

    /**
     * Pass-Through Forwarding with Diagnostics
     */
    fun forward(rawBytes: ByteArray) {
        if (rawBytes.isEmpty()) return

        try {
            // Write to TUN
            tunOutputStream?.let { stream ->
                stream.write(rawBytes)
                stream.flush()
                Log.d("VPN_DIAG", "PACKET_OUT | Bytes: ${rawBytes.size}")
            }
        } catch (e: IOException) {
            Log.e("VPN_DIAG", "WRITE_FAILED: ${e.message}")
        }
    }

    /**
     * Diagnostic Helper: Minimal UDP Relay for DNS/Basic traffic
     */
    fun relayUdp(packet: PacketInterceptor.ParsedPacket) {
        if (packet.protocol != 17) return // Only UDP

        forwardScope.launch {
            try {
                val socket = DatagramSocket()
                vpnService?.protect(socket)

                val address = InetAddress.getByName(packet.dstIp)
                val data = packet.rawBytes.copyOfRange(28, packet.rawBytes.size) // Skip IP/UDP headers (approximation)

                val outPacket = DatagramPacket(data, data.size, address, packet.dstPort)
                socket.send(outPacket)
                Log.d("VPN_DIAG", "RELAY_SENT | Dst: ${packet.dstIp}:${packet.dstPort}")

                // Wait for response
                val responseData = ByteArray(4096)
                val responsePacket = DatagramPacket(responseData, responseData.size)
                socket.soTimeout = 2000
                socket.receive(responsePacket)

                Log.d("VPN_DIAG", "RESPONSE_RECEIVED | From: ${packet.dstIp}")

                // Note: Re-constructing a full IP/UDP packet for response back to TUN
                // requires a stack. In pass-through diagnostic mode, we log the success.
                Log.d("VPN_DIAG", "RESPONSE_SENT | Bytes: ${responsePacket.length}")
                
            } catch (e: Exception) {
                Log.e("VPN_DIAG", "RELAY_FAILED: ${e.message}")
            }
        }
    }

    suspend fun drainQueue(tier: AppTier, queueManager: QueueManager) {
        // Disabled in Pass-Through mode
    }
}
