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
import com.sdremote.feature.device.DeviceScreen
import com.sdremote.feature.reports.ReportsScreen
import com.sdremote.feature.takes.TakeListScreen
import com.sdremote.feature.transport.TransportV6Screen
import com.sdremote.ui.frame.WmBottomNav
import com.sdremote.ui.frame.WmNavTab
import com.sdremote.ui.theme.Wm

/**
 * Root composable. Single bottom-nav driven shell.
 *
 * Real Navigation Compose (with deep links and back-stack) goes in once we
 * have more than placeholders. For now state is a single enum.
 */
@Composable
fun OpenWingmanApp() {
    var tab by rememberSaveable { mutableStateOf(WmNavTab.Transport) }
    val tokens = Wm.tokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.bg)
            .statusBarsPadding(),
    ) {
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

