package com.sdremote.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdremote.feature.device.DeviceScreen
import com.sdremote.feature.reports.ReportsScreen
import com.sdremote.feature.scan.ScanScreen
import com.sdremote.feature.takes.TakeListScreen
import com.sdremote.feature.transport.TransportV6Screen
import com.sdremote.ui.frame.WmBottomNav
import com.sdremote.ui.frame.WmNavTab
import com.sdremote.ui.theme.Wm

/**
 * Root composable. Top-level switch between:
 *   - ScanScreen (no active session)
 *   - bottom-nav app (Transport / Takes / Reports / Device, with a session)
 */
@Composable
fun OpenWingmanApp(
    connection: ConnectionViewModel = viewModel(),
) {
    val stage by connection.stage.collectAsStateWithLifecycle()
    val devices by connection.devices.collectAsStateWithLifecycle()
    val tokens = Wm.tokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.bg)
            .statusBarsPadding(),
    ) {
        when (stage) {
            ConnectionViewModel.Stage.Connected -> ConnectedShell(
                modifier = Modifier.weight(1f),
            )
            else -> Box(modifier = Modifier.weight(1f)) {
                ScanScreen(
                    devices = devices,
                    scanning = stage == ConnectionViewModel.Stage.Scanning,
                    onConnect = connection::connect,
                    onRescan = connection::startScan,
                )
            }
        }
    }
}

@Composable
private fun ConnectedShell(modifier: Modifier = Modifier) {
    var tab by rememberSaveable { mutableStateOf(WmNavTab.Transport) }
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                WmNavTab.Transport -> TransportV6Screen()
                WmNavTab.Takes -> TakeListScreen()
                WmNavTab.Reports -> ReportsScreen()
                WmNavTab.Device -> DeviceScreen()
            }
        }
        WmBottomNav(
            active = tab,
            onSelect = { tab = it },
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}
