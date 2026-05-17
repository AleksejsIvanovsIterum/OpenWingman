package com.sdremote.domain

import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame
import com.sdremote.protocol.auth.Authentication
import com.sdremote.protocol.commands.AuthChallengeParser
import com.sdremote.protocol.commands.AuthenticateInit
import com.sdremote.protocol.commands.AuthenticateResponse
import com.sdremote.protocol.commands.CLinkRequest
import com.sdremote.protocol.commands.ChannelMeter
import com.sdremote.protocol.commands.ExtendedParameterChangeResponse
import com.sdremote.protocol.commands.GetExtendedParameterChangeStatus
import com.sdremote.protocol.commands.MeterType
import com.sdremote.transport.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** High-level session state machine. */
sealed interface SessionState {
    data object Disconnected : SessionState
    data object Connecting : SessionState
    data object Authenticating : SessionState
    data class PasswordRequired(val serial: String?) : SessionState
    data object Authenticated : SessionState
    data class Failed(val cause: Throwable, val previous: SessionState) : SessionState
}

/**
 * Owns a [Transport] and runs the CLink protocol on top.
 *
 * - Re-frames the byte stream into [Frame]s.
 * - Runs the SHA-1 challenge-response handshake on attach.
 * - After auth, starts a meter-poll loop (cmd 97 at the configured cadence).
 * - Exposes [snapshots] as the single source of truth for UI features.
 */
class Session(
    private val transport: Transport,
    private val scope: CoroutineScope,
    private val meterPollPeriodMs: Long = 100L,  // 10 Hz idle; 30 Hz when recording
    /** True for MixPre family — they include an extra "Aux" meter group. */
    private val hasAuxMeterGroup: Boolean = true,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Connecting)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _frames = MutableSharedFlow<Frame>(extraBufferCapacity = 64)
    val frames: SharedFlow<Frame> = _frames.asSharedFlow()

    private val _snapshots = MutableStateFlow(DeviceSnapshot())
    val snapshots: StateFlow<DeviceSnapshot> = _snapshots.asStateFlow()

    private val writeMutex = Mutex()
    private var pumpJob: Job? = null
    private var pollJob: Job? = null

    fun start() {
        if (pumpJob != null) return
        pumpJob = scope.launch { runPump() }
    }

    suspend fun stop() {
        pollJob?.cancel(); pollJob = null
        pumpJob?.cancel(); pumpJob = null
        runCatching { transport.close() }
        _state.value = SessionState.Disconnected
    }

    suspend fun send(request: CLinkRequest) = writeMutex.withLock {
        transport.send(request.toBytes())
    }

    // ── Pump ──

    private suspend fun runPump() {
        val accumulator = ArrayDeque<Byte>()
        _state.value = SessionState.Authenticating
        runCatching { send(AuthenticateInit) }.onFailure { fail(it); return }

        transport.incoming.collect { chunk ->
            for (b in chunk) accumulator.addLast(b)
            drainFrames(accumulator)
        }
    }

    private suspend fun drainFrames(buf: ArrayDeque<Byte>) {
        while (buf.isNotEmpty()) {
            val snap = ByteArray(buf.size).also { var i = 0; for (b in buf) it[i++] = b }
            when (val r = Frame.parse(snap)) {
                is Frame.ParseResult.Ok -> {
                    repeat(r.consumedBytes) { buf.removeFirst() }
                    onFrame(r.frame)
                }
                Frame.ParseResult.NotEnoughBytes -> return
                Frame.ParseResult.BadHeader,
                Frame.ParseResult.BadChecksum -> buf.removeFirst()
            }
        }
    }

    private suspend fun onFrame(frame: Frame) {
        when (_state.value) {
            SessionState.Authenticating -> handleAuth(frame)
            else -> {
                _frames.emit(frame)
                handleData(frame)
            }
        }
    }

    private suspend fun handleAuth(frame: Frame) {
        AuthChallengeParser.parse(frame)?.let { ch ->
            val response = Authentication.computeResponse(ch.bytes)
            runCatching { send(AuthenticateResponse(response)) }.onFailure { fail(it) }
            return
        }
        val payload = frame.payload
        if (frame.command == CommandId.Authenticate.byte &&
            payload.isNotEmpty() && payload[0] == 0x00.toByte()
        ) {
            _state.value = SessionState.Authenticated
            startMeterPolling()
            _frames.emit(frame)
            return
        }
        _frames.emit(frame)
    }

    private fun handleData(frame: Frame) {
        // The meter pump arrives as the only frequent DATA frame; decode it here
        // for the snapshot. Other commands are surfaced via `frames` for callers
        // that subscribe explicitly.
        if (frame.command == CommandId.GetExtendedParameterChangeStatus.byte) {
            ExtendedParameterChangeResponse
                .parse(frame, hasAuxGroup = hasAuxMeterGroup)
                ?.let { update -> applyMeterUpdate(update) }
        }
    }

    private fun applyMeterUpdate(r: ExtendedParameterChangeResponse) {
        _snapshots.update { prev ->
            // Synthesise an L/R mix from the first two output channels if present.
            val mixL = r.outputs.channels.getOrNull(0) ?: ChannelMeter(0, 0, 0)
            val mixR = r.outputs.channels.getOrNull(1) ?: ChannelMeter(0, 0, 0)
            prev.copy(
                inputs = r.inputs,
                outputs = r.outputs,
                tracks = r.tracks,
                mixLeft = mixL,
                mixRight = mixR,
                takeHandle = r.change.takeHandle,
                change = r.change,
            )
        }
    }

    private fun startMeterPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (true) {
                runCatching { send(GetExtendedParameterChangeStatus(MeterType.WithPeaks)) }
                delay(meterPollPeriodMs)
            }
        }
    }

    private fun fail(cause: Throwable) {
        _state.value = SessionState.Failed(cause, _state.value)
    }
}
