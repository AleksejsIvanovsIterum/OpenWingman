package com.sdremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme
import com.sdremote.ui.theme.WmTokens

/** WMPill variants — visual kind. */
enum class PillKind { Default, Outline, Rec, Inverted }
enum class PillSize { Sm, Md }

/**
 * Status pill — uppercase mono badge. Mirrors JSX `WMPill`.
 *
 * Pass a leading dot via [withRedDot] for REC variant per V2 mockup.
 */
@Composable
fun WmPill(
    text: String,
    modifier: Modifier = Modifier,
    kind: PillKind = PillKind.Default,
    size: PillSize = PillSize.Sm,
    withRedDot: Boolean = false,
) {
    val tokens = Wm.tokens
    val (bg, fg, hasBorder) = when (kind) {
        PillKind.Rec      -> Triple(tokens.rec, Color.White, false)
        PillKind.Inverted -> Triple(tokens.ink, if (tokens.isDark) tokens.bg else tokens.surface, false)
        PillKind.Outline  -> Triple(Color.Transparent, tokens.inkDim, true)
        PillKind.Default  -> Triple(tokens.surfaceAlt, tokens.ink, false)
    }
    val pad = when (size) {
        PillSize.Sm -> PaddingValues(horizontal = 7.dp, vertical = 3.dp)
        PillSize.Md -> PaddingValues(horizontal = 10.dp, vertical = 5.dp)
    }
    val textStyle: TextStyle = when (size) {
        PillSize.Sm -> Wm.type.pill
        PillSize.Md -> Wm.type.pill.copy(fontSize = 11.sp)
    }
    val shape = RoundedCornerShape(4.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .let { if (hasBorder) it.border(1.dp, tokens.border, shape) else it }
            .padding(pad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (withRedDot) {
            val dotColor = if (kind == PillKind.Rec) Color.White else tokens.rec
            Row(modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)) {}
        }
        Text(text = text.uppercase(), color = fg, style = textStyle)
    }
}

@Preview(name = "Pills dark", backgroundColor = 0xFF0C0C0D, showBackground = true)
@Composable
private fun PillsDark() {
    WmTheme(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WmPill("STBY", kind = PillKind.Outline)
            WmPill("REC", kind = PillKind.Rec, withRedDot = true)
            WmPill("BWF", kind = PillKind.Default)
            WmPill("POLY", kind = PillKind.Inverted)
        }
    }
}

@Preview(name = "Pills light", backgroundColor = 0xFFF5F4EF, showBackground = true)
@Composable
private fun PillsLight() {
    WmTheme(darkTheme = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WmPill("STBY", kind = PillKind.Outline)
            WmPill("REC", kind = PillKind.Rec, withRedDot = true)
            WmPill("BWF", kind = PillKind.Default)
            WmPill("POLY", kind = PillKind.Inverted)
        }
    }
}

// Used internally by some components, kept minimal.
internal fun WmTokens.bgFor(kind: PillKind): Color = when (kind) {
    PillKind.Rec -> rec
    PillKind.Inverted -> ink
    PillKind.Outline -> Color.Transparent
    PillKind.Default -> surfaceAlt
}
