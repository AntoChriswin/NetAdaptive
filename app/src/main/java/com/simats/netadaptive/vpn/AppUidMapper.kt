package com.simats.netadaptive.vpn

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.simats.netadaptive.ml.AppTier
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUidMapper @Inject constructor(
    private val packageManager: PackageManager
) {
    private val uidToPackage = ConcurrentHashMap<Int, String>()
    private val packageToTier = ConcurrentHashMap<String, AppTier>()

    fun resolvePackage(uid: Int): String {
        if (uid < 0) return "unknown"
        return uidToPackage.getOrPut(uid) {
            try {
                packageManager.getPackagesForUid(uid)?.firstOrNull() ?: "uid:$uid"
            } catch (e: Exception) {
                "uid:$uid"
            }
        }
    }

    fun updateTierMapping(rules: Map<String, AppTier>) {
        if (rules.isNotEmpty()) {
            packageToTier.clear()
            packageToTier.putAll(rules)
            Log.d("VPN_MAPPER", "Mapping updated. Checking visibility for ${rules.size} apps.")
        }
    }

    fun getTier(packageName: String): AppTier =
        packageToTier[packageName] ?: AppTier.HIGH

    /**
     * Finds the real package ID on this specific device.
     * Handles cases where the ID might be slightly different or hidden.
     */
    private fun findRealPackageId(target: String): String? {
        try {
            packageManager.getPackageInfo(target, 0)
            return target // Found exactly
        } catch (e: Exception) {
            // Fuzzy search: Find anything containing the name (e.g. "youtube")
            Log.w("VPN_MAPPER", "Exact ID $target not found. Searching system...")
            val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val match = allApps.find { it.packageName.contains(target, ignoreCase = true) || target.contains(it.packageName, ignoreCase = true) }
            return match?.packageName
        }
    }

    fun getManagedPackages(): List<String> {
        val managed = packageToTier.filter { it.value == AppTier.LOW || it.value == AppTier.NORMAL }.keys
        val verified = mutableListOf<String>()
        
        managed.forEach { pkg ->
            val realId = findRealPackageId(pkg)
            if (realId != null) {
                verified.add(realId)
                Log.d("VPN_MAPPER", "Verified: $realId is present.")
            } else {
                Log.e("VPN_MAPPER", "Discarding $pkg: Not visible/installed.")
            }
        }
        return verified.distinct()
    }

    fun invalidateCache() {
        uidToPackage.clear()
    }
}
