package com.sdremote.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.ui.components.WmHairline
import com.sdremote.ui.components.WmLabel
import com.sdremote.ui.frame.WmTopBar
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

data class ReportTile(val label: String, val value: String)
data class SceneRollup(val scene: String, val takes: Int, val circled: Int, val duration: String)

private val SampleTiles = listOf(
    ReportTile("Takes", "142"),
    ReportTile("Circled", "38"),
    ReportTile("False", "17"),
    ReportTile("Duration", "04:12:33"),
)

private val SampleScenes = listOf(
    SceneRollup("14A", 7, 2, "06:42"),
    SceneRollup("14B", 4, 1, "03:18"),
    SceneRollup("13C", 4, 1, "04:55"),
    SceneRollup("13B", 6, 2, "07:11"),
    SceneRollup("13A", 8, 3, "09:24"),
    SceneRollup("12D", 3, 0, "02:42"),
    SceneRollup("12C", 5, 2, "05:18"),
)

@Composable
fun ReportsScreen(
    day: String = "Day 12 · Wed 16 May",
    synced: Boolean = true,
    tiles: List<ReportTile> = SampleTiles,
    scenes: List<SceneRollup> = SampleScenes,
    onExportPdf: () -> Unit = {},
    onEmailCsv: () -> Unit = {},
    onSceneOpen: (SceneRollup) -> Unit = {},
) {
    val tokens = Wm.tokens
    Column(modifier = Modifier.fillMaxSize().background(tokens.bg)) {
        WmTopBar(
            title = "Sound Report",
            subtitle = day,
            rightSlot = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = WmIcons.Check,
                        contentDescription = null,
                        tint = if (synced) tokens.ok else tokens.warn,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (synced) "SYNCED" else "PENDING",
                        color = tokens.ink,
                        style = Wm.type.pill.copy(fontSize = 10.sp),
                    )
                }
            },
        )

        // Summary tiles (2×2 grid implemented with nested Rows so we don't need foundation-layout grid)
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            tiles.chunked(2).forEachIndexed { idx, pair ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = if (idx == 0) 0.dp else 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { tile ->
                        SummaryTile(tile = tile, modifier = Modifier.weight(1f))
                    }
                    // pad to 2 columns if odd count
                    if (pair.size == 1) Box(modifier = Modifier.weight(1f))
                }
            }
        }

        // Scenes header
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            WmLabel(text = "Scenes")
        }

        // Scene list
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
        ) {
            items(scenes, key = { it.scene }) { sc ->
                SceneRow(sc, onClick = { onSceneOpen(sc) })
                if (sc != scenes.last()) WmHairline()
            }
        }

        // Bottom actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RectangleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomAction(label = "EXPORT PDF", primary = true, modifier = Modifier.weight(1f), onClick = onExportPdf)
            BottomAction(label = "EMAIL CSV", primary = false, modifier = Modifier.weight(1f), onClick = onEmailCsv)
        }
    }
}

@Composable
private fun SummaryTile(tile: ReportTile, modifier: Modifier = Modifier) {
    val tokens = Wm.tokens
    Column(
        modifier = modifier
            .border(1.dp, tokens.border, RectangleShape)
            .background(tokens.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        WmLabel(text = tile.label)
        Text(
            text = tile.value,
            color = tokens.ink,
            style = Wm.type.tcLarge.copy(fontSize = 22.sp),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun SceneRow(sc: SceneRollup, onClick: () -> Unit) {
    val tokens = Wm.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = sc.scene,
            color = tokens.ink,
            style = Wm.type.dataValue.copy(fontSize = 15.sp),
            modifier = Modifier.size(width = 60.dp, height = 18.dp),
        )
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = "${sc.takes} takes · ",
                color = tokens.inkDim,
                style = Wm.type.dataValue.copy(fontSize = 12.sp),
            )
            Text(
                text = "${sc.circled} circled",
                color = tokens.ok,
                style = Wm.type.dataValue.copy(fontSize = 12.sp),
            )
        }
        Text(
            text = sc.duration,
            color = tokens.inkDim,
            style = Wm.type.dataValue.copy(fontSize = 12.sp),
        )
        Icon(
            imageVector = WmIcons.ChevronRight,
            contentDescription = null,
            tint = tokens.inkMute,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun BottomAction(
    label: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = Wm.tokens
    Box(
        modifier = modifier
            .height(44.dp)
            .background(if (primary) tokens.ink else tokens.surface)
            .let { if (!primary) it.border(1.5.dp, tokens.border, RectangleShape) else it }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (primary) tokens.bg else tokens.ink,
            style = Wm.type.dataValue.copy(fontSize = 11.sp),
        )
    }
}

@Preview(name = "Reports dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun ReportsDark() { WmTheme(darkTheme = true) { ReportsScreen() } }

@Preview(name = "Reports light", backgroundColor = 0xFFF5F4EF, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun ReportsLight() { WmTheme(darkTheme = false) { ReportsScreen() } }
