package com.sdremote.feature.scan

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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.transport.ScanResult
import com.sdremote.ui.components.PillKind
import com.sdremote.ui.components.WmHairline
import com.sdremote.ui.components.WmLabel
import com.sdremote.ui.components.WmPill
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

/**
 * Lightweight scan / device picker shown when no session is connected.
 *
 * The screen does not own the scan — it consumes a list of [ScanResult]
 * passed in from a ViewModel that owns the actual Flow subscription.
 */
@Composable
fun ScanScreen(
    devices: List<ScanResult>,
    scanning: Boolean,
    permissionMissing: Boolean = false,
    onConnect: (ScanResult) -> Unit,
    onRescan: () -> Unit = {},
    onRequestPermission: () -> Unit = {},
) {
    val tokens = Wm.tokens
    Column(modifier = Modifier.fillMaxSize().background(tokens.bg)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RectangleShape)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                WmLabel(text = "Bluetooth")
                Text(
                    text = "Choose a Recorder",
                    color = tokens.ink,
                    style = Wm.type.tcLarge.copy(fontSize = 22.sp),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            WmPill(
                text = if (scanning) "SCANNING" else "IDLE",
                kind = if (scanning) PillKind.Default else PillKind.Outline,
            )
        }

        when {
            permissionMissing -> EmptyState(
                title = "Bluetooth permission required",
                subtitle = "Tap to grant permission.",
                icon = WmIcons.Lock,
                onClick = onRequestPermission,
            )
            devices.isEmpty() && scanning -> EmptyState(
                title = "Scanning…",
                subtitle = "Wake your recorder and bring it close.",
                icon = WmIcons.Bluetooth,
            )
            devices.isEmpty() -> EmptyState(
                title = "No recorders nearby",
                subtitle = "Tap to scan again.",
                icon = WmIcons.Bluetooth,
                onClick = onRescan,
            )
            else -> LazyColumn(modifier = Modifier.weight(1f)) {
                items(devices, key = { it.address }) { d ->
                    DeviceRow(d, onClick = { onConnect(d) })
                    WmHairline()
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(d: ScanResult, onClick: () -> Unit) {
    val tokens = Wm.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = WmIcons.Bluetooth,
            contentDescription = null,
            tint = tokens.ink,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = d.displayName,
                color = tokens.ink,
                style = Wm.type.trackName.copy(fontSize = 15.sp),
            )
            val subtitle = buildString {
                d.firmwareMajor?.let { major -> append("FW $major.${d.firmwareMinor ?: 0}") }
                if (isNotEmpty()) append(" · ")
                append("${d.rssi} dBm")
            }
            Text(
                text = subtitle,
                color = tokens.inkDim,
                style = Wm.type.noteBody.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Icon(
            imageVector = WmIcons.ChevronRight,
            contentDescription = null,
            tint = tokens.inkMute,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
) {
    val tokens = Wm.tokens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.inkDim,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = title,
                color = tokens.ink,
                style = Wm.type.sceneTake.copy(fontSize = 20.sp),
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = subtitle,
                color = tokens.inkDim,
                style = Wm.type.noteBody.copy(fontSize = 13.sp),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Preview(name = "Scan with devices dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun ScanWithDevicesDark() {
    WmTheme(darkTheme = true) {
        ScanScreen(
            scanning = true,
            devices = listOf(
                ScanResult("AA:11:22:33:44:55", "MixPre-833", -52,
                    com.sdremote.protocol.types.ProductId.S833, 9, 42, "0X8331234"),
                ScanResult("BB:22:33:44:55:66", "MixPre-6 II", -71,
                    com.sdremote.protocol.types.ProductId.MixPre6II, 8, 21, "MP6234567"),
            ),
            onConnect = {},
        )
    }
}

@Preview(name = "Scan empty dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun ScanEmptyDark() {
    WmTheme(darkTheme = true) {
        ScanScreen(scanning = false, devices = emptyList(), onConnect = {})
    }
}
