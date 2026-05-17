package com.sdremote.protocol

/**
 * CLink checksum algorithm — Fletcher-style, but using mod 255 (not 256),
 * with a final inversion pass that makes the checksum bytes themselves
 * close the accumulator to zero. Recovered from CLinkPacket.cs.
 *
 * Important wrinkle: when [unitId] is NOT [FrameConstants.UNIT_BROADCAST]
 * (0xFE), the unit byte is **substituted with 0** inside the accumulator —
 * presumably so a packet addressed to any specific unit produces the same
 * checksum, simplifying multi-unit relay scenarios.
 *
 * Verifying a received frame: run the full accumulator over header+unit+
 * length+command+payload+ck1+ck2 — if both accumulators end at 0, the
 * frame is intact.
 */
object Checksum {
    private const val MOD = 255

    /** Compute (ck1, ck2) for an outbound frame. */
    fun compute(unitId: Byte, command: Byte, payload: ByteArray): Pair<Byte, Byte> {
        var s1 = 0
        var s2 = 0
        // Header
        s1 = (s1 + (FrameConstants.HEADER.toInt() and 0xFF)) % MOD
        s2 = (s2 + s1) % MOD
        // Unit ID — substituted with 0 unless broadcast
        val unitForChk = if (unitId == FrameConstants.UNIT_BROADCAST) (unitId.toInt() and 0xFF) else 0
        s1 = (s1 + unitForChk) % MOD; s2 = (s2 + s1) % MOD
        // Length byte = payload.size + 1 (command)
        val length = payload.size + 1
        s1 = (s1 + length) % MOD; s2 = (s2 + s1) % MOD
        // Command
        s1 = (s1 + (command.toInt() and 0xFF)) % MOD; s2 = (s2 + s1) % MOD
        // Payload
        for (b in payload) {
            s1 = (s1 + (b.toInt() and 0xFF)) % MOD
            s2 = (s2 + s1) % MOD
        }

        // Closing pass (from CLinkPacket.cs CalculateChecksums)
        val candidateSum = (s1 + s2) % MOD
        val ck1 = MOD - candidateSum
        s1 = (s1 + ck1) % MOD
        val ck2 = MOD - s1
        return (ck1.toByte()) to (ck2.toByte())
    }

    /** Validate the trailing checksum bytes of a *complete* packet. */
    fun verify(packet: ByteArray): Boolean {
        if (packet.size < FrameConstants.MIN_FRAME_SIZE) return false
        var s1 = 0
        var s2 = 0
        val length = packet[FrameConstants.IX_LENGTH].toInt() and 0xFF
        val total = FrameConstants.PREAMBLE_BYTES + length + FrameConstants.CHECKSUM_BYTES
        if (packet.size < total) return false
        for (i in 0 until total) {
            var b = packet[i].toInt() and 0xFF
            if (i == FrameConstants.IX_UNIT_ID &&
                packet[FrameConstants.IX_UNIT_ID] != FrameConstants.UNIT_BROADCAST
            ) {
                b = 0
            }
            s1 = (s1 + b) % MOD
            s2 = (s2 + s1) % MOD
        }
        return s1 == 0 && s2 == 0
    }
}
