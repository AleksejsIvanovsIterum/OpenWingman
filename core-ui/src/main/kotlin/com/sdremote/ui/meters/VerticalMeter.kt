package com.sdremote.ui.meters

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme
import com.sdremote.ui.theme.WmTokens

/** Vertical meter render style. */
enum class MeterFill { Segments, Solid }

/**
 * Vertical channel meter — JSX `WMMeterV`.
 *
 * Compose Canvas-based for performant 30+ fps polling. State should be
 * driven from a hot Flow upstream; this composable is pure render.
 *
 * Layout:
 *   ┌───────────────┐  ← limiter/clip bar (8dp)
 *   │               │
 *   │  segmented or │  ← meter body (height parameter)
 *   │  solid fill   │
 *   │               │
 *   └───────────────┘
 *      Channel name    ← rotated -90° label (truncated)
 *   ┌───────────────┐
 *   │      01       │  ← channel number badge
 *   └───────────────┘
 */
@Composable
fun WmMeterV(
    level: Float,
    peak: Float,
    name: String,
    channelNumber: Int,
    modifier: Modifier = Modifier,
    width: Dp = 18.dp,
    height: Dp = 220.dp,
    segments: Int = 32,
    state: ChannelState = ChannelState.Record,
    fillStyle: MeterFill = MeterFill.Segments,
    showTicks: Boolean = true,
    limiterActive: Boolean = false,
) {
    val tokens = Wm.tokens
    val isClip = isClipping(peak)
    val labelColor = when (state) {
        ChannelState.Off  -> tokens.inkMute
        ChannelState.Mute -> tokens.inkDim
        else              -> tokens.ink
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Limiter / clip bar
        Box(
            modifier = Modifier
                .width(width)
                .height(8.dp)
                .border(1.dp, tokens.border)
                .background(when {
                    isClip         -> tokens.clip
                    limiterActive  -> tokens.warn
                    else           -> Color.Transparent
                }),
        )

        // Body
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .background(tokens.meterTrack)
                .border(1.dp, tokens.border),
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(1.5.dp)) {
                drawVerticalMeterBody(
                    level = level,
                    peak = peak,
                    segments = segments,
                    state = state,
                    fillStyle = fillStyle,
                    showTicks = showTicks,
                    tokens = tokens,
                )
            }
        }

        // Rotated channel name (truncate via clip when too long)
        Box(
            modifier = Modifier.size(width = width, height = 56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name,
                color = labelColor,
                style = Wm.type.trackName.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified),
                modifier = Modifier.rotate(-90f),
                maxLines = 1,
            )
        }

        // Channel number badge
        val badgeBg = when (state) {
            ChannelState.Off  -> Color.Transparent
            ChannelState.Mute -> tokens.surfaceAlt
            else              -> tokens.meterFill
        }
        val badgeFg = when (state) {
            ChannelState.Off  -> tokens.inkMute
            ChannelState.Mute -> tokens.inkDim
            else              -> if (tokens.isDark) tokens.bg else tokens.surface
        }
        Box(
            modifier = Modifier
                .width(width)
                .height(18.dp)
                .background(badgeBg)
                .let { if (state == ChannelState.Off) it.border(1.dp, tokens.border) else it },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = channelNumber.toString().padStart(2, '0'),
                color = badgeFg,
                style = Wm.type.channelBadge,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVerticalMeterBody(
    level: Float,
    peak: Float,
    segments: Int,
    state: ChannelState,
    fillStyle: MeterFill,
    showTicks: Boolean,
    tokens: WmTokens,
) {
    val levelPct = dbToPct(level)
    val peakPct = dbToPct(peak)
    val w = size.width
    val h = size.height

    when (fillStyle) {
        MeterFill.Segments -> {
            val gap = 1.5f
            val segH = (h - gap * (segments - 1)) / segments
            val baseColor = when (state) {
                ChannelState.Mute -> tokens.meterFillSoft
                else              -> tokens.meterFill
            }
            for (i in 0 until segments) {
                val segPct = (i + 1).toFloat() / segments
                val lit = segPct <= levelPct
                if (!lit) continue
                val isTop = segPct > 0.92f  // top 8% is the "peak zone" red
                val color = if (isTop) tokens.meterPeak else baseColor
                val y = h - (i + 1) * (segH + gap) + gap  // counted from bottom
                drawRect(
                    color = color,
                    topLeft = Offset(0f, y),
                    size = Size(w, segH),
                )
            }
        }
        MeterFill.Solid -> {
            val fillH = h * levelPct
            // Gradient: meterFill 0-88%, then meterPeak transition
            drawRect(
                color = tokens.meterFill,
                topLeft = Offset(0f, h - fillH),
                size = Size(w, fillH),
            )
            // Quick peak-zone tint on the top portion
            val peakBandH = fillH * 0.12f
            if (peakBandH > 0f) {
                drawRect(
                    color = tokens.meterPeak,
                    topLeft = Offset(0f, h - fillH),
                    size = Size(w, peakBandH),
                )
            }
        }
    }

    // Peak hold line (2px)
    if (isVisible(peak)) {
        val y = h - h * peakPct
        val color = if (isClipping(peak)) tokens.clip else tokens.meterHold
        drawRect(
            color = color,
            topLeft = Offset(0f, y - 1f),
            size = Size(w, 2f),
        )
    }

    // dB scale ticks (right edge)
    if (showTicks) {
        val ticks = intArrayOf(0, -10, -20, -30)
        for (db in ticks) {
            val y = h - h * dbToPct(db.toFloat())
            drawRect(
                color = tokens.inkMute.copy(alpha = 0.5f),
                topLeft = Offset(w - 3f, y),
                size = Size(3f, 1f),
            )
        }
    }
}

@Preview(name = "VerticalMeter dark", backgroundColor = 0xFF0C0C0D, showBackground = true)
@Composable
private fun VerticalMeterDark() {
    WmTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Top,
        ) {
            WmMeterV(level = -8f, peak = -3f, name = "Boom", channelNumber = 1)
            WmMeterV(level = -14f, peak = -10f, name = "Alan", channelNumber = 2)
            WmMeterV(level = -3f, peak = -0.2f, name = "Carla", channelNumber = 3, limiterActive = true)
            WmMeterV(level = -60f, peak = -60f, name = "Unused", channelNumber = 5, state = ChannelState.Off)
            WmMeterV(level = -60f, peak = -60f, name = "Marla", channelNumber = 9, state = ChannelState.Mute)
        }
    }
}

@Preview(name = "VerticalMeter solid", backgroundColor = 0xFF0C0C0D, showBackground = true)
@Composable
private fun VerticalMeterSolid() {
    WmTheme(darkTheme = true) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Top,
        ) {
            WmMeterV(level = -8f, peak = -3f, name = "Boom", channelNumber = 1, fillStyle = MeterFill.Solid)
            WmMeterV(level = -2f, peak = -0.1f, name = "Plant", channelNumber = 11, fillStyle = MeterFill.Solid)
        }
    }
}
