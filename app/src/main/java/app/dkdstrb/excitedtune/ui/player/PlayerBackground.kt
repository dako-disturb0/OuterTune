/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package app.dkdstrb.excitedtune.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import app.dkdstrb.excitedtune.constants.PlayerBackgroundStyle
import app.dkdstrb.excitedtune.extensions.isPowerSaver
import app.dkdstrb.excitedtune.playback.PlayerConnection
import app.dkdstrb.excitedtune.ui.theme.extractGradientColors
import app.dkdstrb.excitedtune.ui.theme.extractThemeColor
import app.dkdstrb.excitedtune.utils.coilCoroutine
import kotlinx.coroutines.withContext

@Composable
fun PlayerBackground(
    playerConnection: PlayerConnection,
    playerBackground: PlayerBackgroundStyle,
    showLyrics: Boolean,
    useDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    var dominantColor by remember { mutableStateOf<Color?>(null) }
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    // Fetch bitmap and extract colors when needed
    LaunchedEffect(mediaMetadata, playerBackground) {
        if (context.isPowerSaver()) return@LaunchedEffect

        withContext(coilCoroutine) {
            val result = context.imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(mediaMetadata?.getThumbnailModel(100, 100))
                    .allowHardware(false)
                    .build()
            )
            val bitmap = result.image?.toBitmap()
            if (bitmap != null) {
                dominantColor = bitmap.extractThemeColor()
                gradientColors = bitmap.extractGradientColors()
            }
        }
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
            .fillMaxSize()
    ) {
        when (playerBackground) {
            PlayerBackgroundStyle.DYNAMIC_LIGHT -> {
                // Lit gradient from the cover: dominant artwork colors lifted toward
                // white so the player feels illuminated by the artwork
                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = { fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))) },
                    label = "dynamicLightBg"
                ) { colors ->
                    val lightColors = remember(colors) {
                        when (colors.size) {
                            0 -> emptyList()
                            1 -> listOf(colors[0].lighten(0.25f), colors[0].lighten(0.45f))
                            else -> colors.take(2).map { it.lighten(0.25f) }
                        }
                    }
                    if (lightColors.size >= 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(lightColors))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }

            PlayerBackgroundStyle.COLOR_PALETTE -> {
                // Full artwork palette ordered from most to least dominant
                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = { fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))) },
                    label = "paletteBg"
                ) { colors ->
                    if (colors.size >= 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors))
                        )
                    } else if (colors.size == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors[0])
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }
        }

        if (showLyrics) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (useDarkTheme) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
            )
        }
    }
}

/**
 * Lift a color toward white by [fraction] (0f..1f)
 */
private fun Color.lighten(fraction: Float): Color = Color(
    red = red + (1f - red) * fraction,
    green = green + (1f - green) * fraction,
    blue = blue + (1f - blue) * fraction,
    alpha = alpha,
)
