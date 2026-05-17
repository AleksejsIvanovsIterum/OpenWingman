package com.sdremote.protocol

/**
 * All 39 CLink commands, recovered from CLinkCommand.cs (Wingman 5.03).
 *
 * Values are the raw byte that goes in packet[3]. Two have shared codes:
 *  - GetPowerVoltage and GetPowerStatus both = 89 (0x59), context-dependent.
 *
 * Add new commands here as Sound Devices firmware evolves — keep
 * everything alphabetically ordered within their numeric blocks for diffs.
 */
@Suppress("MagicNumber")
enum class CommandId(val code: Int) {
    InvalidCommand(0),

    GetVersion(1),
    SetTimeDate(12),
    GetTimeDate(14),

    TransportControl(17),
    TransportStatus(18),
    GetTimecode(19),

    GetTakeList(56),
    GetTakeParameter(57),
    SetTakeParameter(58),
    FalseTake(59),

    GetSetting(60),
    SetSetting(61),
    GetMeters(62),

    GetDeviceInfo(65),

    GetTakeListChangeStatus(78),

    SetInputRouting(81),
    GetInputRouting(82),

    GetParameterChangeStatus(85),

    GetDriveStatus(88),
    // 89 is both GetPowerVoltage and GetPowerStatus depending on payload.
    GetPowerStatus(89),

    SetTimecodeGenerator(90),
    SetBaudRate(91),

    GetRecordFolder(92),
    SetRecordFolder(93),

    GetChannelLinking(94),
    SetChannelLinking(95),

    Authenticate(96),

    GetExtendedParameterChangeStatus(97),

    GetDynSettingsList(98),
    SetDynSettingsList(99),

    SoundReport(100),
    MediaList(101),
    ShowMessage(102),
    ValidatePassword(103),
    GetUnitMode(104);

    val byte: Byte get() = code.toByte()

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): CommandId? = byCode[code]
        fun fromByte(b: Byte): CommandId? = fromCodeOrNull(b.toInt() and 0xFF)
    }
}

/**
 * Response codes returned in the first payload byte of an ACK packet.
 * 0 = success; anything else triggers the error handler.
 */
@Suppress("MagicNumber")
enum class ResponseCode(val code: Int) {
    Success(0),
    CommandNotSupported(1),
    InvalidParameters(2),
    Busy(3),
    ParameterOutOfRange(4),
    NoResponse(5),
    CommandSpecific(6),
    OperationFailure(7);

    companion object {
        private val byCode = entries.associateBy { it.code }
        fun fromCodeOrNull(code: Int): ResponseCode? = byCode[code]
    }
}
