package com.sdremote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Root theme wrapper.
 *
 * Defaults to dark — the field-recording use case favours dark surfaces
 * regardless of system setting. Pass [followSystem] = true once the user
 * opts into automatic switching (Settings).
 *
 * Custom tokens are exposed via [LocalWmTokens]; M3 ColorScheme is wired
 * just enough for default M3 components to look right.
 */
@Composable
fun WmTheme(
    darkTheme: Boolean = true,
    followSystem: Boolean = false,
    content: @Composable () -> Unit,
) {
    val useDark = if (followSystem) isSystemInDarkTheme() else darkTheme
    val tokens = if (useDark) WmTokensDark else WmTokensLight

    val m3 = if (useDark) {
        darkColorScheme(
            primary = tokens.ink,
            onPrimary = tokens.bg,
            secondary = tokens.inkDim,
            onSecondary = tokens.bg,
            tertiary = tokens.rec,
            onTertiary = tokens.surface,
            background = tokens.bg,
            onBackground = tokens.ink,
            surface = tokens.surface,
            onSurface = tokens.ink,
            surfaceVariant = tokens.surfaceAlt,
            onSurfaceVariant = tokens.inkDim,
            outline = tokens.border,
            outlineVariant = tokens.borderSoft,
            error = tokens.clip,
            onError = tokens.surface,
        )
    } else {
        lightColorScheme(
            primary = tokens.ink,
            onPrimary = tokens.bg,
            secondary = tokens.inkDim,
            onSecondary = tokens.bg,
            tertiary = tokens.rec,
            onTertiary = tokens.surface,
            background = tokens.bg,
            onBackground = tokens.ink,
            surface = tokens.surface,
            onSurface = tokens.ink,
            surfaceVariant = tokens.surfaceAlt,
            onSurfaceVariant = tokens.inkDim,
            outline = tokens.border,
            outlineVariant = tokens.borderSoft,
            error = tokens.clip,
            onError = tokens.surface,
        )
    }

    val typography = WmTypographyDefault

    CompositionLocalProvider(
        LocalWmTokens provides tokens,
        LocalWmTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = m3,
            typography = buildM3Typography(typography),
            content = content,
        )
    }
}

/** Sugar to read tokens without spelling out `LocalWmTokens.current` everywhere. */
object Wm {
    val tokens
        @Composable get() = androidx.compose.runtime.remember { LocalWmTokens } .let { LocalWmTokens.current }

    val type
        @Composable get() = LocalWmTypography.current
}
