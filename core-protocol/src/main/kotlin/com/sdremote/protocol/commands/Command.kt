package com.sdremote.protocol.commands

import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame

/**
 * A typed outbound CLink command — carries its [CommandId] and a [serialize]
 * step that produces the payload bytes.
 *
 * Use `request.toFrame()` to get a ready-to-send [Frame].
 *
 * Implementers usually live in `commands/<Group>.kt` next to their parser.
 */
interface CLinkRequest {
    val id: CommandId
    fun serializePayload(): ByteArray = ByteArray(0)

    fun toFrame(): Frame = Frame.build(command = id.byte, payload = serializePayload())
    fun toBytes(): ByteArray = toFrame().toByteArray()
}

/** Sealed marker for typed response objects parsed from a received [Frame]. */
sealed interface CLinkResponse {
    /**
     * Originating wire frame. Nullable because some parsers (e.g. meter polling
     * decoded from raw payload bytes) don't retain the full frame.
     */
    val sourceFrame: Frame?
}

/** Returned when the command's response is not yet typed — caller must inspect [Frame] directly. */
data class RawResponse(override val sourceFrame: Frame) : CLinkResponse
