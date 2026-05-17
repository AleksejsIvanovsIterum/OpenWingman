package com.sdremote.feature.takes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.ui.components.PillKind
import com.sdremote.ui.components.WmHairline
import com.sdremote.ui.components.WmLabel
import com.sdremote.ui.components.WmPill
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/** Action-button state on the editor. */
data class EditorActions(
    val circle: Boolean = false,
    val falseTake: Boolean = false,
    val wild: Boolean = false,
)

/** Channel name as edited on this screen. */
data class TrackName(val channel: Int, val name: String, val unused: Boolean = false)

private val SampleTracks = listOf(
    TrackName(1, "Boom"),
    TrackName(2, "Alan"),
    TrackName(3, "Carla"),
    TrackName(4, "Denise"),
    TrackName(5, "Unused", unused = true),
)

@Composable
fun SceneTakeEditorScreen(
    scene: String = "14A",
    take: Int = 7,
    actions: EditorActions = EditorActions(circle = true),
    notes: String = "Pickup on door open — Carla's mic was reading a bit hot on the slam. Ask AD for one more for safety.",
    tracks: List<TrackName> = SampleTracks,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSave: () -> Unit = {},
    onActionToggle: (action: EditorAction) -> Unit = {},
    onTrackEdit: (TrackName) -> Unit = {},
) {
    val tokens = Wm.tokens

    Column(modifier = Modifier.fillMaxSize().background(tokens.bg)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RectangleShape)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, tokens.border, RectangleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = WmIcons.ChevronRight,
                        contentDescription = "Back",
                        tint = tokens.ink,
                        modifier = Modifier.size(14.dp).rotate(180f),
                    )
                }
                WmLabel(text = "Edit Take · $scene / ${take.toString().padStart(2, '0')}")
            }
            WmPill(text = "UNSAVED", kind = PillKind.Outline)
        }

        // Scene / Take inputs
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EditField(
                label = "Scene",
                value = scene,
                focused = true,
                modifier = Modifier.weight(1f),
            )
            EditField(
                label = "Take",
                value = take.toString().padStart(2, '0'),
                focused = false,
                modifier = Modifier.weight(1f),
            )
        }

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ActionButton("Circle", WmIcons.Circle, actions.circle,
                modifier = Modifier.weight(1f).height(44.dp),
                onClick = { onActionToggle(EditorAction.Circle) })
            ActionButton("False", WmIcons.FalseX, actions.falseTake,
                modifier = Modifier.weight(1f).height(44.dp),
                onClick = { onActionToggle(EditorAction.False) })
            ActionButton("Wild", WmIcons.Mic, actions.wild,
                modifier = Modifier.weight(1f).height(44.dp),
                onClick = { onActionToggle(EditorAction.Wild) })
        }

        // Notes
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            WmLabel(text = "Notes")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(tokens.surface)
                    .border(1.dp, tokens.border, RectangleShape)
                    .padding(12.dp),
            ) {
                Text(
                    text = notes,
                    color = tokens.ink,
                    style = Wm.type.noteBody.copy(fontSize = 13.sp, lineHeight = 19.sp),
                )
            }
        }

        // Track names list
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).weight(1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WmLabel(text = "Track Names")
                WmLabel(text = "Tap to edit")
            }
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .border(1.dp, tokens.borderSoft, RectangleShape),
            ) {
                LazyColumn {
                    items(tracks, key = { it.channel }) { tr ->
                        TrackRow(tr, onClick = { onTrackEdit(tr) })
                        if (tr != tracks.last()) WmHairline()
                    }
                }
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RectangleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.weight(1f).height(44.dp)
                    .border(1.dp, tokens.border, RectangleShape)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "CANCEL", color = tokens.inkDim,
                    style = Wm.type.dataValue.copy(fontSize = 11.sp))
            }
            Box(
                modifier = Modifier.weight(2f).height(44.dp)
                    .background(tokens.ink)
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "SAVE & APPLY", color = tokens.bg,
                    style = Wm.type.dataValue.copy(fontSize = 11.sp))
            }
        }
    }
}

enum class EditorAction { Circle, False, Wild }

@Composable
private fun EditField(
    label: String,
    value: String,
    focused: Boolean,
    modifier: Modifier = Modifier,
) {
    val tokens = Wm.tokens
    Column(modifier = modifier) {
        WmLabel(text = label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .border(
                    if (focused) 1.5.dp else 1.dp,
                    if (focused) tokens.ink else tokens.border,
                    RectangleShape,
                )
                .padding(horizontal = 10.dp, vertical = 12.dp),
        ) {
            Text(
                text = value,
                color = tokens.ink,
                style = Wm.type.sceneTake.copy(fontSize = 28.sp),
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = Wm.tokens
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .background(if (active) tokens.ink else Color.Transparent)
            .let { if (!active) it.border(1.dp, tokens.border, RectangleShape) else it }
            .clickable(onClick = onClick, interactionSource = interaction, indication = null),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) tokens.bg else tokens.ink,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label.uppercase(),
                color = if (active) tokens.bg else tokens.ink,
                style = Wm.type.dataValue.copy(fontSize = 11.sp),
            )
        }
    }
}

@Composable
private fun TrackRow(tr: TrackName, onClick: () -> Unit) {
    val tokens = Wm.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "CH ${tr.channel.toString().padStart(2, '0')}",
            color = tokens.inkMute,
            style = Wm.type.pill.copy(fontSize = 10.sp),
            modifier = Modifier.width(46.dp),
        )
        Text(
            text = if (tr.unused) "— unused —" else tr.name,
            color = if (tr.unused) tokens.inkMute else tokens.ink,
            style = Wm.type.trackName.copy(fontSize = 13.sp),
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = WmIcons.Edit,
            contentDescription = "Edit",
            tint = tokens.inkMute,
            modifier = Modifier.size(13.dp),
        )
    }
}

@Preview(name = "Editor dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun EditorDark() { WmTheme(darkTheme = true) { SceneTakeEditorScreen() } }

@Preview(name = "Editor light", backgroundColor = 0xFFF5F4EF, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun EditorLight() { WmTheme(darkTheme = false) { SceneTakeEditorScreen() } }
