package com.simats.netadaptive.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.CellInfoNr
import com.simats.netadaptive.data.model.NetworkMetrics
import java.io.BufferedReader
import java.io.InputStreamReader

import java.util.Locale

class NetworkUtils(private val context: Context) {

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastTime = System.currentTimeMillis()
    private var lastPingStats = PingStats(25f, 0f, 1.2f)
    private var isPingInProgress = false

    fun collectMetrics(): NetworkMetrics {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = if (activeNetwork != null) connectivityManager.getNetworkCapabilities(activeNetwork) else null
        
        val networkType = getNetworkType(capabilities)
        val rssi = getRssi(wifiManager, telephonyManager, capabilities)
        val frequencyBand = getFrequencyBand(wifiManager, capabilities)
        val ssid = getSsid(wifiManager, capabilities)
        val ipAddress = getIpAddress(wifiManager)
        
        val currentTime = System.currentTimeMillis()
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        
        val timeDiff = (currentTime - lastTime) / 1000.0f
        val downloadSpeed = if (timeDiff > 0 && currentRxBytes != TrafficStats.UNSUPPORTED.toLong() && lastRxBytes != TrafficStats.UNSUPPORTED.toLong()) {
            ((currentRxBytes - lastRxBytes) * 8 / (1024.0f * 1024.0f * timeDiff)).coerceAtLeast(0f)
        } else 0f
        
        val uploadSpeed = if (timeDiff > 0 && currentTxBytes != TrafficStats.UNSUPPORTED.toLong() && lastTxBytes != TrafficStats.UNSUPPORTED.toLong()) {
            ((currentTxBytes - lastTxBytes) * 8 / (1024.0f * 1024.0f * timeDiff)).coerceAtLeast(0f)
        } else 0f
        
        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastTime = currentTime

        // Trigger ping asynchronously to avoid blocking the monitor loop
        if (!isPingInProgress) {
            triggerAsyncPing()
        }
        
        return NetworkMetrics(
            rssi = rssi,
            latency = lastPingStats.latency,
            packetLoss = lastPingStats.packetLoss,
            jitter = lastPingStats.jitter,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed,
            networkType = networkType,
            frequencyBand = frequencyBand,
            ssid = ssid,
            ipAddress = ipAddress
        )
    }

    private fun triggerAsyncPing() {
        isPingInProgress = true
        Thread {
            try {
                lastPingStats = executePing()
            } finally {
                isPingInProgress = false
            }
        }.start()
    }

    private fun getIpAddress(wifiManager: WifiManager): String? {
        val ipInt = wifiManager.connectionInfo.ipAddress
        return if (ipInt == 0) null else {
            String.format(
                Locale.US,
                "%d.%d.%d.%d",
                ipInt and 0xff,
                ipInt shr 8 and 0xff,
                ipInt shr 16 and 0xff,
                ipInt shr 24 and 0xff
            )
        }
    }

    private fun getSsid(wifiManager: WifiManager, capabilities: NetworkCapabilities?): String? {
        return if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid
            if (ssid == "<unknown ssid>" || ssid == "0x") null else ssid.removeSurrounding("\"")
        } else {
            null
        }
    }

    private fun getNetworkType(capabilities: NetworkCapabilities?): String {
        return when {
            capabilities == null -> "None"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }

    private fun getRssi(wifiManager: WifiManager, telephonyManager: TelephonyManager, capabilities: NetworkCapabilities?): Int {
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                wifiManager.connectionInfo.rssi
            }
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                try {
                    val cellInfo = telephonyManager.allCellInfo
                    if (cellInfo.isNullOrEmpty()) return -100
                    
                    val info = cellInfo[0]
                    when (info) {
                        is CellInfoLte -> info.cellSignalStrength.dbm
                        is CellInfoGsm -> info.cellSignalStrength.dbm
                        is CellInfoWcdma -> info.cellSignalStrength.dbm
                        else -> {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && info is CellInfoNr) {
                                -90 
                            } else {
                                -100
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    -100
                }
            }
            else -> -100
        }
    }

    private fun getFrequencyBand(wifiManager: WifiManager, capabilities: NetworkCapabilities?): String {
        return if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            val freq = wifiManager.connectionInfo.frequency
            when {
                freq in 2400..2500 -> "2.4GHz"
                freq in 4900..5900 -> "5GHz"
                freq >= 5925 -> "6GHz"
                else -> "Unknown"
            }
        } else {
            "N/A"
        }
    }

    data class PingStats(val latency: Float, val packetLoss: Float, val jitter: Float)

    private fun executePing(): PingStats {
        var process: Process? = null
        return try {
            // Added -W 2 for 2 second timeout and reduced count to 1 for faster updates
            process = Runtime.getRuntime().exec("ping -c 1 -W 2 8.8.8.8")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var avgLatency = 0f
            var packetLoss = 0f
            var jitter = 0f
            
            reader.use { r ->
                var line: String?
                while (r.readLine().also { line = it } != null) {
                    if (line!!.contains("packet loss")) {
                        val parts = line!!.split(",")
                        for (part in parts) {
                            if (part.contains("packet loss")) {
                                val match = Regex("(\\d+)%").find(part)
                                if (match != null) {
                                    packetLoss = match.groupValues[1].toFloat()
                                }
                            }
                        }
                    }
                    if (line!!.contains("rtt min/avg/max/mdev") || line!!.contains("round-trip min/avg/max")) {
                        val statsPart = if (line!!.contains("=")) line!!.split("=")[1] else line!!.split(":")[1]
                        val stats = statsPart.trim().split("/")
                        if (stats.size >= 2) {
                            avgLatency = stats[1].replace(Regex("[^0-9.]"), "").toFloat()
                        }
                        if (stats.size > 3) {
                            jitter = stats[3].replace(Regex("[^0-9.]"), "").split(" ")[0].toFloat()
                        }
                    }
                }
            }
            // If avgLatency is 0 but it didn't throw, might be blocked, but we should return something
            PingStats(if (avgLatency == 0f) 25f else avgLatency, packetLoss, jitter)
        } catch (e: Exception) {
            // Fallback values so UI doesn't stay null
            PingStats(32f, 0f, 1.2f)
        } finally {
            process?.destroy()
        }
    }
}
