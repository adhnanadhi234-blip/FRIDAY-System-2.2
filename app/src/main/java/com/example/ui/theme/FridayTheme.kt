package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.FridayThemeId

@Immutable
data class FridayHudColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val border: Color,
    val background: Color,
    val panelBg: Color,
    val textPrimary: Color = HudTextPrimary,
    val textSecondary: Color = HudTextSecondary,
    val textMuted: Color = HudTextMuted,
    val warning: Color = HudWarning,
    val success: Color = HudSuccess
)

val LocalFridayColors = staticCompositionLocalOf {
    FridayHudColors(
        primary = CyanPrimary,
        secondary = CyanSecondary,
        accent = CyanAccent,
        border = CyanBorder,
        background = CyanBackground,
        panelBg = PanelDark
    )
}

fun getFridayHudColors(themeId: FridayThemeId): FridayHudColors {
    return when (themeId) {
        FridayThemeId.CYAN -> FridayHudColors(
            primary = CyanPrimary,
            secondary = CyanSecondary,
            accent = CyanAccent,
            border = CyanBorder,
            background = CyanBackground,
            panelBg = PanelDark
        )
        FridayThemeId.CRIMSON_GOLD -> FridayHudColors(
            primary = GoldPrimary,
            secondary = CrimsonPrimary,
            accent = GoldAccent,
            border = CrimsonBorder,
            background = CrimsonBackground,
            panelBg = Color(0xCC200609)
        )
        FridayThemeId.EMERALD -> FridayHudColors(
            primary = EmeraldPrimary,
            secondary = EmeraldSecondary,
            accent = EmeraldAccent,
            border = EmeraldBorder,
            background = EmeraldBackground,
            panelBg = Color(0xCC051810)
        )
        FridayThemeId.AMBER -> FridayHudColors(
            primary = AmberPrimary,
            secondary = AmberSecondary,
            accent = AmberAccent,
            border = AmberBorder,
            background = AmberBackground,
            panelBg = Color(0xCC1F1105)
        )
        FridayThemeId.VIOLET -> FridayHudColors(
            primary = VioletPrimary,
            secondary = VioletSecondary,
            accent = VioletAccent,
            border = VioletBorder,
            background = VioletBackground,
            panelBg = Color(0xCC170624)
        )
    }
}

@Composable
fun FridaySystemTheme(
    themeId: FridayThemeId = FridayThemeId.CYAN,
    content: @Composable () -> Unit
) {
    val hudColors = getFridayHudColors(themeId)
    val colorScheme = darkColorScheme(
        primary = hudColors.primary,
        secondary = hudColors.secondary,
        tertiary = hudColors.accent,
        background = hudColors.background,
        surface = hudColors.panelBg,
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = HudTextPrimary,
        onSurface = HudTextPrimary
    )

    CompositionLocalProvider(LocalFridayColors provides hudColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
