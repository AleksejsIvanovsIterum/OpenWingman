package com.sdremote.feature.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Drives the transport screen. For now uses [MockTransportSource]; once
 * core-protocol + transport-ble land, this gets a constructor parameter
 * for a real device session.
 */
class TransportViewModel(
    private val source: MockTransportSource = MockTransportSource(),
) : ViewModel() {
    val ui: StateFlow<TransportUi> = source.snapshots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransportUi())

    fun onRec()       { /* TODO: cmd 17 TransportControl(REC) */ }
    fun onStop()      { /* TODO: cmd 17 TransportControl(STOP) */ }
    fun onFalseTake() { /* TODO: cmd 59 FalseTake(currentHandle) */ }
    fun onCircle()    { /* TODO: cmd 58 SetTakeParameter(CircleTake, true) */ }
    fun onPlay()      { /* TODO: cmd 17 TransportControl(PLAY) */ }
}
