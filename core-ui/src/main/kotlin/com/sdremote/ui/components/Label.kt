package com.sdremote.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * Mono uppercase caption above a value — JSX `WMLabel`.
 *
 * Color defaults to [Wm.tokens].inkMute and text-transforms to uppercase.
 * Pass [size] to bump up to the 11 sp variant from the design.
 */
@Composable
fun WmLabel(
    text: String,
    modifier: Modifier = Modifier,
    sizeOverride: TextStyle? = null,
    icon: ImageVector? = null,
) {
    val style = sizeOverride ?: Wm.type.captionLabel
    Row(modifier = modifier) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Wm.tokens.inkMute,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text.uppercase(),
            color = Wm.tokens.inkMute,
            style = style,
            textAlign = TextAlign.Start,
        )
    }
}

@Preview(name = "WmLabel light", showBackground = true)
@Composable
private fun WmLabelLight() {
    WmTheme(darkTheme = false) {
        WmLabel("Scene · Take")
    }
}

@Preview(name = "WmLabel dark", showBackground = true, backgroundColor = 0xFF0C0C0D)
@Composable
private fun WmLabelDark() {
    WmTheme(darkTheme = true) {
        WmLabel("12 ch · Lim · Clip")
    }
}
