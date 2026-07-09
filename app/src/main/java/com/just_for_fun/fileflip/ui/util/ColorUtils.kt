package com.just_for_fun.fileflip.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.just_for_fun.fileflip.ui.theme.LocalAppColors

val PrimaryBlue: Color
    @Composable
    get() = LocalAppColors.current.primaryBlue

val BackgroundDark: Color
    @Composable
    get() = LocalAppColors.current.background

val SurfaceDark: Color
    @Composable
    get() = LocalAppColors.current.surface

val GutterColor: Color
    @Composable
    get() = LocalAppColors.current.gutter

val TextWhite: Color
    @Composable
    get() = LocalAppColors.current.textPrimary

val TextGray: Color
    @Composable
    get() = LocalAppColors.current.textSecondary

val DividerColor: Color
    @Composable
    get() = LocalAppColors.current.divider

val CardBackground: Color
    @Composable
    get() = LocalAppColors.current.surface
