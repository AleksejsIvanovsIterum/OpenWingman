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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdremote.domain.Session
import com.sdremote.feature.device.DeviceScreen
import com.sdremote.feature.reports.ReportsScreen
import com.sdremote.feature.scan.ScanScreen
import com.sdremote.feature.takes.TakeListScreen
import com.sdremote.feature.transport.TransportV6Screen
import com.sdremote.feature.transport.TransportViewModel
import com.sdremote.ui.frame.WmBottomNav
import com.sdremote.ui.frame.WmNavTab
import com.sdremote.ui.theme.Wm

/**
 * Root composable. Top-level switch between:
 *   - Permission prompt (if BLE permissions aren't yet granted)
 *   - ScanScreen   (no active session, permissions OK)
 *   - bottom-nav   (Transport / Takes / Reports / Device, session present)
 */
@Composable
fun OpenWingmanApp(
    connection: ConnectionViewModel = viewModel(),
) {
    val stage by connection.stage.collectAsStateWithLifecycle()
    val devices by connection.devices.collectAsStateWithLifecycle()
    val session by connection.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tokens = Wm.tokens

    // On first composition, check permissions and react.
    OneShotPermissionCheck { granted ->
        if (granted) connection.onPermissionsGranted() else connection.onPermissionsDenied()
    }
    val askForPermissions = rememberBlePermissionGate { granted ->
        if (granted) connection.onPermissionsGranted() else connection.onPermissionsDenied()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.bg)
            .statusBarsPadding(),
    ) {
        when (stage) {
            ConnectionViewModel.Stage.Connected -> ConnectedShell(
                session = session,
                modifier = Modifier.weight(1f),
            )
            else -> Box(modifier = Modifier.weight(1f)) {
                ScanScreen(
                    devices = devices,
                    scanning = stage == ConnectionViewModel.Stage.Scanning,
                    permissionMissing = stage == ConnectionViewModel.Stage.PermissionMissing,
                    onConnect = connection::connect,
                    onRescan = connection::startScan,
                    onRequestPermission = askForPermissions,
                )
            }
        }
    }
}

@Composable
private fun ConnectedShell(
    session: Session?,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(WmNavTab.Transport) }

    // Build the TransportViewModel with the live Session when available.
    val transportVm: TransportViewModel = viewModel(
        key = "transport-${session?.hashCode() ?: "mock"}",
        factory = remember(session) {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    TransportViewModel(session = session) as T
            }
        },
    )

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                WmNavTab.Transport -> TransportV6Screen(viewModel = transportVm)
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
