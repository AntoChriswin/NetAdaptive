package com.simats.netadaptive.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import androidx.core.app.NotificationCompat
import com.simats.netadaptive.ml.AppTier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class NetAdaptiveVpnService : VpnService() {

    @Inject lateinit var stateManager: VpnStateManager
    @Inject lateinit var interceptor:  PacketInterceptor
    @Inject lateinit var uidMapper:    AppUidMapper
    @Inject lateinit var resolver:     PriorityResolver
    @Inject lateinit var ruleEngine:   RuleEngine
    @Inject lateinit var controller:   TrafficController
    @Inject lateinit var forwarder:    PacketForwarder
    @Inject lateinit var queueManager: QueueManager
    @Inject lateinit var metrics:      MetricsManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var tunFd: ParcelFileDescriptor? = null
    
    private var packetLoopJob: Job? = null
    private var drainLoopJob: Job? = null
    private var metricsJob: Job? = null

    companion object {
        const val START_ACTION = "com.simats.netadaptive.vpn.START"
        const val STOP_ACTION  = "com.simats.netadaptive.vpn.STOP"
        const val UPDATE_ACTION = "com.simats.netadaptive.vpn.UPDATE"
        private const val NOTIFICATION_ID = 1001

        fun startIntent(context: Context) =
            Intent(context, NetAdaptiveVpnService::class.java).setAction(START_ACTION)

        fun stopIntent(context: Context) =
            Intent(context, NetAdaptiveVpnService::class.java).setAction(STOP_ACTION)
            
        fun updateIntent(context: Context) =
            Intent(context, NetAdaptiveVpnService::class.java).setAction(UPDATE_ACTION)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            STOP_ACTION -> stopVpn()
            UPDATE_ACTION -> restartVpn()
            else        -> startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (tunFd != null) return
        
        stateManager.transition(VpnState.Connecting)
        startForeground(NOTIFICATION_ID, buildNotification())
        
        try {
            val builder = Builder()
                .setSession("NetAdaptive_Hybrid")
                .addAddress("10.0.0.2", 24)
                .setMtu(1500)
                .setBlocking(false)

            val managedApps = uidMapper.getManagedPackages()
            var appsSuccessfullyAdded = 0

            if (managedApps.isNotEmpty()) {
                // RULE: You cannot call addAllowedApplication AND addDisallowedApplication in the same builder.
                // To exclude ourselves in an allowlist mode, we simply don't add ourselves to the list.
                managedApps.forEach { pkg ->
                    try { 
                        builder.addAllowedApplication(pkg)
                        appsSuccessfullyAdded++
                        Log.d("VPN_SAFE", "Successfully added $pkg to restricted tunnel.")
                    } catch (e: Exception) {
                        Log.e("VPN_SAFE", "App $pkg is not valid for tunnel inclusion. Skipping.")
                    }
                }
            }

            if (appsSuccessfullyAdded > 0) {
                // Only intercept internet traffic for these specific apps
                builder.addRoute("0.0.0.0", 0)
                Log.d("VPN_SAFE", "Routing active for $appsSuccessfullyAdded apps.")
            } else {
                // Safety: No apps to manage, so we exclude our own app and add NO ROUTES.
                // This ensures total bypass for everything on the phone.
                builder.addDisallowedApplication(packageName)
                Log.w("VPN_SAFE", "No managed apps found. VPN is in full pass-through mode.")
            }

            tunFd = builder.establish() ?: run {
                stateManager.transition(VpnState.Error("VPN establish() failed"))
                return
            }
            
            if (appsSuccessfullyAdded > 0) {
                forwarder.bind(tunFd!!.fileDescriptor, this)
                stateManager.transition(VpnState.Running)
                packetLoopJob = launchPacketLoop()
                drainLoopJob = launchQueueDrainLoop()
                metricsJob = launchMetricsPublishLoop()
            } else {
                stateManager.transition(VpnState.Running)
                Log.d("VPN_SAFE", "VPN running as monitoring standby.")
            }

        } catch (e: Exception) {
            Log.e("VPN_SAFE", "Fatal VPN startup error: ${e.message}", e)
            stateManager.transition(VpnState.Error(e.message ?: "unknown"))
        }
    }

    private fun restartVpn() {
        Log.d("VPN_SAFE", "Restarting to apply new priority rules...")
        packetLoopJob?.cancel()
        drainLoopJob?.cancel()
        metricsJob?.cancel()
        
        forwarder.unbind()
        val oldFd = tunFd
        tunFd = null
        try { oldFd?.close() } catch (e: Exception) {}
        
        startVpn()
    }

    private fun stopVpn() {
        stateManager.transition(VpnState.Stopping)
        packetLoopJob?.cancel()
        drainLoopJob?.cancel()
        metricsJob?.cancel()
        forwarder.unbind()
        tunFd?.close()
        tunFd = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        stateManager.transition(VpnState.Idle)
    }

    private fun launchPacketLoop() = serviceScope.launch {
        val buffer = ByteBuffer.allocate(32_768)
        tunFd?.let { fd ->
            val inputStream = FileInputStream(fd.fileDescriptor)
            try {
                while (isActive) {
                    buffer.clear()
                    val length = try {
                        withContext(Dispatchers.IO) { inputStream.read(buffer.array()) }
                    } catch (e: IOException) { -1 }
                    
                    if (length <= 0) break
                    
                    val rawBytes = buffer.array().copyOf(length)
                    launch {
                        val parsed = interceptor.intercept(rawBytes)
                        val uid    = resolveUid(parsed)
                        val rule   = resolver.resolve(uid)
                        val action = ruleEngine.evaluate(rule)
                        controller.handle(parsed.copy(uid = uid), rule, action)
                    }
                }
            } catch (e: Exception) {
            } finally {
                try { inputStream.close() } catch (e: Exception) {}
            }
        }
    }

    private fun resolveUid(packet: PacketInterceptor.ParsedPacket): Int =
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val cm = getSystemService(ConnectivityManager::class.java)
                cm?.getConnectionOwnerUid(
                    if (packet.protocol == 6) OsConstants.IPPROTO_TCP
                    else if (packet.protocol == 17) OsConstants.IPPROTO_UDP
                    else -1,
                    InetSocketAddress(packet.srcIp, packet.srcPort),
                    InetSocketAddress(packet.dstIp, packet.dstPort)
                ) ?: -1
            } else -1
        } catch (e: Exception) { -1 }

    private fun launchQueueDrainLoop() = serviceScope.launch {
        while (isActive) {
            delay(200)
            queueManager.dropExpired()
            val stress = resolver.resolve(0).stressScore
            if (stress < 0.60f) {
                forwarder.drainQueue(AppTier.NORMAL, queueManager)
            }
            if (stress < 0.20f) {
                forwarder.drainQueue(AppTier.LOW, queueManager)
            }
        }
    }

    private fun launchMetricsPublishLoop() = serviceScope.launch {
        while (isActive) {
            delay(1_000)
            metrics.publish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    private fun buildNotification(): Notification {
        val channelId = "vpn_service"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm?.createNotificationChannel(NotificationChannel(channelId, "VPN", NotificationManager.IMPORTANCE_LOW))
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("NetAdaptive VPN")
            .setContentText("Surgical Traffic Guard Active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}
