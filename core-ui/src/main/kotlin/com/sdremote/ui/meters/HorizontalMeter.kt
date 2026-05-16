package com.sdremote.ui.meters

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * Horizontal mix-bus meter — JSX `WMMixMeter`.
 *
 * Used for L/R master bus visualization. Includes:
 *   - L/R letter tag (label)
 *   - solid horizontal fill
 *   - 1 px overlay grid (visual rhythm)
 *   - 2 px peak hold marker
 *   - 0 dBFS reference tick line
 */
@Composable
fun WmMixMeter(
    label: String,
    level: Float,
    peak: Float,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    limiterActive: Boolean = false,
) {
    val tokens = Wm.tokens
    val isClip = isClipping(peak)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // L/R tag box
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = height)
                .background(when {
                    isClip        -> tokens.clip
                    limiterActive -> tokens.warn
                    else          -> tokens.surfaceAlt
                })
                .border(1.dp, tokens.border),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (isClip || limiterActive) Color.White else tokens.inkDim,
                style = Wm.type.pill.copy(fontSize = androidx.compose.ui.unit.TextUnit.Unspecified),
            )
        }

        // Bar itself
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height)
                .background(tokens.meterTrack)
                .border(1.dp, tokens.border),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val levelPct = dbToPct(level)
                val peakPct = dbToPct(peak)

                // Fill
                drawRect(
                    color = tokens.meterFill,
                    topLeft = Offset(0f, 0f),
                    size = Size(w * levelPct, h),
                )

                // Vertical grid (visual rhythm, every ~7px)
                var x = 6f
                while (x < w) {
                    drawRect(
                        color = tokens.bg.copy(alpha = 0.4f),
                        topLeft = Offset(x, 0f),
                        size = Size(1f, h),
                    )
                    x += 7f
                }

                // Peak hold (2 px)
                if (isVisible(peak)) {
                    val px = w * peakPct
                    drawRect(
                        color = if (isClip) tokens.clip else tokens.meterHold,
                        topLeft = Offset(px - 1f, 0f),
                        size = Size(2f, h),
                    )
                }

                // 0 dBFS reference tick (50% alpha)
                val zx = w * dbToPct(0f)
                drawRect(
                    color = tokens.clip.copy(alpha = 0.5f),
                    topLeft = Offset(zx, 0f),
                    size = Size(1f, h),
                )
            }
        }
    }
}

@Preview(name = "MixMeter dark", backgroundColor = 0xFF0C0C0D, showBackground = true, widthDp = 360)
@Composable
private fun MixMeterDark() {
    WmTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            WmMixMeter(label = "L", level = -6f, peak = -2f)
            WmMixMeter(label = "R", level = -7f, peak = -3f)
        }
    }
}
