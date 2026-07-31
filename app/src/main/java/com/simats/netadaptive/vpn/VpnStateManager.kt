package com.simats.netadaptive.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class VpnState {
    object Idle        : VpnState()
    object Connecting  : VpnState()
    object Running     : VpnState()
    object Stopping    : VpnState()
    data class Error(val message: String) : VpnState()
}

@Singleton
class VpnStateManager @Inject constructor() {
    private val _state = MutableStateFlow<VpnState>(VpnState.Idle)
    val state: StateFlow<VpnState> = _state.asStateFlow()

    fun transition(newState: VpnState) { _state.value = newState }
    fun isRunning(): Boolean = _state.value is VpnState.Running
}
