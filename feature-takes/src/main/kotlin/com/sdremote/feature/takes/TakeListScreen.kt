package com.sdremote.feature.takes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.ui.components.WmHairline
import com.sdremote.ui.frame.WmTopBar
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

@Composable
fun TakeListScreen(
    takes: List<TakeRow> = SampleTakes,
    projectName: String = "A_Wedding_Day",
    totalCount: Int = 142,
    onSearch: () -> Unit = {},
    onAdd: () -> Unit = {},
    onSelect: (TakeRow) -> Unit = {},
) {
    val tokens = Wm.tokens
    var filter by remember { mutableStateOf(TakeFilter.All) }

    Column(modifier = Modifier.fillMaxSize().background(tokens.bg)) {
        WmTopBar(
            title = "Take List",
            subtitle = "$projectName · $totalCount takes",
            rightSlot = {
                IconBox(onClick = onSearch, modifier = Modifier.size(36.dp)) {
                    Icon(WmIcons.Search, contentDescription = "Search",
                        tint = tokens.ink, modifier = Modifier.size(16.dp))
                }
                Box(modifier = Modifier.size(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(tokens.ink)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(WmIcons.Plus, contentDescription = "Add",
                        tint = tokens.bg, modifier = Modifier.size(16.dp))
                }
            },
        )

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().background(tokens.bg),
        ) {
            items(TakeFilter.entries.toList()) { f ->
                FilterChip(label = f.label, selected = f == filter, onClick = { filter = f })
            }
        }
        WmHairline()

        // List
        LazyColumn(modifier = Modifier.weight(1f)) {
            val visible = takes.filter { row ->
                when (filter) {
                    TakeFilter.All     -> true
                    TakeFilter.Circled -> row.circled
                    TakeFilter.False   -> row.falseTake
                    TakeFilter.NoNote  -> row.note.isBlank()
                    TakeFilter.Scene14A -> row.scene == "14A"
                    TakeFilter.Scene13C -> row.scene == "13C"
                }
            }
            items(visible, key = { it.handle }) { row ->
                TakeRowView(row, onClick = { onSelect(row) })
                WmHairline()
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val tokens = Wm.tokens
    Box(
        modifier = Modifier
            .clip(RectangleShape)
            .background(if (selected) tokens.ink else Color.Transparent)
            .let { if (!selected) it.border(1.dp, tokens.border, RectangleShape) else it }
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = if (selected) tokens.bg else tokens.inkDim,
            style = Wm.type.pill,
        )
    }
}

@Composable
private fun TakeRowView(row: TakeRow, onClick: () -> Unit) {
    val tokens = Wm.tokens
    val rowBg = if (row.circled) tokens.surfaceAlt else Color.Transparent
    val rowAlpha = if (row.falseTake) 0.55f else 1f
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onClick, interactionSource = interaction, indication = null)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Take badge — circle if circled, square otherwise, struck-through if false
        val badgeShape = if (row.circled) CircleShape else RectangleShape
        val badgeBorder = when {
            row.circled   -> tokens.ink
            row.falseTake -> tokens.clip
            else          -> tokens.border
        }
        val badgeStroke = if (row.circled || row.falseTake) 2.dp else 1.dp
        val badgeFg = if (row.falseTake) tokens.clip else tokens.ink
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(badgeShape)
                .border(badgeStroke, badgeBorder, badgeShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = row.take.toString(),
                color = badgeFg,
                style = Wm.type.pill.copy(
                    fontSize = 11.sp,
                    textDecoration = if (row.falseTake) TextDecoration.LineThrough else null,
                ),
            )
        }

        // Middle: scene/take + status pills, with note below
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${row.scene} / ${row.take.toString().padStart(2, '0')}",
                    color = tokens.ink,
                    style = Wm.type.dataValue.copy(fontSize = 14.sp),
                )
                if (row.circled) {
                    Text(
                        text = "  CIRCLED",
                        color = tokens.ok,
                        style = Wm.type.pill.copy(fontSize = 9.sp),
                    )
                }
                if (row.falseTake) {
                    Text(
                        text = "  FALSE",
                        color = tokens.clip,
                        style = Wm.type.pill.copy(fontSize = 9.sp),
                    )
                }
            }
            if (row.note.isNotBlank()) {
                Text(
                    text = row.note,
                    color = tokens.inkDim,
                    style = Wm.type.noteBody.copy(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // Right: duration + time
        Column(horizontalAlignment = Alignment.End) {
            Text(text = row.durationStr, color = tokens.inkDim,
                style = Wm.type.dataValue.copy(fontSize = 11.sp))
            Text(text = row.timeStr, color = tokens.inkMute,
                style = Wm.type.dataValue.copy(fontSize = 11.sp),
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun IconBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = Wm.tokens
    Box(
        modifier = modifier
            .border(1.dp, tokens.border, RectangleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Preview(name = "TakeList dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun TakeListDark() { WmTheme(darkTheme = true) { TakeListScreen() } }

@Preview(name = "TakeList light", backgroundColor = 0xFFF5F4EF, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun TakeListLight() { WmTheme(darkTheme = false) { TakeListScreen() } }
