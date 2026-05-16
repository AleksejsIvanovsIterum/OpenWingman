package com.sdremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/** Transport action — drives both icon choice and semantic role. */
enum class TransportAction { Rec, Stop, FalseTake, Circle, Play }

/**
 * Square transport button — JSX `WMTransportButton`.
 *
 * Renders a 14 dp rounded square with stroke border. Active REC variant
 * inverts background to `tokens.rec`, white icon, and adds a 4 dp glow ring
 * at 13% opacity (the design's 22% × 0.5 alpha → ≈ 13% over dark surface).
 *
 * Size defaults to 64 dp — the design's standard. Min hit-target enforced
 * by callers via parent layout.
 */
@Composable
fun WmTransportButton(
    action: TransportAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    size: Dp = 64.dp,
    label: String? = null,
) {
    val tokens = Wm.tokens
    val isRec = action == TransportAction.Rec
    val isActiveRec = isRec && active

    val bg = if (isActiveRec) tokens.rec else tokens.surface
    val fg = if (isActiveRec) Color.White else tokens.ink
    val ring = if (isActiveRec) tokens.rec else tokens.border

    val shape = RoundedCornerShape(14.dp)
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            // outer 4 dp glow ring for active REC
            .let { mod ->
                if (isActiveRec) mod.drawBehind {
                    drawRoundRect(
                        color = tokens.rec.copy(alpha = 0.22f),
                        style = Stroke(width = 8f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(56f, 56f),
                    )
                } else mod
            }
            .clip(shape)
            .background(bg)
            .border(1.5.dp, ring, shape)
            .clickable(
                onClick = onClick,
                interactionSource = interaction,
                indication = null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val iconSize = size * iconScale(action)
            Icon(
                imageVector = iconFor(action),
                contentDescription = action.name,
                tint = iconTint(action, active, fg),
                modifier = Modifier.size(iconSize),
            )
            if (label != null) {
                Text(
                    text = label.uppercase(),
                    color = if (isActiveRec) Color.White else tokens.inkDim,
                    style = Wm.type.pill,
                )
            }
        }
    }
}

private fun iconFor(action: TransportAction) = when (action) {
    TransportAction.Rec       -> WmIcons.Record
    TransportAction.Stop      -> WmIcons.Stop
    TransportAction.FalseTake -> WmIcons.FalseX
    TransportAction.Circle    -> WmIcons.Circle
    TransportAction.Play      -> WmIcons.Play
}

private fun iconScale(action: TransportAction): Float = when (action) {
    TransportAction.Rec, TransportAction.Play -> 0.36f
    TransportAction.Stop                      -> 0.32f
    TransportAction.FalseTake                 -> 0.38f
    TransportAction.Circle                    -> 0.42f
}

@Composable
private fun iconTint(action: TransportAction, active: Boolean, fg: Color): Color {
    val tokens = Wm.tokens
    return when {
        action == TransportAction.Rec && active -> Color.White
        action == TransportAction.Rec           -> tokens.rec  // idle REC icon stays red
        else                                    -> fg
    }
}

@Preview(name = "Transport row dark", backgroundColor = 0xFF0C0C0D, showBackground = true, widthDp = 393)
@Composable
private fun TransportRowDark() {
    WmTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            WmTransportButton(TransportAction.Rec, {}, active = true, label = "REC")
            WmTransportButton(TransportAction.Stop, {}, label = "STOP")
            WmTransportButton(TransportAction.FalseTake, {}, label = "FALSE")
            WmTransportButton(TransportAction.Circle, {}, label = "CIRCLE")
            WmTransportButton(TransportAction.Play, {}, label = "PLAY")
        }
    }
}

@Preview(name = "Transport row light", backgroundColor = 0xFFF5F4EF, showBackground = true, widthDp = 393)
@Composable
private fun TransportRowLight() {
    WmTheme(darkTheme = false) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            WmTransportButton(TransportAction.Rec, {}, label = "REC")
            WmTransportButton(TransportAction.Stop, {}, label = "STOP")
            WmTransportButton(TransportAction.FalseTake, {}, label = "FALSE")
            WmTransportButton(TransportAction.Circle, {}, label = "CIRCLE")
            WmTransportButton(TransportAction.Play, {}, label = "PLAY")
        }
    }
}
