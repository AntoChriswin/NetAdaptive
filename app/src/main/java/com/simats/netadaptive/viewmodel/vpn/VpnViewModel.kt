package com.simats.netadaptive.viewmodel.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.ViewModel
import com.simats.netadaptive.vpn.NetAdaptiveVpnService
import com.simats.netadaptive.vpn.VpnState
import com.simats.netadaptive.vpn.VpnStateManager
import com.simats.netadaptive.vpn.MetricsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
    private val stateManager: VpnStateManager,
    val metricsManager: MetricsManager
) : ViewModel() {

    val vpnState: StateFlow<VpnState> = stateManager.state

    fun prepareVpn(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    fun startVpn(context: Context) {
        context.startService(NetAdaptiveVpnService.startIntent(context))
    }

    fun stopVpn(context: Context) {
        context.startService(NetAdaptiveVpnService.stopIntent(context))
    }

    fun onPermissionDenied() {
        stateManager.transition(VpnState.Error("VPN permission denied by user"))
    }
}
