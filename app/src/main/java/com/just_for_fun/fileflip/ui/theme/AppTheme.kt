package com.just_for_fun.fileflip.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * FileFlip App Color Palette.
 * Contains all colors used across the app for both theme variants.
 */
data class AppColorScheme(
    val primaryBlue: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val gutter: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val iconOrange: Color,
    val iconEmerald: Color,
    val searchHighlight: Color,
    val replaceHighlightPending: Color,
    val replaceHighlightDone: Color,
)

// --- Dark Theme (Default - Teal/Oceanic) ---
val DarkColors = AppColorScheme(
    primaryBlue = Color(0xFF0DA6F2),
    background = Color(0xFF101C22),
    surface = Color(0xFF1A2830),
    surfaceVariant = Color(0xFF152329),
    gutter = Color(0xFF152329),
    border = Color(0xFF2D3748),
    divider = Color(0xFF0DA6F2).copy(alpha = 0.1f),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    iconOrange = Color(0xFFFF9F1C),
    iconEmerald = Color(0xFF10B981),
    searchHighlight = Color(0xFFFFD600).copy(alpha = 0.35f),
    replaceHighlightPending = Color(0xFFF44336).copy(alpha = 0.35f),
    replaceHighlightDone = Color(0xFF4CAF50).copy(alpha = 0.35f),
)

// --- GitHub Dark Gray Theme ---
val GitHubDarkColors = AppColorScheme(
    primaryBlue = Color(0xFF58A6FF),
    background = Color(0xFF0D1117),
    surface = Color(0xFF161B22),
    surfaceVariant = Color(0xFF21262D),
    gutter = Color(0xFF161B22),
    border = Color(0xFF30363D),
    divider = Color(0xFF21262D),
    textPrimary = Color(0xFFC9D1D9),
    textSecondary = Color(0xFF8B949E),
    iconOrange = Color(0xFFD29922),
    iconEmerald = Color(0xFF3FB950),
    searchHighlight = Color(0xFFE3B341).copy(alpha = 0.35f),
    replaceHighlightPending = Color(0xFFF85149).copy(alpha = 0.35f),
    replaceHighlightDone = Color(0xFF3FB950).copy(alpha = 0.35f),
)

/**
 * Singleton to hold the current theme. Persisted via SharedPreferences.
 * 0 = Dark (Oceanic), 1 = GitHub Dark Gray
 */
object ThemeManager {
    var currentThemeIndex by mutableStateOf(0)
        private set

    val colors: AppColorScheme
        get() = if (currentThemeIndex == 0) DarkColors else GitHubDarkColors

    fun setTheme(index: Int, context: Context? = null) {
        currentThemeIndex = index.coerceIn(0, 1)
        context?.let {
            it.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("selected_theme", currentThemeIndex)
                .apply()
        }
    }

    fun loadTheme(context: Context) {
        currentThemeIndex = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getInt("selected_theme", 0)
    }
}

/** CompositionLocal so any composable can access the current colors. */
val LocalAppColors = compositionLocalOf { DarkColors }

@Composable
fun FileFlipAppTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppColors provides ThemeManager.colors) {
        content()
    }
}
