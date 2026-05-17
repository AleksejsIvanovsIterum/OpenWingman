package com.sdremote.protocol.commands

import com.sdremote.protocol.Codec
import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame

/** Meter type selector for cmd 97 (matches the original Wingman enum). */
enum class MeterType(val code: Int) {
    Standard(0),
    WithPeaks(1),
    Extended(2);
}

/** Request the meter+parameter snapshot (cmd 97). */
data class GetExtendedParameterChangeStatus(val meterType: MeterType = MeterType.WithPeaks) : CLinkRequest {
    override val id = CommandId.GetExtendedParameterChangeStatus
    override fun serializePayload(): ByteArray = byteArrayOf(meterType.code.toByte())
}

/** One channel's metering data, all in 0..255 device units. */
data class ChannelMeter(val vu: Int, val peak: Int, val extra: Int) {
    /** Convert the device's 0..255 VU byte to a dBFS reading.
     *  Empirically: 0 → -inf (treat as -60), 255 → +20. */
    fun vuDb(min: Float = -60f, max: Float = 20f): Float =
        if (vu == 0) min else min + (max - min) * (vu / 255f)

    fun peakDb(min: Float = -60f, max: Float = 20f): Float =
        if (peak == 0) min else min + (max - min) * (peak / 255f)
}

/** Snapshot of a group of meters (inputs / outputs / returns / tracks / aux). */
data class MeterGroup(val channels: List<ChannelMeter>)

/** Parameter change flags returned at the tail of every cmd 97 response. */
data class ChangeStatus(
    val takeFlags: Int,
    val takeHandle: Long,
    val takeInfo1: Int,
    val takeInfo2: Int,
    val pFlags1: Int,
    val pFlags2: Int,
    val pFlags3: Int,
)

/** Full decoded cmd 97 response. */
data class ExtendedParameterChangeResponse(
    val inputs: MeterGroup,
    val outputs: MeterGroup,
    val returns: MeterGroup,
    val tracks: MeterGroup,
    val aux: MeterGroup?,        // present on MixPre family
    val change: ChangeStatus,
    override val sourceFrame: Frame? = null,
) : CLinkResponse {
    companion object {
        /**
         * Parse the meter response directly from a DATA frame's payload bytes
         * (i.e. positions [4..N-1] of the wire frame, which is what
         * [Frame.payload] returns).
         *
         * Returns null on truncation. The caller is responsible for routing —
         * only invoke when [Session]'s pending command is cmd 97.
         *
         * @param hasAuxGroup whether the device is a MixPre (extra group).
         */
        fun parsePayload(p: ByteArray, hasAuxGroup: Boolean): ExtendedParameterChangeResponse? {
            var i = 0
            fun readGroup(): MeterGroup? {
                if (i >= p.size) return null
                val n = Codec.readU8(p, i); i++
                if (i + n * 3 > p.size) return null
                val list = ArrayList<ChannelMeter>(n)
                repeat(n) {
                    list.add(
                        ChannelMeter(
                            vu = Codec.readU8(p, i),
                            peak = Codec.readU8(p, i + 1),
                            extra = Codec.readU8(p, i + 2),
                        )
                    )
                    i += 3
                }
                return MeterGroup(list)
            }

            val inputs = readGroup() ?: return null
            val outputs = readGroup() ?: return null
            val returns = readGroup() ?: return null
            val tracks = readGroup() ?: return null
            val aux = if (hasAuxGroup) readGroup() else null

            if (i + 10 > p.size) return null
            val change = ChangeStatus(
                takeFlags = Codec.readU8(p, i),
                takeHandle = Codec.readU32(p, i + 1),
                takeInfo1 = Codec.readU8(p, i + 5),
                takeInfo2 = Codec.readU8(p, i + 6),
                pFlags1 = Codec.readU8(p, i + 7),
                pFlags2 = Codec.readU8(p, i + 8),
                pFlags3 = Codec.readU8(p, i + 9),
            )

            return ExtendedParameterChangeResponse(
                inputs, outputs, returns, tracks, aux, change,
                sourceFrame = null,
            )
        }
    }
}
