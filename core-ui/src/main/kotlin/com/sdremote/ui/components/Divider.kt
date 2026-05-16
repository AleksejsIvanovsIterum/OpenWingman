package com.sdremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * Horizontal divider, optional centered label between two lines.
 * Mirrors JSX `WMDivider`.
 */
@Composable
fun WmDivider(
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = Wm.tokens
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(tokens.border),
        )
        if (label != null) {
            Text(
                text = label.uppercase(),
                color = tokens.inkMute,
                style = Wm.type.dividerLabel,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(tokens.border),
            )
        }
    }
}

/** Plain hairline, no label. Use for in-screen separators. */
@Composable
fun WmHairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Wm.tokens.border),
    )
}

@Preview(name = "Divider dark", backgroundColor = 0xFF0C0C0D, showBackground = true, widthDp = 360)
@Composable
private fun DividerDark() {
    WmTheme(darkTheme = true) {
        WmDivider(label = "Mix bus", modifier = Modifier.padding(8.dp))
    }
}
