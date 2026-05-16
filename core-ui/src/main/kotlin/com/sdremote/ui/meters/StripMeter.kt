package com.sdremote.ui.meters

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * Compact horizontal strip meter — JSX `WMStripMeter`.
 *
 * Used in V3 row layout and V4 track card grid. No label, no L/R tag —
 * just the bar. Caller sizes via [height] and parent width.
 */
@Composable
fun WmStripMeter(
    level: Float,
    peak: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val tokens = Wm.tokens
    Box(
        modifier = modifier
            .height(height)
            .background(tokens.meterTrack)
            .border(1.dp, tokens.border),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val levelPct = dbToPct(level)
            val peakPct = dbToPct(peak)

            // Solid fill
            drawRect(
                color = tokens.meterFill,
                topLeft = Offset(0f, 0f),
                size = Size(w * levelPct, h),
            )

            // Peak hold marker (2 px)
            if (isVisible(peak)) {
                val px = w * peakPct
                drawRect(
                    color = if (isClipping(peak)) tokens.clip else tokens.meterHold,
                    topLeft = Offset(px - 1f, 0f),
                    size = Size(2f, h),
                )
            }

            // 0 dBFS reference at 40% alpha
            val zx = w * dbToPct(0f)
            drawRect(
                color = tokens.clip.copy(alpha = 0.4f),
                topLeft = Offset(zx, 0f),
                size = Size(1f, h),
            )
        }
    }
}

@Preview(name = "StripMeter row dark", backgroundColor = 0xFF0C0C0D, showBackground = true, widthDp = 220)
@Composable
private fun StripMeterDark() {
    WmTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            WmStripMeter(level = -8f, peak = -3f, modifier = Modifier.fillMaxSize().height(8.dp))
            WmStripMeter(level = -14f, peak = -10f, modifier = Modifier.fillMaxSize().height(8.dp))
            WmStripMeter(level = -2f, peak = -0.2f, modifier = Modifier.fillMaxSize().height(8.dp))
        }
    }
}
