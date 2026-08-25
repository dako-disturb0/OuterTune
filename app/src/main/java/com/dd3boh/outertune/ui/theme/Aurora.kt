/*
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dd3boh.outertune.R

/**
 * Aurora theme - dark-first palette with an electric violet accent and teal
 * lyrics highlight. Colors follow the Aurora design tokens.
 */

val AuroraSora = FontFamily(
    Font(R.font.sora_400, FontWeight.Normal),
    Font(R.font.sora_600, FontWeight.SemiBold),
    Font(R.font.sora_700, FontWeight.Bold),
    Font(R.font.sora_800, FontWeight.ExtraBold),
)

val AuroraHanken = FontFamily(
    Font(R.font.hanken_grotesk_400, FontWeight.Normal),
    Font(R.font.hanken_grotesk_500, FontWeight.Medium),
    Font(R.font.hanken_grotesk_600, FontWeight.SemiBold),
    Font(R.font.hanken_grotesk_700, FontWeight.Bold),
)

private val AuroraDarkColors = ColorScheme(
    primary = Color(0xFFCABEFF),
    onPrimary = Color(0xFF32009A),
    primaryContainer = Color(0xFF947DFF),
    onPrimaryContainer = Color(0xFF2B0088),
    inversePrimary = Color(0xFF613DE0),
    secondary = Color(0xFFCCBEFF),
    onSecondary = Color(0xFF341F75),
    secondaryContainer = Color(0xFF4D3A90),
    onSecondaryContainer = Color(0xFFBEADFF),
    tertiary = Color(0xFF3CDDC7),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF00A392),
    onTertiaryContainer = Color(0xFF00302A),
    background = Color(0xFF131317),
    onBackground = Color(0xFFE4E1E7),
    surface = Color(0xFF131317),
    onSurface = Color(0xFFE4E1E7),
    surfaceVariant = Color(0xFF353439),
    onSurfaceVariant = Color(0xFFC9C4D8),
    surfaceTint = Color(0xFFCABEFF),
    inverseSurface = Color(0xFFE4E1E7),
    inverseOnSurface = Color(0xFF303034),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF938EA1),
    outlineVariant = Color(0xFF484555),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF39393D),
    surfaceDim = Color(0xFF131317),
    surfaceContainerLowest = Color(0xFF0E0E12),
    surfaceContainerLow = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFF1F1F23),
    surfaceContainerHigh = Color(0xFF2A292E),
    surfaceContainerHighest = Color(0xFF353439),
    primaryFixed = Color(0xFFE6DEFF),
    primaryFixedDim = Color(0xFFCABEFF),
    onPrimaryFixed = Color(0xFF1C0062),
    onPrimaryFixedVariant = Color(0xFF4918C8),
    secondaryFixed = Color(0xFFE7DEFF),
    secondaryFixedDim = Color(0xFFCCBEFF),
    onSecondaryFixed = Color(0xFF1E0060),
    onSecondaryFixedVariant = Color(0xFF4B388D),
    tertiaryFixed = Color(0xFF62FAE3),
    tertiaryFixedDim = Color(0xFF3CDDC7),
    onTertiaryFixed = Color(0xFF00201C),
    onTertiaryFixedVariant = Color(0xFF005047),
)

private val AuroraLightColors = ColorScheme(
    primary = Color(0xFF613DE0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF2B0088),
    inversePrimary = Color(0xFFCABEFF),
    secondary = Color(0xFF5B4A9E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7DEFF),
    onSecondaryContainer = Color(0xFF1E0060),
    tertiary = Color(0xFF00A392),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF62FAE3),
    onTertiaryContainer = Color(0xFF00201C),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFE7E1F4),
    onSurfaceVariant = Color(0xFF484555),
    surfaceTint = Color(0xFF613DE0),
    inverseSurface = Color(0xFF313036),
    inverseOnSurface = Color(0xFFF3EFF7),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF787487),
    outlineVariant = Color(0xFFC9C4D8),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFDFBFF),
    surfaceDim = Color(0xFFD7D4E0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F4FA),
    surfaceContainer = Color(0xFFF1EEF6),
    surfaceContainerHigh = Color(0xFFECE8F0),
    surfaceContainerHighest = Color(0xFFE6E2EB),
    primaryFixed = Color(0xFFE6DEFF),
    primaryFixedDim = Color(0xFFCABEFF),
    onPrimaryFixed = Color(0xFF1C0062),
    onPrimaryFixedVariant = Color(0xFF4918C8),
    secondaryFixed = Color(0xFFE7DEFF),
    secondaryFixedDim = Color(0xFFCCBEFF),
    onSecondaryFixed = Color(0xFF1E0060),
    onSecondaryFixedVariant = Color(0xFF4B388D),
    tertiaryFixed = Color(0xFF62FAE3),
    tertiaryFixedDim = Color(0xFF3CDDC7),
    onTertiaryFixed = Color(0xFF00201C),
    onTertiaryFixedVariant = Color(0xFF005047),
)

/**
 * Sora for expressive headings, Hanken Grotesk for body text.
 * Sizes and spacing follow Material 3 defaults so existing layouts are unaffected.
 */
fun auroraTypography(base: Typography): Typography = base.withFontFamilies()

private fun Typography.withFontFamilies(): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = AuroraSora),
    displayMedium = displayMedium.copy(fontFamily = AuroraSora),
    displaySmall = displaySmall.copy(fontFamily = AuroraSora),
    headlineLarge = headlineLarge.copy(fontFamily = AuroraSora),
    headlineMedium = headlineMedium.copy(fontFamily = AuroraSora),
    headlineSmall = headlineSmall.copy(fontFamily = AuroraSora),
    titleLarge = titleLarge.copy(fontFamily = AuroraSora),
    titleMedium = titleMedium.copy(fontFamily = AuroraSora),
    bodyLarge = bodyLarge.copy(fontFamily = AuroraHanken),
    bodyMedium = bodyMedium.copy(fontFamily = AuroraHanken),
    bodySmall = bodySmall.copy(fontFamily = AuroraHanken),
    labelLarge = labelLarge.copy(fontFamily = AuroraHanken),
    labelMedium = labelMedium.copy(fontFamily = AuroraHanken),
    labelSmall = labelSmall.copy(fontFamily = AuroraHanken),
)

fun auroraColorScheme(darkTheme: Boolean, pureBlack: Boolean): ColorScheme =
    if (darkTheme) {
        val scheme = AuroraDarkColors
        if (pureBlack) scheme.copy(surface = Color.Black, background = Color.Black) else scheme
    } else {
        AuroraLightColors
    }
