package com.sdremote.domain

import com.sdremote.protocol.commands.ChangeStatus
import com.sdremote.protocol.commands.ChannelMeter
import com.sdremote.protocol.commands.MeterGroup

/**
 * Coalesced view of "what the device is doing right now".
 *
 * Built by [Session]'s poll loop from the most recent meter, timecode,
 * transport-status and unit-mode responses. UI features observe this via
 * [Session.snapshots] and adapt freely (e.g. each transport screen variant
 * presents the same data differently).
 */
data class DeviceSnapshot(
    val timecode: String = "00:00:00:00",
    val transportRecording: Boolean = false,
    val scene: String = "",
    val take: Int = 0,
    val takeHandle: Long = 0L,
    val inputs: MeterGroup = MeterGroup(emptyList()),
    val outputs: MeterGroup = MeterGroup(emptyList()),
    val tracks: MeterGroup = MeterGroup(emptyList()),
    val mixLeft: ChannelMeter = ChannelMeter(0, 0, 0),
    val mixRight: ChannelMeter = ChannelMeter(0, 0, 0),
    val change: ChangeStatus? = null,
)
