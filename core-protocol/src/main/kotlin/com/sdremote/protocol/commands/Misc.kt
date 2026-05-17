package com.sdremote.protocol.commands

import com.sdremote.protocol.Codec
import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame

/** Get version of the recorder firmware (cmd 1). Returns u16 LE (major, minor). */
data object GetVersion : CLinkRequest {
    override val id = CommandId.GetVersion
}

/** Get one drive's status block (cmd 88). */
data class GetDriveStatus(val mediaIndex: Int) : CLinkRequest {
    override val id = CommandId.GetDriveStatus
    override fun serializePayload(): ByteArray = byteArrayOf(mediaIndex.toByte())
}

/** Show a message on the device's screen (cmd 102). */
data class ShowMessage(
    val text: String,
    val options: ShowMessageOptions = ShowMessageOptions.Standard,
) : CLinkRequest {
    override val id = CommandId.ShowMessage
    override fun serializePayload(): ByteArray = Codec.Writer(text.length + 2)
        .u8(options.code)
        .cstr(text)
        .toByteArray()
}

/** Options bitmap for ShowMessage (cmd 102). */
enum class ShowMessageOptions(val code: Int) {
    Standard(0),
    Persistent(1),
    Urgent(2);
}

/** Decoded drive status (cmd 88 response).
 *
 *  Payload layout (recovered + completed from CLinkPacket.GetDriveStatusResponse):
 *    [+0]  status byte
 *    [+1..4]  free bytes (u32 LE)
 *    [+5..8]  used bytes (u32 LE)
 *    [+9..12] total bytes (u32 LE)
 */
data class DriveStatusResponse(
    val mediaIndex: Int,
    val status: Int,
    val freeBytes: Long,
    val usedBytes: Long,
    val totalBytes: Long,
    override val sourceFrame: Frame,
) : CLinkResponse {
    companion object {
        /** Caller passes the original [mediaIndex] (it's not echoed in the frame). */
        fun parse(frame: Frame, mediaIndex: Int): DriveStatusResponse? {
            if (frame.command != CommandId.GetDriveStatus.byte) return null
            val p = frame.payload
            if (p.size < 13) return null
            return DriveStatusResponse(
                mediaIndex = mediaIndex,
                status = Codec.readU8(p, 0),
                freeBytes = Codec.readU32(p, 1),
                usedBytes = Codec.readU32(p, 5),
                totalBytes = Codec.readU32(p, 9),
                sourceFrame = frame,
            )
        }
    }
}
