package com.sdremote.protocol.commands

import com.sdremote.protocol.Codec
import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame
import com.sdremote.protocol.types.DeviceInfoParam
import com.sdremote.protocol.types.DeviceMode
import com.sdremote.protocol.types.ProductId

/** Query one field of device info (cmd 65). */
data class GetDeviceInfo(val param: DeviceInfoParam) : CLinkRequest {
    override val id = CommandId.GetDeviceInfo
    override fun serializePayload(): ByteArray = byteArrayOf(param.code.toByte())
}

/** Poll unit mode + feature flags (cmd 104). */
data object GetUnitMode : CLinkRequest {
    override val id = CommandId.GetUnitMode
}

/** Sealed result type for GetDeviceInfo — shape depends on requested [DeviceInfoParam]. */
sealed interface DeviceInfoResponse : CLinkResponse {
    data class ProductIdResult(val product: ProductId, override val sourceFrame: Frame) : DeviceInfoResponse
    data class SerialNumberResult(val serial: String, override val sourceFrame: Frame) : DeviceInfoResponse
    /** Reported as (major, minor). */
    data class FirmwareVersionResult(val major: Int, val minor: Int, override val sourceFrame: Frame) : DeviceInfoResponse
    data class Cl12FirmwareVersionResult(val major: Int, val minor: Int, override val sourceFrame: Frame) : DeviceInfoResponse

    companion object {
        /**
         * Parse a DeviceInfo DATA frame. Caller must pass the [param] they
         * requested — the device echoes the parameter byte at payload[0],
         * we validate against that.
         */
        fun parse(frame: Frame, requested: DeviceInfoParam): DeviceInfoResponse? {
            // Routing now handled by Session.pendingCmd, not by command-byte echo.
            val payload = frame.payload
            if (payload.isEmpty()) return null

            return when (requested) {
                DeviceInfoParam.ProductId -> {
                    val code = Codec.readU8(payload, 0)
                    ProductIdResult(ProductId.fromCode(code), frame)
                }
                DeviceInfoParam.SerialNumber -> {
                    // First byte is the param echo; serial starts at offset 0 in the
                    // original C# (cf. CLinkPacket.GetString with offset=0 + base offset 4).
                    val sn = Codec.readString(payload, offset = 0)
                    SerialNumberResult(sn, frame)
                }
                DeviceInfoParam.SoftwareVersion -> {
                    if (payload.size < 2) return null
                    var major = Codec.readU8(payload, 1)
                    val minor = Codec.readU8(payload, 0)
                    if (major == 0xFF) major = 1
                    FirmwareVersionResult(major, minor, frame)
                }
                DeviceInfoParam.Cl12SoftwareVersion -> {
                    if (payload.size < 2) return null
                    val minor = Codec.readU8(payload, 0)
                    val major = Codec.readU8(payload, 1)
                    Cl12FirmwareVersionResult(major, minor, frame)
                }
            }
        }
    }
}

/**
 * Decoded GetUnitMode response (cmd 104).
 *
 * Layout (after dataLen byte):
 *   [+0..1]   DeviceMode (u16 LE)
 *   [+2..5]   FeatureGroups bitmap (u32 LE)
 *   [+6..7]   RecordOptions (u16 LE)
 *   [+8..9]   ChannelOptions (u16 LE)
 *   [+10..11] reserved
 *   [+12..13] MetadataOptions (u16 LE)
 */
data class UnitModeResponse(
    val mode: DeviceMode,
    val featureGroups: Long,
    val recordOptions: Int,
    val channelOptions: Int,
    val metadataOptions: Int,
    override val sourceFrame: Frame,
) : CLinkResponse {
    companion object {
        fun parse(frame: Frame): UnitModeResponse? {
            // Routing now handled by Session.pendingCmd, not by command-byte echo.
            val p = frame.payload
            if (p.size < 14) return null
            val modeCode = Codec.readU16(p, 0)
            val mode = DeviceMode.fromCodeOrNull(modeCode) ?: DeviceMode.Unknown
            return UnitModeResponse(
                mode = mode,
                featureGroups = Codec.readU32(p, 2),
                recordOptions = Codec.readU16(p, 6),
                channelOptions = Codec.readU16(p, 8),
                metadataOptions = Codec.readU16(p, 12),
                sourceFrame = frame,
            )
        }
    }
}
