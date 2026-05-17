package com.sdremote.protocol.types

/** Transport actions / playback states — payload of cmd 17 / 18. */
enum class TransportState(val code: Int) {
    Stop(0),
    Play(1),
    FastForward(2),
    Rewind(3),
    Record(4),
    Pause(5),
    Idle(6),
    Unknown(0xFF);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): TransportState? = byCode[code]
    }
}

/** Error sub-codes returned alongside `CommandSpecific` for TransportControl on MixPre. */
enum class TransportError(val code: Int) {
    Ok(0),
    NoMedia(1),
    NoTracks(2),
    InvalidSrate(3),
    Busy(4),
    InvalidFile(5),
    MediaFull(6),
    TooManyTracks(7);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): TransportError? = byCode[code]
    }
}

/** Recorder mode — payload of cmd 104. */
enum class DeviceMode(val code: Int) {
    Basic(0), Advanced(1), Custom(2), FileTransfer(3), Music(4), Unknown(0xFFFF);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): DeviceMode? = byCode[code]
    }
}

/** Sample rate enum — payload value for `CLSetting.SampleRate`. */
enum class SampleRate(val code: Int, val hz: Int) {
    Sr44100(0, 44_100),
    Sr47952(1, 47_952),
    Sr47952F(2, 47_952),
    Sr48000(3, 48_000),
    Sr48048(4, 48_048),
    Sr48048F(5, 48_048),
    Sr88200(6, 88_200),
    Sr96000(7, 96_000),
    Sr192000(8, 192_000);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): SampleRate? = byCode[code]
    }
}

/** HH:MM:SS:FF timecode tuple. */
data class Timecode(val hours: Int, val minutes: Int, val seconds: Int, val frames: Int) {
    override fun toString(): String =
        "%02d:%02d:%02d:%02d".format(hours, minutes, seconds, frames)

    companion object {
        /**
         * Decode the 4-byte little-endian frame returned by cmd 19.
         * Order in payload (per CLinkPacket.cs): hh, ss, mm, ff offsets vary
         * per sub-packet — caller passes start index, we read 4 successive
         * bytes as a tuple where byte[0]=ff, [1]=ss, [2]=mm, [3]=hh.
         *
         * NB: the original code reads packetData[6,5,4,3] for the generator —
         * the descending order is intentional because the C# uses
         * SetTimecode(packetData[6], packetData[5], packetData[4], packetData[3])
         * which is hh, mm, ss, ff.
         */
        fun fromBytes(bytes: ByteArray, offset: Int): Timecode {
            val ff = bytes[offset].toInt() and 0xFF
            val ss = bytes[offset + 1].toInt() and 0xFF
            val mm = bytes[offset + 2].toInt() and 0xFF
            val hh = bytes[offset + 3].toInt() and 0xFF
            return Timecode(hh, mm, ss, ff)
        }
    }
}

/** Aggregated TC payload returned by GetTimecode (cmd 19): 12 bytes total. */
data class TimecodeBlock(
    val generator: Timecode,
    val received: Timecode,
    val fileTime: Timecode,
) {
    companion object {
        fun fromPayload(payload: ByteArray): TimecodeBlock {
            require(payload.size >= 12) { "GetTimecode payload too short: ${payload.size}" }
            return TimecodeBlock(
                generator = Timecode.fromBytes(payload, 0),
                received  = Timecode.fromBytes(payload, 4),
                fileTime  = Timecode.fromBytes(payload, 8),
            )
        }
    }
}

/** Device info parameter selector — payload of cmd 65 GetDeviceInfo. */
enum class DeviceInfoParam(val code: Int) {
    ProductId(0),
    SerialNumber(1),
    SoftwareVersion(2),
    Cl12SoftwareVersion(3);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): DeviceInfoParam? = byCode[code]
    }
}

/** Product ID — first byte of advertisement 0x180A data and of cmd 65 response. */
enum class ProductId(val code: Int, val displayName: String) {
    MixPre3(0, "MixPre-3"),
    MixPre6(1, "MixPre-6"),
    MixPre10T(2, "MixPre-10T"),
    MixPre3II(3, "MixPre-3 II"),
    MixPre6II(4, "MixPre-6 II"),
    MixPre10II(5, "MixPre-10 II"),
    S833(6, "833"),
    S888(7, "888"),
    Scorpio(8, "Scorpio"),
    Unknown(0xFF, "Unknown");

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): ProductId? = byCode[code]
        fun fromCode(code: Int): ProductId = byCode[code] ?: Unknown
    }
}
