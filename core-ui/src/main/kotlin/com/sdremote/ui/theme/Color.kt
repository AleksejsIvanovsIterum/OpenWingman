package com.sdremote.ui.theme

import androidx.compose.ui.graphics.Color

// Wingman design tokens — direct translation of WM_LIGHT / WM_DARK from JSX.
// Naming preserved 1:1 so design ↔ code reviews stay readable.

internal object WmLightColors {
    val bg          = Color(0xFFF5F4EF)
    val surface     = Color(0xFFFFFFFF)
    val surfaceAlt  = Color(0xFFEBE9E3)
    val divider     = Color(0xFF1A1A1A)
    val border      = Color(0xFFD9D6CF)
    val borderSoft  = Color(0xFFE7E4DD)
    val ink         = Color(0xFF0A0A0A)
    val inkDim      = Color(0xFF5B5852)
    val inkMute     = Color(0xFF8C8A83)
    val accent      = Color(0xFF0A0A0A)
    val rec         = Color(0xFFD92121)
    val recDim      = Color(0xFFFDE4E1)
    val ok          = Color(0xFF1F7A3A)
    val warn        = Color(0xFFC98412)
    val clip        = Color(0xFFD92121)
    val meterTrack  = Color(0xFFEBE9E3)
    val meterFill   = Color(0xFF0A0A0A)
    val meterFillSoft = Color(0xFF3A3A3A)
    val meterPeak   = Color(0xFFD92121)
    val meterHold   = Color(0xFF0A0A0A)
    val chipBg      = Color(0xFF0A0A0A)
    val chipInk     = Color(0xFFF5F4EF)
}

internal object WmDarkColors {
    val bg          = Color(0xFF0C0C0D)
    val surface     = Color(0xFF141416)
    val surfaceAlt  = Color(0xFF1C1C20)
    val divider     = Color(0xFFF2F2F0)
    val border      = Color(0xFF26262B)
    val borderSoft  = Color(0xFF1C1C20)
    val ink         = Color(0xFFF4F3EF)
    val inkDim      = Color(0xFFA8A6A0)
    val inkMute     = Color(0xFF6B6A66)
    val accent      = Color(0xFFF4F3EF)
    val rec         = Color(0xFFFF3B30)
    val recDim      = Color(0xFF2A0E0D)
    val ok          = Color(0xFF3DDC84)
    val warn        = Color(0xFFFFB020)
    val clip        = Color(0xFFFF3B30)
    val meterTrack  = Color(0xFF1C1C20)
    val meterFill   = Color(0xFFF4F3EF)
    val meterFillSoft = Color(0xFFA8A6A0)
    val meterPeak   = Color(0xFFFF3B30)
    val meterHold   = Color(0xFFF4F3EF)
    val chipBg      = Color(0xFFF4F3EF)
    val chipInk     = Color(0xFF0C0C0D)
}
