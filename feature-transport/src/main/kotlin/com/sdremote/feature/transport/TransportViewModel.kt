package com.sdremote.feature.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdremote.domain.DeviceSnapshot
import com.sdremote.domain.Session
import com.sdremote.protocol.commands.CLinkRequest
import com.sdremote.protocol.commands.TransportControl
import com.sdremote.protocol.types.TransportState
import com.sdremote.ui.meters.ChannelState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Drives the transport screen.
 *
 * Source-aware:
 *   - With a live [Session]: snapshots from BLE meter polling drive the UI.
 *   - Without (preview / disconnected): falls back to [MockTransportSource].
 *
 * Actions dispatch to [Session] when present; logged at debug otherwise.
 */
class TransportViewModel(
    private val session: Session? = null,
    private val mockSource: MockTransportSource = MockTransportSource(),
) : ViewModel() {

    val ui: StateFlow<TransportUi> = sourceFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransportUi())

    private fun sourceFlow(): Flow<TransportUi> = when (session) {
        null -> mockSource.snapshots()
        else -> session.snapshots.map { it.toUi() }
    }

    fun onRec()       = dispatch(TransportControl(TransportState.Record))
    fun onStop()      = dispatch(TransportControl(TransportState.Stop))
    fun onPlay()      = dispatch(TransportControl(TransportState.Play))
    fun onCircle()    { /* TODO cmd 58 SetTakeParameter(CircleTake, true) — needs current take handle */ }
    fun onFalseTake() { /* TODO cmd 59 FalseTake(currentHandle) — needs current take handle */ }

    private fun dispatch(req: CLinkRequest) {
        val s = session ?: run { Timber.d("No session; ignoring ${req.id}"); return }
        viewModelScope.launch {
            runCatching { s.send(req) }.onFailure { Timber.w(it, "Failed to send ${req.id}") }
        }
    }
}

/** Translate a [DeviceSnapshot] from the protocol layer into the UI's [TransportUi]. */
private fun DeviceSnapshot.toUi(): TransportUi {
    val channels = inputs.channels.mapIndexed { idx, ch ->
        ChannelSnapshot(
            id = idx + 1,
            name = "CH ${(idx + 1).toString().padStart(2, '0')}",
            level = ch.vuDb(),
            peak = ch.peakDb(),
            state = if (ch.vu == 0 && ch.peak == 0) ChannelState.Off else ChannelState.Record,
        )
    }
    return TransportUi(
        scene = scene.ifEmpty { "—" },
        take = take,
        recording = transportRecording,
        timecode = timecode,
        frameRate = "—",
        timeRemaining = "—",
        channels = channels,
        mix = MixBus(
            leftLevel = mixLeft.vuDb(), leftPeak = mixLeft.peakDb(),
            rightLevel = mixRight.vuDb(), rightPeak = mixRight.peakDb(),
        ),
    )
}
