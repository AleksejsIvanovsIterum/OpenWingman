package com.sdremote.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Mirror of the JSX WM_LIGHT / WM_DARK token shapes.
// Compose Material 3 ColorScheme doesn't have slots for meter / chip / ink-dim
// concepts, so we expose them via this dedicated token object instead of
// shoehorning them into M3 slots.
@Immutable
data class WmTokens(
    val name: String,
    val bg: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val divider: Color,
    val border: Color,
    val borderSoft: Color,
    val ink: Color,
    val inkDim: Color,
    val inkMute: Color,
    val accent: Color,
    val rec: Color,
    val recDim: Color,
    val ok: Color,
    val warn: Color,
    val clip: Color,
    val meterTrack: Color,
    val meterFill: Color,
    val meterFillSoft: Color,
    val meterPeak: Color,
    val meterHold: Color,
    val chipBg: Color,
    val chipInk: Color,
) {
    val isDark: Boolean get() = name == "dark"
}

val WmTokensLight = WmTokens(
    name = "light",
    bg = WmLightColors.bg,
    surface = WmLightColors.surface,
    surfaceAlt = WmLightColors.surfaceAlt,
    divider = WmLightColors.divider,
    border = WmLightColors.border,
    borderSoft = WmLightColors.borderSoft,
    ink = WmLightColors.ink,
    inkDim = WmLightColors.inkDim,
    inkMute = WmLightColors.inkMute,
    accent = WmLightColors.accent,
    rec = WmLightColors.rec,
    recDim = WmLightColors.recDim,
    ok = WmLightColors.ok,
    warn = WmLightColors.warn,
    clip = WmLightColors.clip,
    meterTrack = WmLightColors.meterTrack,
    meterFill = WmLightColors.meterFill,
    meterFillSoft = WmLightColors.meterFillSoft,
    meterPeak = WmLightColors.meterPeak,
    meterHold = WmLightColors.meterHold,
    chipBg = WmLightColors.chipBg,
    chipInk = WmLightColors.chipInk,
)

val WmTokensDark = WmTokens(
    name = "dark",
    bg = WmDarkColors.bg,
    surface = WmDarkColors.surface,
    surfaceAlt = WmDarkColors.surfaceAlt,
    divider = WmDarkColors.divider,
    border = WmDarkColors.border,
    borderSoft = WmDarkColors.borderSoft,
    ink = WmDarkColors.ink,
    inkDim = WmDarkColors.inkDim,
    inkMute = WmDarkColors.inkMute,
    accent = WmDarkColors.accent,
    rec = WmDarkColors.rec,
    recDim = WmDarkColors.recDim,
    ok = WmDarkColors.ok,
    warn = WmDarkColors.warn,
    clip = WmDarkColors.clip,
    meterTrack = WmDarkColors.meterTrack,
    meterFill = WmDarkColors.meterFill,
    meterFillSoft = WmDarkColors.meterFillSoft,
    meterPeak = WmDarkColors.meterPeak,
    meterHold = WmDarkColors.meterHold,
    chipBg = WmDarkColors.chipBg,
    chipInk = WmDarkColors.chipInk,
)

val LocalWmTokens = staticCompositionLocalOf { WmTokensDark }
