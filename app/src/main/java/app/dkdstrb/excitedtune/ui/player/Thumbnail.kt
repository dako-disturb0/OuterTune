/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package app.dkdstrb.excitedtune.ui.player

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import app.dkdstrb.excitedtune.LocalPlayerConnection
import app.dkdstrb.excitedtune.constants.AuroraThemeKey
import app.dkdstrb.excitedtune.constants.PlayerHorizontalPadding
import app.dkdstrb.excitedtune.constants.PlayerThumbnailAutoCropKey
import app.dkdstrb.excitedtune.constants.PlayerThumbnailRoundnessKey
import app.dkdstrb.excitedtune.constants.PlayerThumbnailSizeKey
import app.dkdstrb.excitedtune.constants.ShowLyricsKey
import app.dkdstrb.excitedtune.models.MediaMetadata
import app.dkdstrb.excitedtune.ui.component.Lyrics
import app.dkdstrb.excitedtune.ui.utils.expressiveClickable
import app.dkdstrb.excitedtune.ui.utils.highRes
import app.dkdstrb.excitedtune.utils.rememberPreference

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Thumbnail(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    showLyricsOnClick: Boolean = false,
    customMediaMetadata: MediaMetadata? = null,
    size: Float? = null,
    roundness: Int? = null,
    autoCrop: Boolean? = null,
) {
    val context = LocalContext.current
    val currentView = LocalView.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val prefSize by rememberPreference(PlayerThumbnailSizeKey, defaultValue = 1.0f)
    val prefRoundness by rememberPreference(PlayerThumbnailRoundnessKey, defaultValue = 24)
    val prefAutoCrop by rememberPreference(PlayerThumbnailAutoCropKey, defaultValue = false)
    val auroraTheme by rememberPreference(AuroraThemeKey, defaultValue = false)

    val thumbnailSize = size ?: prefSize
    val thumbnailRoundness = roundness ?: prefRoundness
    val thumbnailAutoCrop = autoCrop ?: prefAutoCrop

    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    val playerMediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata = customMediaMetadata ?: playerMediaMetadata

    DisposableEffect(showLyrics) {
        currentView.keepScreenOn = showLyrics
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    val shape = RoundedCornerShape(thumbnailRoundness.dp)

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !showLyrics && error == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f, false)
                ) {
                    // Aurora: soft primary bloom behind the artwork
                    if (auroraTheme) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(thumbnailSize.coerceIn(0.1f, 1.0f))
                                .aspectRatio(1f)
                                .blur(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    RoundedCornerShape(thumbnailRoundness.dp)
                                )
                        )
                    }

                    AsyncImage(
                        // highRes() upgrades the URL to maxresdefault / hq720 for full-screen display
                        model = (mediaMetadata?.thumbnailUrl?.highRes() ?: mediaMetadata?.getThumbnailModel()),
                        contentDescription = null,
                        // Auto-crop fills the square (cropping non-square artwork);
                        // otherwise the whole artwork is shown uncropped
                        contentScale = if (thumbnailAutoCrop) ContentScale.Crop else ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize(thumbnailSize.coerceIn(0.1f, 1.0f))
                            .aspectRatio(1f)
                            .clip(shape)
                            .expressiveClickable(
                                enabled = showLyricsOnClick,
                                pressedScale = 0.95f,
                            ) {
                                showLyrics = !showLyrics
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showLyrics && error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Lyrics(sliderPositionProvider = sliderPositionProvider)
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            error?.let { error ->
                ThumbnailPlaybackError(
                    error = error,
                    retry = playerConnection.player::prepare
                )
            }
        }
    }
}
