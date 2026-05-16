package com.sdremote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sdremote.ui.theme.WmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Dark default, system-following toggle exposed via Settings later.
            WmTheme(darkTheme = true, followSystem = false) {
                OpenWingmanApp()
            }
        }
    }
}
