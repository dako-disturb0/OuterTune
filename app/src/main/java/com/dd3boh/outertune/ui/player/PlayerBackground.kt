/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.dd3boh.outertune.constants.PlayerBackgroundStyle
import com.dd3boh.outertune.constants.PlayerCustomColorPaletteKey
import com.dd3boh.outertune.extensions.isPowerSaver
import com.dd3boh.outertune.playback.PlayerConnection
import com.dd3boh.outertune.ui.theme.extractGradientColors
import com.dd3boh.outertune.ui.theme.extractThemeColor
import com.dd3boh.outertune.utils.coilCoroutine
import com.dd3boh.outertune.utils.rememberPreference
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

    val customColorPalette by rememberPreference(PlayerCustomColorPaletteKey, defaultValue = "")

    var dominantColor by remember { mutableStateOf<Color?>(null) }
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    // Fetch bitmap and extract colors when needed
    LaunchedEffect(mediaMetadata, playerBackground) {
        if (context.isPowerSaver()) return@LaunchedEffect
        if (playerBackground == PlayerBackgroundStyle.FOLLOW_THEME || playerBackground == PlayerBackgroundStyle.COLOR_PALETTE) {
            return@LaunchedEffect
        }

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

    val animatedDominantColor by animateColorAsState(
        targetValue = dominantColor ?: MaterialTheme.colorScheme.surface,
        animationSpec = tween(1000),
        label = "dominantColorAnimation"
    )

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
            .fillMaxSize()
    ) {
        when (playerBackground) {
            PlayerBackgroundStyle.FOLLOW_ARTWORK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(animatedDominantColor)
                )
            }

            PlayerBackgroundStyle.FOLLOW_THEME -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            PlayerBackgroundStyle.GRADIENT -> {
                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = { fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))) },
                    label = "gradientBg"
                ) { colors ->
                    if (colors.size >= 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors))
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

            PlayerBackgroundStyle.BLUR -> {
                AnimatedContent(
                    targetState = mediaMetadata,
                    transitionSpec = { fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))) },
                    label = "blurBg"
                ) { metadata ->
                    AsyncImage(
                        model = metadata?.getThumbnailModel(100, 100),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(100.dp)
                            .alpha(0.5f)
                    )
                }
            }

            PlayerBackgroundStyle.COLOR_PALETTE -> {
                val parsedColors = remember(customColorPalette) {
                    if (customColorPalette.isBlank()) emptyList()
                    else {
                        customColorPalette.split(",").mapNotNull { hex ->
                            val trimmed = hex.trim()
                            runCatching {
                                val colorInt = android.graphics.Color.parseColor(
                                    if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                                )
                                Color(colorInt)
                            }.getOrNull()
                        }
                    }
                }

                if (parsedColors.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(parsedColors))
                    )
                } else if (parsedColors.size == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(parsedColors[0])
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }

            PlayerBackgroundStyle.ANIMATED_GRADIENT -> {
                val infiniteTransition = rememberInfiniteTransition(label = "animatedGradientTransition")
                val xOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(10000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "xOffset"
                )
                val yOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(14000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "yOffset"
                )

                val animColors = remember(gradientColors, dominantColor) {
                    if (gradientColors.size >= 2) {
                        gradientColors + listOf(dominantColor ?: gradientColors[0])
                    } else if (dominantColor != null) {
                        listOf(dominantColor!!, dominantColor!!.copy(alpha = 0.6f), dominantColor!!)
                    } else {
                        emptyList()
                    }
                }

                if (animColors.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithCache {
                                val start = Offset(size.width * xOffset, size.height * (1f - yOffset))
                                val end = Offset(size.width * (1f - xOffset), size.height * yOffset)
                                val brush = Brush.linearGradient(
                                    colors = animColors,
                                    start = start,
                                    end = end
                                )
                                onDrawBehind {
                                    drawRect(brush = brush)
                                }
                            }
                    )
                } else {
                    val fallbackColors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithCache {
                                val start = Offset(size.width * xOffset, size.height * (1f - yOffset))
                                val end = Offset(size.width * (1f - xOffset), size.height * yOffset)
                                val brush = Brush.linearGradient(
                                    colors = fallbackColors,
                                    start = start,
                                    end = end
                                )
                                onDrawBehind {
                                    drawRect(brush = brush)
                                }
                            }
                    )
                }
            }

            PlayerBackgroundStyle.DEFAULT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
                )
                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = { fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))) },
                    label = "defaultGradientOverlay"
                ) { colors ->
                    if (colors.size >= 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors), alpha = 0.4f)
                        )
                    }
                }
            }
        }

        if (playerBackground != PlayerBackgroundStyle.FOLLOW_THEME && showLyrics) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (useDarkTheme) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
            )
        }
    }
}
