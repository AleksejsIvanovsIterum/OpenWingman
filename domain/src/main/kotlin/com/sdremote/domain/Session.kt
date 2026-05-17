package com.sdremote.domain

import com.sdremote.protocol.Frame
import com.sdremote.protocol.auth.Authentication
import com.sdremote.protocol.commands.AuthChallengeParser
import com.sdremote.protocol.commands.AuthenticateInit
import com.sdremote.protocol.commands.AuthenticateResponse
import com.sdremote.protocol.commands.CLinkRequest
import com.sdremote.transport.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Session state machine.
 *
 *   Disconnected ──connect──▶ Connecting ──BLE up──▶ Authenticating
 *                                                       │
 *                            ┌──────────────────────────┘
 *                            ▼
 *                       Authenticated ──any error──▶ Disconnected
 *                            │
 *                            ▼
 *                       (optional) PasswordRequired
 */
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
 * Responsibilities:
 *  - Re-frame raw byte stream into [Frame] events.
 *  - Drive the authentication handshake on attach.
 *  - Expose a write API for [CLinkRequest].
 *
 * Scope: caller-supplied. The session attaches its coroutines to it and
 * tears down when the scope is cancelled.
 */
class Session(
    private val transport: Transport,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Connecting)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _frames = MutableSharedFlow<Frame>(extraBufferCapacity = 64)
    val frames: SharedFlow<Frame> = _frames.asSharedFlow()

    private val writeMutex = Mutex()
    private var pumpJob: Job? = null

    /** Begin reading the transport and run the auth handshake. */
    fun start() {
        if (pumpJob != null) return
        pumpJob = scope.launch { runPump() }
    }

    /** Stop pump and close transport. */
    suspend fun stop() {
        pumpJob?.cancel()
        pumpJob = null
        runCatching { transport.close() }
        _state.value = SessionState.Disconnected
    }

    /** Send a typed command. Returns immediately after the write — responses arrive on [frames]. */
    suspend fun send(request: CLinkRequest) = writeMutex.withLock {
        transport.send(request.toBytes())
    }

    // ── Internals ──

    private suspend fun runPump() {
        val accumulator = ArrayDeque<Byte>()
        _state.value = SessionState.Authenticating
        // Kick off the handshake with an initial Authenticate request.
        runCatching { send(AuthenticateInit) }.onFailure { fail(it); return }

        transport.incoming.collect { chunk ->
            chunk.forEach { accumulator.addLast(it) }
            drainFrames(accumulator)
        }
    }

    private suspend fun drainFrames(buf: ArrayDeque<Byte>) {
        while (true) {
            // Snapshot current buffer to a ByteArray for the parser.
            val snapshot = buf.toByteArray()
            when (val r = Frame.parse(snapshot)) {
                is Frame.ParseResult.Ok -> {
                    repeat(r.consumedBytes) { buf.removeFirst() }
                    onFrame(r.frame)
                }
                Frame.ParseResult.NotEnoughBytes -> return  // wait for more
                Frame.ParseResult.BadHeader -> {
                    // Resync to next 0xA5 boundary.
                    buf.removeFirst()
                }
                Frame.ParseResult.BadChecksum -> {
                    // Drop one byte and try again — corrupted frame.
                    buf.removeFirst()
                }
            }
            if (buf.isEmpty()) return
        }
    }

    private suspend fun onFrame(frame: Frame) {
        // Auth-state handshake before any general traffic.
        when (_state.value) {
            SessionState.Authenticating -> handleAuth(frame)
            else -> _frames.emit(frame)
        }
    }

    private suspend fun handleAuth(frame: Frame) {
        // 1. A challenge frame? Respond with SHA-1(challenge || AUTH_KEY).
        AuthChallengeParser.parse(frame)?.let { ch ->
            val response = Authentication.computeResponse(ch.bytes)
            runCatching { send(AuthenticateResponse(response)) }.onFailure { fail(it) }
            return
        }

        // 2. ACK with success? Auth complete.
        val payload = frame.payload
        if (frame.command == com.sdremote.protocol.CommandId.Authenticate.byte &&
            payload.isNotEmpty() && payload[0] == 0x00.toByte()
        ) {
            _state.value = SessionState.Authenticated
            _frames.emit(frame)
            return
        }

        // 3. Anything else mid-auth: surface to listeners (caller will see
        //    OperationFailure if password gate is on).
        _frames.emit(frame)
    }

    private fun fail(cause: Throwable) {
        _state.value = SessionState.Failed(cause, _state.value)
    }
}

private fun ArrayDeque<Byte>.toByteArray(): ByteArray {
    val out = ByteArray(size)
    var i = 0
    for (b in this) out[i++] = b
    return out
}
