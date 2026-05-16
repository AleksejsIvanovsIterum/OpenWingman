package com.sdremote.feature.transport

import com.sdremote.ui.meters.ChannelState

/** Snapshot of a single channel's meter state. */
data class ChannelSnapshot(
    val id: Int,
    val name: String,
    val level: Float,
    val peak: Float,
    val state: ChannelState = ChannelState.Record,
    val limiterActive: Boolean = false,
)

/** Snapshot of the master mix bus. */
data class MixBus(
    val leftLevel: Float,
    val leftPeak: Float,
    val rightLevel: Float,
    val rightPeak: Float,
)

/** Transport-screen UI state. */
data class TransportUi(
    val scene: String = "14A",
    val take: Int = 7,
    val recording: Boolean = false,
    val timecode: String = "00:00:00:00",
    val frameRate: String = "29.97 ND",
    val timeRemaining: String = "—",
    val channels: List<ChannelSnapshot> = emptyList(),
    val mix: MixBus = MixBus(-60f, -60f, -60f, -60f),
)
