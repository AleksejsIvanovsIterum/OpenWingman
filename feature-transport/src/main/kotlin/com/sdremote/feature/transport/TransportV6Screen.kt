package com.sdremote.feature.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdremote.ui.components.PillKind
import com.sdremote.ui.components.WmHairline
import com.sdremote.ui.components.WmLabel
import com.sdremote.ui.components.WmPill
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.meters.WmMixMeter
import com.sdremote.ui.meters.dbToPct
import com.sdremote.ui.meters.isClipping
import com.sdremote.ui.meters.isVisible
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * V6 — One-Hand / Stage transport screen.
 *
 * Layout (top → bottom):
 *   1. Glance band:  Scene/Take big numbers + STBY/REC pill
 *   2. TC card:      inverted dark monolith with frame rate + time remaining
 *   3. Live meters:  12 thin vertical bars + L/R master strip (compact)
 *   4. Thumb zone:   huge RECORD (2× width) + STOP, then 3 small secondaries
 */
@Composable
fun TransportV6Screen(
    viewModel: TransportViewModel = viewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    TransportV6Content(
        ui = ui,
        onRec = viewModel::onRec,
        onStop = viewModel::onStop,
        onFalseTake = viewModel::onFalseTake,
        onCircle = viewModel::onCircle,
        onPlay = viewModel::onPlay,
    )
}

@Composable
internal fun TransportV6Content(
    ui: TransportUi,
    onRec: () -> Unit,
    onStop: () -> Unit,
    onFalseTake: () -> Unit,
    onCircle: () -> Unit,
    onPlay: () -> Unit,
) {
    val tokens = Wm.tokens
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxSize().background(tokens.bg),
    ) {
        // ── 1. Glance band ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                WmLabel(text = "Scene · Take")
                Text(
                    text = "${ui.scene} / ${ui.take.toString().padStart(2, '0')}",
                    color = tokens.ink,
                    style = Wm.type.sceneTake.copy(fontSize = 30.sp),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            WmPill(
                text = if (ui.recording) "REC" else "STBY",
                kind = if (ui.recording) PillKind.Rec else PillKind.Outline,
                withRedDot = ui.recording,
            )
        }

        // ── 2. TC card ──
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(tokens.ink)
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TIMECODE",
                    color = tokens.bg.copy(alpha = 0.7f),
                    style = Wm.type.pill,
                )
                TcInverted(value = ui.timecode, tokens.bg)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = ui.frameRate,
                        color = tokens.bg.copy(alpha = 0.7f),
                        style = Wm.type.dataValue.copy(fontSize = 10.sp),
                    )
                    Text(
                        text = ui.timeRemaining,
                        color = tokens.bg.copy(alpha = 0.7f),
                        style = Wm.type.dataValue.copy(fontSize = 10.sp),
                    )
                }
            }
        }

        // ── 3. Meters ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WmLabel(text = "Live Meters")
                WmLabel(text = "${ui.channels.size} ch")
            }
            // Compact vertical bars — no labels, optimized for glance
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                ui.channels.forEach { ch ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(tokens.meterTrack)
                                .border(1.dp, tokens.borderSoft),
                        ) {
                            CompactBar(level = ch.level, peak = ch.peak, state = ch.state)
                        }
                        Text(
                            text = ch.id.toString(),
                            color = if (ch.state == com.sdremote.ui.meters.ChannelState.Off)
                                tokens.inkMute else tokens.inkDim,
                            style = Wm.type.dataValue.copy(fontSize = 8.sp),
                        )
                    }
                }
            }
            WmMixMeter(label = "L", level = ui.mix.leftLevel, peak = ui.mix.leftPeak)
            WmMixMeter(label = "R", level = ui.mix.rightLevel, peak = ui.mix.rightPeak)
        }

        // ── 4. Thumb zone ──
        WmHairline()
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Big REC + STOP pair
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigButton(
                    label = "RECORD",
                    icon = WmIcons.Record,
                    bg = tokens.rec,
                    fg = Color.White,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRec()
                    },
                    modifier = Modifier.weight(2f).height(64.dp),
                )
                BigButton(
                    label = "STOP",
                    icon = WmIcons.Stop,
                    bg = tokens.surface,
                    fg = tokens.ink,
                    border = tokens.border,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStop()
                    },
                    modifier = Modifier.weight(1f).height(64.dp),
                )
            }
            // Secondary row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("FALSE",  WmIcons.FalseX, onClick = onFalseTake, modifier = Modifier.weight(1f).height(48.dp))
                SecondaryButton("CIRCLE", WmIcons.Circle, onClick = onCircle,    modifier = Modifier.weight(1f).height(48.dp))
                SecondaryButton("PLAY",   WmIcons.Play,   onClick = onPlay,      modifier = Modifier.weight(1f).height(48.dp))
            }
        }
    }
}

