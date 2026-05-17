package com.sdremote.feature.device

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdremote.ui.components.WmHairline
import com.sdremote.ui.frame.WmTopBar
import com.sdremote.ui.icons.WmIcons
import com.sdremote.ui.theme.Wm
import com.sdremote.ui.theme.WmTheme

data class DeviceInfo(
    val productLine: String = "Sound Devices",
    val model: String = "833",
    val firmware: String = "FW 9.42",
    val serialNumber: String = "0X8331234",
    val wifiIp: String = "192.168.4.1",
    val storageUsed: String = "78.4 GB used",
    val storageFree: String = "252.6 GB free",
    val storageFillPct: Float = 0.24f,   // 0..1
)

data class SettingRow(val label: String, val value: String)

private val SampleSettings = listOf(
    SettingRow("Project", "A_Wedding_Day"),
    SettingRow("Sample Rate", "48 kHz / 24 bit"),
    SettingRow("Timecode", "29.97 ND · Free Run"),
    SettingRow("Record Mode", "BWF Poly · 12 ch"),
    SettingRow("Slate Mic", "Internal"),
    SettingRow("Wi-Fi", "Wingman-833"),
)

@Composable
fun DeviceScreen(
    info: DeviceInfo = DeviceInfo(),
    settings: List<SettingRow> = SampleSettings,
    online: Boolean = true,
    onSettingTap: (SettingRow) -> Unit = {},
    onDisconnect: () -> Unit = {},
) {
    val tokens = Wm.tokens
    Column(modifier = Modifier.fillMaxSize().background(tokens.bg)) {
        WmTopBar(
            title = "Device",
            subtitle = "Connected · ${info.model} Mixer-Recorder",
            rightSlot = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (online) tokens.ok else tokens.warn))
                    Text(
                        text = if (online) "ONLINE" else "OFFLINE",
                        color = if (online) tokens.ok else tokens.warn,
                        style = Wm.type.pill.copy(fontSize = 10.sp),
                    )
                }
            },
        )

        // Device card (inverted ink panel)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(tokens.ink)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = info.productLine.uppercase(),
                    color = tokens.bg.copy(alpha = 0.6f),
                    style = Wm.type.pill.copy(fontSize = 9.sp),
                )
                Text(
                    text = info.firmware,
                    color = tokens.bg.copy(alpha = 0.6f),
                    style = Wm.type.pill.copy(fontSize = 9.sp),
                )
            }
            Text(
                text = info.model,
                color = tokens.bg,
                style = Wm.type.sceneTake.copy(fontSize = 28.sp),
            )
            Text(
                text = "SN ${info.serialNumber} · Wi-Fi ${info.wifiIp}",
                color = tokens.bg.copy(alpha = 0.7f),
                style = Wm.type.noteBody.copy(fontSize = 12.sp),
            )

            // Storage bar
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "SD A · ${info.storageUsed}",
                        color = tokens.bg.copy(alpha = 0.7f),
                        style = Wm.type.dataValue.copy(fontSize = 10.sp),
                    )
                    Text(
                        text = info.storageFree,
                        color = tokens.bg.copy(alpha = 0.7f),
                        style = Wm.type.dataValue.copy(fontSize = 10.sp),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(4.dp)
                        .background(tokens.bg.copy(alpha = 0.15f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(info.storageFillPct.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(tokens.bg),
                    )
                }
            }
        }

        // Settings list
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
        ) {
            items(settings, key = { it.label }) { row ->
                SettingItem(row, onClick = { onSettingTap(row) })
                if (row != settings.last()) WmHairline()
            }
        }

        // Disconnect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, tokens.border, RectangleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(1.dp, tokens.clip, RectangleShape)
                    .clickable(onClick = onDisconnect),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "DISCONNECT",
                    color = tokens.clip,
                    style = Wm.type.dataValue.copy(fontSize = 11.sp),
                )
            }
        }
    }
}

@Composable
private fun SettingItem(row: SettingRow, onClick: () -> Unit) {
    val tokens = Wm.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = row.label,
            color = tokens.ink,
            style = Wm.type.trackName.copy(fontSize = 14.sp),
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = row.value,
                color = tokens.inkDim,
                style = Wm.type.dataValue.copy(fontSize = 12.sp),
            )
            Icon(
                imageVector = WmIcons.ChevronRight,
                contentDescription = null,
                tint = tokens.inkMute,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Preview(name = "Device dark", backgroundColor = 0xFF0C0C0D, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun DeviceDark() { WmTheme(darkTheme = true) { DeviceScreen() } }

@Preview(name = "Device light", backgroundColor = 0xFFF5F4EF, showBackground = true,
    widthDp = 393, heightDp = 808)
@Composable
private fun DeviceLight() { WmTheme(darkTheme = false) { DeviceScreen() } }
