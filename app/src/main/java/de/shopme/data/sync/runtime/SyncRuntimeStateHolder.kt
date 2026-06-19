package de.shopme.data.sync.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncRuntimeStateHolder {

    private val _state =
        MutableStateFlow(SyncRuntimeState.IDLE)

    val state: StateFlow<SyncRuntimeState> =
        _state.asStateFlow()

    fun setState(
        newState: SyncRuntimeState
    ) {
        _state.value = newState
    }

    fun currentState(): SyncRuntimeState {
        return _state.value
    }

    fun isRealtimeActive(): Boolean {
        return _state.value ==
                SyncRuntimeState.REALTIME_ACTIVE
    }

    fun isRecovering(): Boolean {
        return _state.value ==
                SyncRuntimeState.RECOVERING
    }

    fun isReplaying(): Boolean {
        return _state.value ==
                SyncRuntimeState.REPLAYING
    }
}