@Composable
private fun TcInverted(value: String, separatorColor: Color) {
    val parts = value.split(":").let { if (it.size == 4) it else listOf("00","00","00","00") }
    val (hh, mm, ss, ff) = parts
    val main = Wm.type.tcMonolith.copy(
        fontSize = 44.sp,
        color = Wm.tokens.bg,
        fontFeatureSettings = "tnum",
    )
    val sep = main.copy(color = separatorColor.copy(alpha = 0.5f))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(hh, style = main); Text(":", style = sep)
        Text(mm, style = main); Text(":", style = sep)
        Text(ss, style = main); Text(":", style = sep)
        Text(ff, style = main)
    }
}

@Composable
private fun CompactBar(
    level: Float,
    peak: Float,
    state: com.sdremote.ui.meters.ChannelState,
) {
    val tokens = Wm.tokens
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val isOff = state == com.sdremote.ui.meters.ChannelState.Off
        val levelPct = dbToPct(level)
        val peakPct = dbToPct(peak)
        // Fill
        if (!isOff) {
            drawRect(
                color = tokens.meterFill,
                topLeft = androidx.compose.ui.geometry.Offset(0f, h - h * levelPct),
                size = androidx.compose.ui.geometry.Size(w, h * levelPct),
            )
        }
        // Peak hold
        if (!isOff && isVisible(peak)) {
            drawRect(
                color = if (isClipping(peak)) tokens.clip else tokens.meterPeak,
                topLeft = androidx.compose.ui.geometry.Offset(0f, h - h * peakPct - 1f),
                size = androidx.compose.ui.geometry.Size(w, 2f),
            )
        }
    }
}

@Composable
private fun BigButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bg: Color,
    fg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: Color? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bg)
            .let { if (border != null) it.border(1.5.dp, border, RectangleShape) else it }
            .clickable(onClick = onClick, interactionSource = interaction, indication = null),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = fg, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                color = fg,
                style = Wm.type.dataValue.copy(fontSize = 14.sp),
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = Wm.tokens
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .background(tokens.surface)
            .border(1.dp, tokens.border, RectangleShape)
            .clickable(onClick = onClick, interactionSource = interaction, indication = null),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tokens.inkDim, modifier = Modifier.size(13.dp))
            Text(
                text = label,
                color = tokens.inkDim,
                style = Wm.type.dataValue.copy(fontSize = 11.sp),
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// Previews
// ────────────────────────────────────────────────────────────
@Preview(name = "V6 dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun TransportV6Preview() {
    val source = remember { MockTransportSource() }
    WmTheme(darkTheme = true) {
        val ui = TransportUi(
            scene = "14A", take = 7, recording = false,
            timecode = "00:27:42:13",
            frameRate = "29.97 ND", timeRemaining = "04:17 LEFT",
            channels = listOf(
                ChannelSnapshot(1, "Boom", -8f, -3f),
                ChannelSnapshot(2, "Alan", -14f, -10f),
                ChannelSnapshot(3, "Carla", -11f, -6f, limiterActive = true),
                ChannelSnapshot(4, "Denise", -19f, -14f),
                ChannelSnapshot(5, "Unused", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Off),
                ChannelSnapshot(6, "Unused", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Off),
                ChannelSnapshot(7, "Beth", -10f, -4f),
                ChannelSnapshot(8, "Henry", -16f, -12f),
                ChannelSnapshot(9, "Marla", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Mute),
                ChannelSnapshot(10, "Steve", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Mute),
                ChannelSnapshot(11, "Plant L", -12f, -8f),
                ChannelSnapshot(12, "Plant R", -13f, -9f),
            ),
            mix = MixBus(leftLevel = -6f, leftPeak = -2f, rightLevel = -7f, rightPeak = -3f),
        )
        TransportV6Content(ui = ui, onRec = {}, onStop = {}, onFalseTake = {}, onCircle = {}, onPlay = {})
    }
}

@Preview(name = "V6 light", backgroundColor = 0xFFF5F4EF, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun TransportV6PreviewLight() {
    WmTheme(darkTheme = false) {
        val ui = TransportUi(
            scene = "14A", take = 7, recording = true,
            timecode = "00:27:42:13",
            frameRate = "29.97 ND", timeRemaining = "04:17 LEFT",
            channels = listOf(
                ChannelSnapshot(1, "Boom", -8f, -3f),
                ChannelSnapshot(2, "Alan", -14f, -10f),
                ChannelSnapshot(3, "Carla", -11f, -6f, limiterActive = true),
                ChannelSnapshot(4, "Denise", -19f, -14f),
                ChannelSnapshot(5, "Unused", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Off),
                ChannelSnapshot(6, "Unused", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Off),
                ChannelSnapshot(7, "Beth", -10f, -4f),
                ChannelSnapshot(8, "Henry", -16f, -12f),
                ChannelSnapshot(9, "Marla", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Mute),
                ChannelSnapshot(10, "Steve", -60f, -60f, state = com.sdremote.ui.meters.ChannelState.Mute),
                ChannelSnapshot(11, "Plant L", -12f, -8f),
                ChannelSnapshot(12, "Plant R", -13f, -9f),
            ),
            mix = MixBus(leftLevel = -6f, leftPeak = -2f, rightLevel = -7f, rightPeak = -3f),
        )
        TransportV6Content(ui = ui, onRec = {}, onStop = {}, onFalseTake = {}, onCircle = {}, onPlay = {})
    }
}

