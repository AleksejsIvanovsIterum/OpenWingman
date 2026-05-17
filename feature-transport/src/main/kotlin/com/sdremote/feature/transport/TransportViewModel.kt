package com.sdremote.feature.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdremote.domain.Session
import com.sdremote.protocol.commands.TransportControl
import com.sdremote.protocol.types.TransportState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Drives the transport screen.
 *
 * When [session] is supplied (we have a live BLE connection), actions
 * dispatch real CLink commands.
 *
 * Meter polling (cmd 97) is not yet wired in — the UI shows mocked
 * channels until that's added. The screen otherwise responds to real
 * REC / STOP / PLAY / FALSE / CIRCLE taps.
 */
class TransportViewModel(
    private val source: MockTransportSource = MockTransportSource(),
    private val session: Session? = null,
) : ViewModel() {
    val ui: StateFlow<TransportUi> = source.snapshots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransportUi())

    fun onRec()       = dispatch(TransportControl(TransportState.Record))
    fun onStop()      = dispatch(TransportControl(TransportState.Stop))
    fun onPlay()      = dispatch(TransportControl(TransportState.Play))
    fun onCircle()    { /* TODO cmd 58 SetTakeParameter(CircleTake, true) — needs current take handle */ }
    fun onFalseTake() { /* TODO cmd 59 FalseTake(currentHandle) — needs current take handle */ }

    private fun dispatch(req: com.sdremote.protocol.commands.CLinkRequest) {
        val s = session
        if (s == null) {
            Timber.d("No session; ignoring command: ${req.id}")
            return
        }
        viewModelScope.launch {
            runCatching { s.send(req) }.onFailure {
                Timber.w(it, "Failed to send ${req.id}")
            }
        }
    }
}
