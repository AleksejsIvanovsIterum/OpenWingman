package com.sdremote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFeature
import androidx.compose.ui.text.font.FontFeatureSettings
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmFontMono
import com.sdremote.ui.theme.WmTheme

/**
 * HH:MM:SS:FF monospace timecode — JSX `WMTimecode`.
 *
 * Splits into 4 segments. Separators (`:`) rendered in [Wm.tokens].inkMute.
 * Frame portion (FF) rendered in [Wm.tokens].inkDim so the eye lands on
 * seconds first.
 *
 * Uses tabular-nums via [FontFeatureSettings] so digits don't reflow.
 *
 * @param value HH:MM:SS:FF format (e.g. "00:27:42:13").
 * @param size base font size; if null falls back to the design's tcMonolith.
 */
@Composable
fun WmTimecode(
    value: String = "00:00:00:00",
    modifier: Modifier = Modifier,
    style: TextStyle = Wm.type.tcMonolith,
) {
    val parts = value.split(":").let {
        if (it.size == 4) it else listOf("00", "00", "00", "00")
    }
    val (hh, mm, ss, ff) = parts

    val main = style.copy(
        fontFamily = WmFontMono,
        fontFeatureSettings = "tnum",
        color = Wm.tokens.ink,
    )
    val sep = main.copy(color = Wm.tokens.inkMute)
    val frame = main.copy(
        color = Wm.tokens.inkDim,
        // FF is slightly smaller for hierarchical emphasis (per V2 mockup)
        fontSize = (style.fontSize.value * 0.68f).sp,
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(hh, style = main)
        Text(":", style = sep)
        Text(mm, style = main)
        Text(":", style = sep)
        Text(ss, style = main)
        Text(":", style = sep)
        Text(ff, style = frame)
    }
}

@Preview(name = "Timecode monolith dark", backgroundColor = 0xFF0C0C0D, showBackground = true)
@Composable
private fun TimecodeMonolithDark() {
    WmTheme(darkTheme = true) {
        WmTimecode(value = "00:27:42:13")
    }
}

@Preview(name = "Timecode large light", backgroundColor = 0xFFF5F4EF, showBackground = true)
@Composable
private fun TimecodeLargeLight() {
    WmTheme(darkTheme = false) {
        WmTimecode(value = "00:27:42:13", style = Wm.type.tcLarge)
    }
}
