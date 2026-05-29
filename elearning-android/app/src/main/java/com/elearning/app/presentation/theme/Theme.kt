package com.elearning.app.presentation.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Brand Color Palette ──────────────────────────────────────────────────────

object ELearningColors {
    // Figma-aligned tokens for the mobile redesign. Keep the legacy Material
    // palette below until existing screens are migrated.
    val BrandBlue         = Color(0xFF2954C8)
    val BrandBlueDark     = Color(0xFF1E3A8A)
    val AppBackground     = Color(0xFFFAFAFC)
    val CardSurface       = Color(0xFFFFFFFF)
    val TextPrimary       = Color(0xFF111827)
    val TextSecondary     = Color(0xFF4B5563)
    val TextTertiary      = Color(0xFF6B7280)
    val BorderSubtle      = Color(0xFFF3F4F6)

    // Primary — Rich Indigo
    val Primary            = Color(0xFF4F46E5)
    val PrimaryContainer   = Color(0xFFE0E7FF)
    val OnPrimary          = Color.White
    val OnPrimaryContainer = Color(0xFF1E1B4B)

    // Secondary — Vibrant Cyan
    val Secondary            = Color(0xFF06B6D4)
    val SecondaryContainer   = Color(0xFFCFFAFE)
    val OnSecondary          = Color.White
    val OnSecondaryContainer = Color(0xFF083344)

    // Tertiary — Warm Amber (badges, achievements)
    val Tertiary            = Color(0xFFF59E0B)
    val TertiaryContainer   = Color(0xFFFEF3C7)
    val OnTertiary          = Color.White
    val OnTertiaryContainer = Color(0xFF451A03)

    // Surface (Light)
    val SurfaceLight        = Color(0xFFF8FAFF)
    val SurfaceVariantLight = Color(0xFFE9EBF8)
    val BackgroundLight     = Color(0xFFF0F2FF)

    // Surface (Dark)
    val SurfaceDark        = Color(0xFF0F0F1A)
    val SurfaceVariantDark = Color(0xFF1E1E32)
    val BackgroundDark     = Color(0xFF070710)

    // Error
    val Error          = Color(0xFFEF4444)
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnError        = Color.White

    // Outline
    val OutlineLight = Color(0xFFBEC2E8)
    val OutlineDark  = Color(0xFF3B3E63)
}

// ─── Material3 Color Schemes ──────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary              = ELearningColors.Primary,
    onPrimary            = ELearningColors.OnPrimary,
    primaryContainer     = ELearningColors.PrimaryContainer,
    onPrimaryContainer   = ELearningColors.OnPrimaryContainer,
    secondary            = ELearningColors.Secondary,
    onSecondary          = ELearningColors.OnSecondary,
    secondaryContainer   = ELearningColors.SecondaryContainer,
    onSecondaryContainer = ELearningColors.OnSecondaryContainer,
    tertiary             = ELearningColors.Tertiary,
    onTertiary           = ELearningColors.OnTertiary,
    tertiaryContainer    = ELearningColors.TertiaryContainer,
    onTertiaryContainer  = ELearningColors.OnTertiaryContainer,
    error                = ELearningColors.Error,
    errorContainer       = ELearningColors.ErrorContainer,
    onError              = ELearningColors.OnError,
    background           = ELearningColors.BackgroundLight,
    surface              = ELearningColors.SurfaceLight,
    surfaceVariant       = ELearningColors.SurfaceVariantLight,
    outline              = ELearningColors.OutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary              = Color(0xFF818CF8),
    onPrimary            = Color(0xFF1E1B4B),
    primaryContainer     = Color(0xFF312E81),
    onPrimaryContainer   = Color(0xFFE0E7FF),
    secondary            = Color(0xFF22D3EE),
    onSecondary          = Color(0xFF083344),
    secondaryContainer   = Color(0xFF155E75),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary             = Color(0xFFFBBF24),
    onTertiary           = Color(0xFF451A03),
    tertiaryContainer    = Color(0xFF92400E),
    onTertiaryContainer  = Color(0xFFFEF3C7),
    error                = Color(0xFFF87171),
    errorContainer       = Color(0xFF7F1D1D),
    onError              = Color(0xFF450A0A),
    background           = ELearningColors.BackgroundDark,
    surface              = ELearningColors.SurfaceDark,
    surfaceVariant       = ELearningColors.SurfaceVariantDark,
    outline              = ELearningColors.OutlineDark,
)

// ─── Typography Scale ─────────────────────────────────────────────────────────

val ELearningTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

// ─── Theme Accessor ───────────────────────────────────────────────────────────

object ELearningTheme {
    val extended: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

// ─── Root Theme Composable ────────────────────────────────────────────────────

@Composable
fun ELearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme  = if (darkTheme) DarkColorScheme  else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = ELearningTypography,
            shapes      = ELearningShapes,
            content     = content
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Light Theme Colors")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme Colors")
@Composable
private fun ThemePreview() {
    ELearningTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Primary", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                Text("Secondary", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleMedium)
                Text("Tertiary", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.titleSmall)
                Text("Success Extended", color = ELearningTheme.extended.success, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview(showBackground = true, name = "Figma Tokens")
@Composable
private fun FigmaTokensPreview() {
    ELearningTheme {
        Surface(color = ELearningColors.AppBackground) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(FigmaSpacing.itemGap)
            ) {
                Text(
                    text = "Figma mobile tokens",
                    color = ELearningColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                FigmaColorSwatch("BrandBlue", ELearningColors.BrandBlue)
                FigmaColorSwatch("BrandBlueDark", ELearningColors.BrandBlueDark)
                FigmaColorSwatch("AppBackground", ELearningColors.AppBackground)
                FigmaColorSwatch("CardSurface", ELearningColors.CardSurface)
                FigmaColorSwatch("BorderSubtle", ELearningColors.BorderSubtle)
            }
        }
    }
}

@Composable
private fun FigmaColorSwatch(name: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = color, shape = Radius.md)
        )
        Text(
            text = name,
            color = ELearningColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
