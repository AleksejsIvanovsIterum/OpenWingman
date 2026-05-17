package com.sdremote.protocol

/**
 * CLink protocol packet — the wire format used between Wingman and Sound
 * Devices recorders (MixPre, 833, Scorpio) over a BLE GATT channel.
 *
 * Frame layout (little-endian for multi-byte values):
 * ```
 *   [0]      Header        = 0xA5
 *   [1]      Unit ID       0x00..0xEF general; 0xF0 ACK; 0xF1 DATA; 0xFE broadcast
 *   [2]      Data length   bytes 3..N inclusive (command + payload, NOT checksum)
 *   [3]      Command       see [CommandId]
 *   [4..N]   Payload       command-specific
 *   [N+1]    Checksum 1    Fletcher-style, see [Checksum]
 *   [N+2]    Checksum 2
 *
 *   Total bytes = 3 + dataLength + 2
 * ```
 */
object FrameConstants {
    const val HEADER: Byte = 0xA5.toByte()

    /** Index into the packet byte array. */
    const val IX_HEADER = 0
    const val IX_UNIT_ID = 1
    const val IX_LENGTH = 2
    /** On send this is the [CommandId]; on receive it's an [ACK]/[DATA] marker or legacy data. */
    const val IX_CMD_OR_MARKER = 3
    const val IX_PAYLOAD_START = 4

    /** Markers used by the device at [IX_CMD_OR_MARKER] in responses. */
    const val ACK: Byte = 0xF0.toByte()
    const val DATA: Byte = 0xF1.toByte()
    const val UNIT_BROADCAST: Byte = 0xFE.toByte()

    /** Bytes added on top of [IX_LENGTH] value to get total frame size. */
    const val PREAMBLE_BYTES = 3   // header, unit id, length
    const val CHECKSUM_BYTES = 2

    /** Smallest valid frame: header + unit + length + command + 2× checksum. */
    const val MIN_FRAME_SIZE = PREAMBLE_BYTES + 1 + CHECKSUM_BYTES
}

/**
 * Immutable wire representation of a CLink frame.
 *
 * Build outbound frames via [Frame.build] (computes length + checksum for you).
 * Parse inbound bytes via [Frame.parse].
 */
data class Frame(
    val unitId: Byte,
    /**
     * Byte at offset 3.
     *  - On a frame this client constructed via [build], this is the [CommandId] code.
     *  - On a frame received from the device, this is [FrameConstants.ACK] / [FrameConstants.DATA],
     *    or — for legacy commands — the first byte of the response data itself.
     */
    val command: Byte,
    val payload: ByteArray,
    val checksum1: Byte,
    val checksum2: Byte,
) {
    /** True if this is a device→client ACK frame (success or error code in payload[0]). */
    val isAck: Boolean get() = command == FrameConstants.ACK

    /** True if this is a device→client DATA response (payload holds command-specific data). */
    val isData: Boolean get() = command == FrameConstants.DATA

    /** Whether the parsed checksums actually match the recomputed ones. */
    val checksumsValid: Boolean
        get() {
            val (c1, c2) = Checksum.compute(unitId, command, payload)
            return c1 == checksum1 && c2 == checksum2
        }

    /** Serialize back to bytes. */
    fun toByteArray(): ByteArray {
        val len = payload.size + 1  // payload + command byte
        require(len <= 0xFF) { "Payload too large: $len bytes (max 254 incl. command)" }
        val out = ByteArray(FrameConstants.PREAMBLE_BYTES + len + FrameConstants.CHECKSUM_BYTES)
        out[FrameConstants.IX_HEADER] = FrameConstants.HEADER
        out[FrameConstants.IX_UNIT_ID] = unitId
        out[FrameConstants.IX_LENGTH] = len.toByte()
        out[FrameConstants.IX_CMD_OR_MARKER] = command
        payload.copyInto(out, FrameConstants.IX_PAYLOAD_START)
        out[out.size - 2] = checksum1
        out[out.size - 1] = checksum2
        return out
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Frame) return false
        return unitId == other.unitId &&
            command == other.command &&
            payload.contentEquals(other.payload) &&
            checksum1 == other.checksum1 &&
            checksum2 == other.checksum2
    }

    override fun hashCode(): Int {
        var result = unitId.toInt()
        result = 31 * result + command.toInt()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + checksum1.toInt()
        result = 31 * result + checksum2.toInt()
        return result
    }

    companion object {
        /** Build a fresh frame from a command + payload, computing checksums. */
        fun build(
            command: Byte,
            payload: ByteArray = ByteArray(0),
            unitId: Byte = 0x00,
        ): Frame {
            val (c1, c2) = Checksum.compute(unitId, command, payload)
            return Frame(unitId, command, payload, c1, c2)
        }

        /**
         * Parse a complete on-wire frame. Returns null when the buffer is too
         * short, the header byte is wrong, or the declared length doesn't fit
         * the supplied bytes.
         *
         * [validateChecksum] is exposed because the original Wingman implementation
         * compiles-then-discards the checksum check — we expose the choice but
         * default to validating, which is the correct behaviour.
         */
        fun parse(buf: ByteArray, validateChecksum: Boolean = true): ParseResult {
            if (buf.size < FrameConstants.MIN_FRAME_SIZE)
                return ParseResult.NotEnoughBytes
            if (buf[FrameConstants.IX_HEADER] != FrameConstants.HEADER)
                return ParseResult.BadHeader

            val dataLen = buf[FrameConstants.IX_LENGTH].toInt() and 0xFF
            // dataLen counts the command byte + payload, so must be >= 1.
            if (dataLen < 1) return ParseResult.BadHeader
            val expectedTotal = FrameConstants.PREAMBLE_BYTES + dataLen + FrameConstants.CHECKSUM_BYTES
            if (buf.size < expectedTotal) return ParseResult.NotEnoughBytes

            val unit = buf[FrameConstants.IX_UNIT_ID]
            val cmd = buf[FrameConstants.IX_CMD_OR_MARKER]
            val payload = buf.copyOfRange(
                FrameConstants.IX_PAYLOAD_START,
                FrameConstants.IX_PAYLOAD_START + dataLen - 1,
            )
            val c1 = buf[expectedTotal - 2]
            val c2 = buf[expectedTotal - 1]
            val frame = Frame(unit, cmd, payload, c1, c2)

            if (validateChecksum && !frame.checksumsValid) {
                return ParseResult.BadChecksum
            }
            return ParseResult.Ok(frame, consumedBytes = expectedTotal)
        }
    }

    /** Result of parsing a buffer. */
    sealed interface ParseResult {
        /** A complete valid frame was found; [consumedBytes] tells the caller how many bytes to advance. */
        data class Ok(val frame: Frame, val consumedBytes: Int) : ParseResult
        /** Buffer doesn't yet contain a complete frame — wait for more bytes. */
        data object NotEnoughBytes : ParseResult
        /** First byte is not 0xA5 — caller should resync to the next 0xA5. */
        data object BadHeader : ParseResult
        /** Checksum mismatch — frame is corrupt. */
        data object BadChecksum : ParseResult
    }
}
