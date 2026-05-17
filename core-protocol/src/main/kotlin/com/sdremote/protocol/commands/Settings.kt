package com.sdremote.protocol.commands

import com.sdremote.protocol.Codec
import com.sdremote.protocol.CommandId
import com.sdremote.protocol.Frame

/** Subset of CLSetting — fill in the rest as you need them. */
enum class Setting(val code: Int) {
    SampleRate(0),
    BitDepth(1),
    RecordTrackArms(2),
    InputEnableBitmap(3),
    TimecodeFrameRate(4),
    MeterBallistics(5),
    PeakHold(6),
    OutputMetersVUCollectionEnable(7),
    TimecodeMode(8),
    AutoMixerMode(9),
    AutoMixerEnables(10),
    AutoMixerAttenuation(11),
    MeterGroup(12),
    PlaybackEnableBitmap(13),
    Channel1Linking(14),
    Channel3Linking(15),
    Channel5Linking(16),
    Channel7Linking(17),
    Channel9Linking(18),
    Channel11Linking(19);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): Setting? = byCode[code]
    }
}

/** Read one setting (cmd 60). */
data class GetSetting(val setting: Setting) : CLinkRequest {
    override val id = CommandId.GetSetting
    override fun serializePayload(): ByteArray =
        Codec.Writer(2).u16(setting.code).toByteArray()
}

/** Write one setting (cmd 61). */
data class SetSetting(val setting: Setting, val value: Int) : CLinkRequest {
    override val id = CommandId.SetSetting
    override fun serializePayload(): ByteArray =
        Codec.Writer(4).u16(setting.code).u16(value).toByteArray()
}

/** Read input/output channel linking bitmaps (cmd 94). */
data object GetChannelLinking : CLinkRequest {
    override val id = CommandId.GetChannelLinking
}

/** DATA frame for GetSetting. */
data class SettingResponse(
    val setting: Setting,
    val value: Int,
    override val sourceFrame: Frame,
) : CLinkResponse {
    companion object {
        fun parse(frame: Frame): SettingResponse? {
            if (frame.command != CommandId.GetSetting.byte) return null
            if (frame.payload.size < 4) return null
            val sCode = Codec.readU16(frame.payload, 0)
            val setting = Setting.fromCodeOrNull(sCode) ?: return null
            val value = Codec.readU16(frame.payload, 2)
            return SettingResponse(setting, value, frame)
        }
    }
}

/** DATA frame for GetChannelLinking. */
data class ChannelLinkingResponse(
    val outputs: Int,
    val inputs: Int,
    val inputsMS: Int,
    override val sourceFrame: Frame,
) : CLinkResponse {
    companion object {
        fun parse(frame: Frame): ChannelLinkingResponse? {
            if (frame.command != CommandId.GetChannelLinking.byte) return null
            if (frame.payload.size < 6) return null
            return ChannelLinkingResponse(
                outputs = Codec.readU16(frame.payload, 0),
                inputs = Codec.readU16(frame.payload, 2),
                inputsMS = Codec.readU16(frame.payload, 4),
                sourceFrame = frame,
            )
        }
    }
}
