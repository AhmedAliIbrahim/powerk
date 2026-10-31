package com.powerk.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared dashboard palette. Keep both themes and UI_GUIDELINES.md in sync. */
object PowerkColors {
    val LightBackground = Color(0xFFF6F7F9)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSubtle = Color(0xFFF1F3F6)
    val LightInk = Color(0xFF17202E)
    val LightMuted = Color(0xFF566276)
    val LightLine = Color(0xFFD6DBE3)
    val LightBorder = Color(0xFF788496)
    val LightAccent = Color(0xFF2457D6)
    val LightAccentSoft = Color(0xFFEDF2FF)
    val LightAccentInk = Color(0xFF2448A5)
    val LightDanger = Color(0xFFB42318)
    val LightDangerBackground = Color(0xFFFFF1F0)
    val DarkBackground = Color(0xFF111318)
    val DarkSurface = Color(0xFF191D25)
    val DarkSubtle = Color(0xFF202631)
    val DarkInk = Color(0xFFEDF1F7)
    val DarkMuted = Color(0xFFA9B4C4)
    val DarkLine = Color(0xFF343C4A)
    val DarkBorder = Color(0xFF8190A6)
    val DarkAccent = Color(0xFF8AACFF)
    val DarkAccentSoft = Color(0xFF202E4F)
    val DarkAccentInk = Color(0xFFBED0FF)
    val DarkDanger = Color(0xFFFFB4AC)
    val DarkDangerBackground = Color(0xFF3B2224)
}

/** dp counterparts of the web spacing scale; native targets are at least 48 dp. */
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val section = 32.dp
    val target = 48.dp
}

object DashboardLayout {
    val compactWidth = 380.dp
    val outletPairWidth = 256.dp
    val outletFourWidth = 640.dp
}

private val LightScheme = with(PowerkColors) { lightColorScheme(
    primary = LightAccent, onPrimary = LightSurface,
    primaryContainer = LightAccentSoft, onPrimaryContainer = LightAccentInk,
    secondary = LightMuted, onSecondary = LightSurface,
    secondaryContainer = LightSubtle, onSecondaryContainer = LightInk,
    tertiary = LightAccent, onTertiary = LightSurface,
    tertiaryContainer = LightAccentSoft, onTertiaryContainer = LightAccentInk,
    background = LightBackground, onBackground = LightInk,
    surface = LightSurface, onSurface = LightInk,
    surfaceVariant = LightSubtle, onSurfaceVariant = LightMuted,
    surfaceContainerLowest = LightSurface, surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface, surfaceContainerHigh = LightSubtle,
    surfaceContainerHighest = LightSubtle, surfaceTint = LightSurface,
    outline = LightBorder, outlineVariant = LightLine,
    error = LightDanger, onError = LightSurface,
    errorContainer = LightDangerBackground, onErrorContainer = LightDanger,
    inverseSurface = DarkSurface, inverseOnSurface = DarkInk, inversePrimary = DarkAccent,
) }
private val DarkScheme = with(PowerkColors) { darkColorScheme(
    primary = DarkAccent, onPrimary = DarkBackground,
    primaryContainer = DarkAccentSoft, onPrimaryContainer = DarkAccentInk,
    secondary = DarkMuted, onSecondary = DarkBackground,
    secondaryContainer = DarkSubtle, onSecondaryContainer = DarkInk,
    tertiary = DarkAccent, onTertiary = DarkBackground,
    tertiaryContainer = DarkAccentSoft, onTertiaryContainer = DarkAccentInk,
    background = DarkBackground, onBackground = DarkInk,
    surface = DarkSurface, onSurface = DarkInk,
    surfaceVariant = DarkSubtle, onSurfaceVariant = DarkMuted,
    surfaceContainerLowest = DarkSurface, surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface, surfaceContainerHigh = DarkSubtle,
    surfaceContainerHighest = DarkSubtle, surfaceTint = DarkSurface,
    outline = DarkBorder, outlineVariant = DarkLine,
    error = DarkDanger, onError = DarkBackground,
    errorContainer = DarkDangerBackground, onErrorContainer = DarkDanger,
    inverseSurface = LightSurface, inverseOnSurface = LightInk, inversePrimary = LightAccent,
) }

private fun type(size: Int, weight: FontWeight = FontWeight.Normal, heading: Boolean = false) = TextStyle(
    fontFamily = FontFamily.Default, fontSize = size.sp, fontWeight = weight,
    lineHeight = (size * if (heading) 1.2f else 1.5f).sp,
)
private val PowerkTypography = Typography(
    displayLarge = type(48, FontWeight.SemiBold, true),
    displayMedium = type(40, FontWeight.SemiBold, true),
    displaySmall = type(32, FontWeight.SemiBold, true),
    headlineLarge = type(32, FontWeight.SemiBold, true),
    headlineMedium = type(28, FontWeight.SemiBold, true),
    headlineSmall = type(24, FontWeight.SemiBold, true),
    titleLarge = type(20, FontWeight.SemiBold, true),
    titleMedium = type(16, FontWeight.SemiBold), titleSmall = type(14, FontWeight.SemiBold),
    bodyLarge = type(16), bodyMedium = type(14), bodySmall = type(12),
    labelLarge = type(14, FontWeight.Medium), labelMedium = type(12, FontWeight.Medium),
    labelSmall = type(12, FontWeight.Medium),
)
private val PowerkShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp),
)
fun powerkScheme(dark: Boolean): ColorScheme = if (dark) DarkScheme else LightScheme

@Composable
fun PowerkTheme(dark: Boolean = androidx.compose.foundation.isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = powerkScheme(dark), typography = PowerkTypography, shapes = PowerkShapes, content = content)
}
