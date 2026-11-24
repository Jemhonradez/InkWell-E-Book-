package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = InkTangerine,
    onPrimary = Color.White,
    primaryContainer = InkTangerineContainerLight,
    onPrimaryContainer = Color(0xFF3E1200),
    secondary = MidnightInk,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1E6EF),
    onSecondaryContainer = MidnightInk,
    tertiary = WarmGold,
    onTertiary = Color.White,
    tertiaryContainer = WarmGoldContainerLight,
    onTertiaryContainer = Color(0xFF362100),
    background = PaperBackgroundLight,
    onBackground = InkCharcoal,
    surface = PaperSurfaceLight,
    onSurface = InkCharcoal,
    surfaceVariant = PaperSurfaceVariantLight,
    onSurfaceVariant = InkMuted,
    outline = InkOutlineLight,
    outlineVariant = Color(0xFFEDE7DE)
)

private val DarkColorScheme = darkColorScheme(
    primary = InkTangerineDark,
    onPrimary = Color(0xFF380F00),
    primaryContainer = InkTangerineContainerDark,
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFB5C4DB),
    onSecondary = Color(0xFF151E2B),
    secondaryContainer = MidnightInkSoft,
    onSecondaryContainer = Color(0xFFDCE4F2),
    tertiary = WarmGold,
    onTertiary = Color(0xFF311F00),
    tertiaryContainer = WarmGoldContainerDark,
    onTertiaryContainer = Color(0xFFFFDFAA),
    background = PaperBackgroundDark,
    onBackground = InkCream,
    surface = PaperSurfaceDark,
    onSurface = InkCream,
    surfaceVariant = PaperSurfaceVariantDark,
    onSurfaceVariant = InkMutedDark,
    outline = InkOutlineDark,
    outlineVariant = Color(0xFF282B33)
)

val InkwellShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun InkwellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InkwellTypography,
        shapes = InkwellShapes,
        content = content
    )
}
