package com.just_for_fun.fileflip.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FileFlipTheme(
    content: @Composable () -> Unit
) {
    val appColors = ThemeManager.colors

    val colorScheme = darkColorScheme(
        primary = appColors.primaryBlue,
        secondary = ElectricPurple,
        tertiary = Pink80,
        background = appColors.background,
        surface = appColors.surface,
    )

    // Set status bar to match theme background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    FileFlipAppTheme {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}