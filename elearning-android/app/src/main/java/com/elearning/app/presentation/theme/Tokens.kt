package com.elearning.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Spacing Scale ────────────────────────────────────────────────────────────

object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp  = 4.dp
    val sm: Dp  = 8.dp
    val md: Dp  = 12.dp
    val lg: Dp  = 16.dp
    val xl: Dp  = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
    val huge: Dp = 64.dp
}

// Figma mobile redesign layout tokens. Use these for new Figma-derived screens
// while keeping the generic spacing scale available for existing screens.
object FigmaSpacing {
    val pageHorizontal: Dp = 24.dp
    val sectionGap: Dp = 32.dp
    val cardPadding: Dp = 16.dp
    val heroPadding: Dp = 24.dp
    val itemGap: Dp = 12.dp
    val rowGap: Dp = 16.dp
}

// ─── Shape Scale ──────────────────────────────────────────────────────────────

val ELearningShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Shape aliases for readability
object Radius {
    val xs   = RoundedCornerShape(4.dp)
    val sm   = RoundedCornerShape(8.dp)
    val md   = RoundedCornerShape(12.dp)
    val lg   = RoundedCornerShape(16.dp)
    val item = RoundedCornerShape(14.dp)
    val panel = RoundedCornerShape(20.dp)
    val xl   = RoundedCornerShape(24.dp)
    val search = RoundedCornerShape(18.dp)
    val dialog = RoundedCornerShape(32.dp)
    val full = RoundedCornerShape(50)   // Pill / circle
}

// ─── Animation Durations ──────────────────────────────────────────────────────

object AnimDuration {
    const val fast   = 150
    const val normal = 250
    const val slow   = 400
    const val xSlow  = 600
}

// ─── Elevation Scale ──────────────────────────────────────────────────────────

object Elevation {
    val none:   Dp = 0.dp
    val low:    Dp = 2.dp
    val medium: Dp = 4.dp
    val high:   Dp = 8.dp
    val xHigh:  Dp = 16.dp
}

// ─── Custom Color Extensions (CompositionLocal) ───────────────────────────────
// These semantic colors go beyond Material3's standard palette.

data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val offlineBanner: Color,
    val onOfflineBanner: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color,
    // Level badge colors
    val levelBeginner: Color,
    val levelIntermediate: Color,
    val levelAdvanced: Color
)

val LightExtendedColors = ExtendedColors(
    success              = Color(0xFF10B981),
    onSuccess            = Color.White,
    successContainer     = Color(0xFFD1FAE5),
    onSuccessContainer   = Color(0xFF064E3B),
    warning              = Color(0xFFF59E0B),
    onWarning            = Color.White,
    warningContainer     = Color(0xFFFEF3C7),
    offlineBanner        = Color(0xFF374151),
    onOfflineBanner      = Color.White,
    shimmerBase          = Color(0xFFE5E7EB),
    shimmerHighlight     = Color(0xFFF9FAFB),
    levelBeginner        = Color(0xFF10B981),   // Green
    levelIntermediate    = Color(0xFFF59E0B),   // Amber
    levelAdvanced        = Color(0xFFEF4444),   // Red
)

val DarkExtendedColors = ExtendedColors(
    success              = Color(0xFF34D399),
    onSuccess            = Color(0xFF064E3B),
    successContainer     = Color(0xFF065F46),
    onSuccessContainer   = Color(0xFFD1FAE5),
    warning              = Color(0xFFFBBF24),
    onWarning            = Color(0xFF451A03),
    warningContainer     = Color(0xFF92400E),
    offlineBanner        = Color(0xFF1F2937),
    onOfflineBanner      = Color(0xFFD1D5DB),
    shimmerBase          = Color(0xFF1F2937),
    shimmerHighlight     = Color(0xFF374151),
    levelBeginner        = Color(0xFF34D399),
    levelIntermediate    = Color(0xFFFBBF24),
    levelAdvanced        = Color(0xFFF87171),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
