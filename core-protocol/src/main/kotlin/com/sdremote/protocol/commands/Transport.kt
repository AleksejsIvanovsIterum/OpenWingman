package com.sdremote.protocol.commands

import com.sdremote.protocol.Codec
import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame
import com.sdremote.protocol.types.TimecodeBlock
import com.sdremote.protocol.types.TransportState

/** Set REC / STOP / PLAY / FF / REW (cmd 17). */
data class TransportControl(val state: TransportState) : CLinkRequest {
    override val id = CommandId.TransportControl
    override fun serializePayload(): ByteArray = byteArrayOf(state.code.toByte())
}

/** Poll current transport state (cmd 18). */
data object QueryTransportStatus : CLinkRequest {
    override val id = CommandId.TransportStatus
}

/** Poll all three timecodes (cmd 19). 12-byte response. */
data object QueryTimecode : CLinkRequest {
    override val id = CommandId.GetTimecode
}

/** Decoded transport-status DATA frame. */
data class TransportStatusResponse(
    val state: TransportState,
    override val sourceFrame: Frame,
) : CLinkResponse {
    companion object {
        fun parse(frame: Frame): TransportStatusResponse? {
            // Routing now handled by Session.pendingCmd, not by command-byte echo.
            if (frame.payload.isEmpty()) return null
            val state = TransportState.fromCodeOrNull(Codec.readU8(frame.payload, 0))
                ?: TransportState.Unknown
            return TransportStatusResponse(state, frame)
        }
    }
}

/** Decoded timecode DATA frame (cmd 19). */
data class TimecodeResponse(
    val tc: TimecodeBlock,
    override val sourceFrame: Frame,
) : CLinkResponse {
    companion object {
        fun parse(frame: Frame): TimecodeResponse? {
            // Routing now handled by Session.pendingCmd, not by command-byte echo.
            if (frame.payload.size < 12) return null
            return TimecodeResponse(TimecodeBlock.fromPayload(frame.payload), frame)
        }
    }
}
