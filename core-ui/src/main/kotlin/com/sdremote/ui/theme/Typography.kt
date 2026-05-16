package com.sdremote.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sdremote.ui.R

// Two type families, matching JSX intent:
// - Mono: all data (timecode, dB, scene·take, channel IDs, labels)
// - Sans: user content (track names, notes)
//
// Font files must be in core-ui/src/main/res/font/ with these snake_case names:
//   inter_variable.ttf, jetbrains_mono_variable.ttf
// (Variable fonts; we use weight axis at the call site.)

val WmFontMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Normal),
    Font(R.font.jetbrains_mono_variable, FontWeight.Medium),
    Font(R.font.jetbrains_mono_variable, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_variable, FontWeight.Bold),
)

val WmFontSans = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold),
)

/**
 * Type slots used across the design.
 * Naming matches the visual purpose in mockups rather than M3 type scale.
 */
@Immutable
data class WmTypography(
    /** 50–64 sp tabular-nums monospace, for the running timecode "monolith". */
    val tcMonolith: TextStyle,
    /** 22–40 sp monospace, top-line timecode in compact layouts. */
    val tcLarge: TextStyle,
    /** Scene / Take big number (e.g. "14A / 07"). */
    val sceneTake: TextStyle,
    /** Inline data values (channel dB, durations, filenames). */
    val dataValue: TextStyle,
    /** Tiny mono uppercase caption above data — WMLabel equivalent. */
    val captionLabel: TextStyle,
    /** Pill text — bold mono, all-caps, tight tracking. */
    val pill: TextStyle,
    /** Section divider label. */
    val dividerLabel: TextStyle,
    /** Track name (user content). */
    val trackName: TextStyle,
    /** Take note (user content). */
    val noteBody: TextStyle,
    /** Channel number badge (e.g. "01" under each meter). */
    val channelBadge: TextStyle,
)

val WmTypographyDefault = WmTypography(
    tcMonolith = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 50.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.04).em,
        lineHeight = 50.sp,
    ),
    tcLarge = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.02).em,
        lineHeight = 30.sp,
    ),
    sceneTake = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.02).em,
        lineHeight = 28.sp,
    ),
    dataValue = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 14.sp,
    ),
    captionLabel = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.12.em,
        lineHeight = 12.sp,
    ),
    pill = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.1.em,
        lineHeight = 9.sp,
    ),
    dividerLabel = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.14.em,
        lineHeight = 10.sp,
    ),
    trackName = TextStyle(
        fontFamily = WmFontSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 18.sp,
    ),
    noteBody = TextStyle(
        fontFamily = WmFontSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    ),
    channelBadge = TextStyle(
        fontFamily = WmFontMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 12.sp,
        textAlign = TextAlign.Center,
    ),
)

val LocalWmTypography = staticCompositionLocalOf { WmTypographyDefault }

/**
 * Material 3 Typography mapped to our type slots, so any default M3 component
 * (e.g. NavigationBar labels, Text without explicit style) picks up the right
 * font. Custom slots are read directly from [LocalWmTypography].
 */
internal fun buildM3Typography(wm: WmTypography) = Typography(
    displayLarge = wm.tcMonolith,
    displayMedium = wm.tcLarge,
    titleLarge = wm.sceneTake,
    titleMedium = wm.sceneTake.copy(fontSize = 18.sp),
    titleSmall = wm.dataValue,
    bodyLarge = wm.trackName,
    bodyMedium = wm.noteBody,
    bodySmall = wm.captionLabel,
    labelLarge = wm.pill,
    labelMedium = wm.captionLabel,
    labelSmall = wm.pill.copy(fontSize = 8.sp),
)
