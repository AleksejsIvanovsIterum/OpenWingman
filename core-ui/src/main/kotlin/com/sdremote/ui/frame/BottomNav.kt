package com.sdremote.ui.frame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/** App-level navigation destinations. */
enum class WmNavTab(val label: String, val icon: ImageVector) {
    Transport("Transport", WmIcons.Record),
    Takes("Takes", WmIcons.List),
    Reports("Reports", WmIcons.Report),
    Device("Device", WmIcons.Settings),
}

/**
 * Bottom navigation — JSX `WMBottomNav`.
 *
 * Active tab gets a 28×2 dp indicator line at the top + full-color icon/label,
 * inactive tabs are inkMute. Border on top edge as in design.
 */
@Composable
fun WmBottomNav(
    active: WmNavTab,
    onSelect: (WmNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = Wm.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(tokens.bg)
            .border(1.dp, tokens.border, shape = RectangleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (tab in WmNavTab.entries) {
            NavItem(
                tab = tab,
                isActive = tab == active,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: WmNavTab,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = Wm.tokens
    val color = if (isActive) tokens.ink else tokens.inkMute
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.clickable(
            onClick = onClick,
            interactionSource = interaction,
            indication = null,
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (isActive) {
            // 28×2dp top indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 28.dp, height = 2.dp)
                    .background(tokens.ink),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = tab.label.uppercase(),
                color = color,
                style = Wm.type.pill.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
        }
    }
}

@Preview(name = "BottomNav dark", backgroundColor = 0xFF0C0C0D, showBackground = true, widthDp = 393)
@Composable
private fun BottomNavDark() {
    WmTheme(darkTheme = true) { WmBottomNav(active = WmNavTab.Transport, onSelect = {}) }
}

@Preview(name = "BottomNav light", backgroundColor = 0xFFF5F4EF, showBackground = true, widthDp = 393)
@Composable
private fun BottomNavLight() {
    WmTheme(darkTheme = false) { WmBottomNav(active = WmNavTab.Takes, onSelect = {}) }
}
