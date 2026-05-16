package com.sdremote.feature.transport

import com.sdremote.ui.meters.ChannelState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fake data source that emits a [TransportUi] at ~30 Hz, with believable
 * meter movement (sinusoidal base + jitter). Used until real BLE transport
 * lands.
 *
 * Channel names and rec/mute/off pattern preserved from JSX `WM_CHANNELS`
 * so the screen looks identical to design mockups in Preview.
 */
class MockTransportSource(
    private val periodMs: Long = 33L,  // ~30 Hz
) {
    private val baseChannels = listOf(
        ChannelBase(1, "Boom",   baseDb = -10f, swing = 6f, state = ChannelState.Record),
        ChannelBase(2, "Alan",   baseDb = -16f, swing = 5f, state = ChannelState.Record),
        ChannelBase(3, "Carla",  baseDb = -13f, swing = 8f, state = ChannelState.Record, limiter = true),
        ChannelBase(4, "Denise", baseDb = -21f, swing = 4f, state = ChannelState.Record),
        ChannelBase(5, "Unused", baseDb = -60f, swing = 0f, state = ChannelState.Off),
        ChannelBase(6, "Unused", baseDb = -60f, swing = 0f, state = ChannelState.Off),
        ChannelBase(7, "Beth",   baseDb = -12f, swing = 5f, state = ChannelState.Record),
        ChannelBase(8, "Henry",  baseDb = -18f, swing = 4f, state = ChannelState.Record),
        ChannelBase(9, "Marla",  baseDb = -60f, swing = 0f, state = ChannelState.Mute),
        ChannelBase(10, "Steve", baseDb = -60f, swing = 0f, state = ChannelState.Mute),
        ChannelBase(11, "Plant L", baseDb = -14f, swing = 5f, state = ChannelState.Record),
        ChannelBase(12, "Plant R", baseDb = -15f, swing = 5f, state = ChannelState.Record),
    )

    fun snapshots(): Flow<TransportUi> = flow {
        val start = System.currentTimeMillis()
        var tc = 0L  // tc tick = 1 frame
        while (true) {
            val tMs = System.currentTimeMillis() - start
            val tSec = tMs / 1000f
            val channels = baseChannels.map { it.sample(tSec) }
            val left  = sampleMix(tSec, phase = 0.0)
            val right = sampleMix(tSec, phase = 0.7)
            tc++
            emit(
                TransportUi(
                    scene = "14A",
                    take = 7,
                    recording = (tMs / 4000) % 2 == 0L,   // toggle every 4 s for demo
                    timecode = formatTimecode(tc, frameRate = 29.97f),
                    frameRate = "29.97 ND",
                    timeRemaining = "04:17 LEFT",
                    channels = channels,
                    mix = MixBus(left.first, left.second, right.first, right.second),
                )
            )
            delay(periodMs)
        }
    }

    private fun sampleMix(t: Float, phase: Double): Pair<Float, Float> {
        val level = (-6f + 3f * sin(2.0 * t + phase).toFloat() + Random.nextFloat() * 1.5f)
        val peak = level + 4f + Random.nextFloat() * 1.5f
        return level to peak.coerceAtMost(-0.2f)
    }
}

private data class ChannelBase(
    val id: Int,
    val name: String,
    val baseDb: Float,
    val swing: Float,
    val state: ChannelState,
    val limiter: Boolean = false,
) {
    fun sample(t: Float): ChannelSnapshot {
        if (state == ChannelState.Off || state == ChannelState.Mute) {
            return ChannelSnapshot(id, name, baseDb, baseDb, state, limiter)
        }
        // Each channel gets a deterministic phase from its id so they don't all swing in sync.
        val phase = id * 0.6
        val drift = swing * sin(1.7 * t + phase).toFloat()
        val flick = (Random.nextFloat() - 0.5f) * 2.4f
        val level = baseDb + drift + flick
        val peak = level + 4f + Random.nextFloat() * 1.6f
        return ChannelSnapshot(id, name, level, peak.coerceAtMost(-0.1f), state, limiter)
    }
}

/** HH:MM:SS:FF formatter. Frame count derived from absolute tick. */
internal fun formatTimecode(tickFrames: Long, frameRate: Float): String {
    val totalSeconds = (tickFrames / frameRate).toLong()
    val ff = (tickFrames % frameRate.toLong()).toInt()
    val ss = (totalSeconds % 60).toInt()
    val mm = ((totalSeconds / 60) % 60).toInt()
    val hh = (totalSeconds / 3600).toInt()
    fun p(n: Int) = n.toString().padStart(2, '0')
    return "${p(hh)}:${p(mm)}:${p(ss)}:${p(ff)}"
}
