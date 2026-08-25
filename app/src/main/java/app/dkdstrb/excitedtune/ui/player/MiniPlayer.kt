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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_READY
import coil3.compose.AsyncImage
import app.dkdstrb.excitedtune.LocalPlayerAwareWindowInsets
import app.dkdstrb.excitedtune.LocalPlayerConnection
import app.dkdstrb.excitedtune.R
import app.dkdstrb.excitedtune.constants.ListThumbnailSize
import app.dkdstrb.excitedtune.constants.MiniPlayerHeight
import app.dkdstrb.excitedtune.constants.MiniPlayerLyricModeKey
import app.dkdstrb.excitedtune.constants.ShowLyricInMiniPlayerKey
import app.dkdstrb.excitedtune.constants.ThumbnailCornerRadius
import app.dkdstrb.excitedtune.extensions.togglePlayPause
import app.dkdstrb.excitedtune.models.MediaMetadata
import app.dkdstrb.excitedtune.ui.component.button.IconButton
import app.dkdstrb.excitedtune.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.akanework.gramophone.logic.utils.SemanticLyrics
import kotlin.math.roundToInt

// ─── Lyric display mode ──────────────────────────────────────────────────────
private const val LYRIC_MODE_DYNAMIC = "dynamic"
private const val LYRIC_MODE_STATIC  = "static"

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val queueBoard by playerConnection.queueBoard.collectAsState()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val showLyricInMiniPlayer by rememberPreference(ShowLyricInMiniPlayerKey, defaultValue = true)
    val lyricsModel by playerConnection.currentLyrics.collectAsState()

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }

    LaunchedEffect(playbackState, isPlaying) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(300)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 8.dp,
        tonalElevation = 6.dp,
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .fillMaxWidth()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Progress micro-line pinned at the top edge of the card, giving the
            // miniplayer its own identity against the navbar card below it
            LinearProgressIndicator(
                progress = { (position.toFloat() / duration).coerceIn(0f, 1f) },
                drawStopIndicator = { },
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MiniPlayerHeight - 8.dp)
                    .padding(end = 6.dp),
            ) {
                val iconButtonColor = MaterialTheme.colorScheme.onSurface
                Box(Modifier.weight(1f)) {
                    mediaMetadata?.let {
                        MiniMediaInfo(
                            mediaMetadata = it,
                            error = error,
                            lyricsModel = lyricsModel,
                            currentPosition = position,
                            showLyricInMiniPlayer = showLyricInMiniPlayer,
                            playbackState = playbackState,
                            isPlaying = isPlaying,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (playerConnection.player.currentMediaItem == null) {
                            queueBoard.setCurrQueue()
                            playerConnection.player.togglePlayPause()
                        } else if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    }
                ) {
                    androidx.compose.material3.Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState == Player.STATE_ENDED) Icons.Rounded.Replay else if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                IconButton(
                    enabled = canSkipNext,
                    onClick = {
                        if (playerConnection.player.currentMediaItem == null) {
                            queueBoard.setCurrQueue()
                            playerConnection.player.playWhenReady = true
                        }
                        playerConnection.player.seekToNext()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        tint = iconButtonColor.copy(alpha = (if (canSkipNext) 1f else 0.4f)),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    lyricsModel: SemanticLyrics? = null,
    currentPosition: Long = 0L,
    showLyricInMiniPlayer: Boolean = true,
    playbackState: Int = STATE_READY,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val playerConnection = LocalPlayerConnection.current
    val isWaitingForNetwork by playerConnection?.waitingForNetworkConnection?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    // Lyric display mode preference
    val lyricMode by rememberPreference(MiniPlayerLyricModeKey, defaultValue = LYRIC_MODE_STATIC)
    val isDynamicMode = lyricMode == LYRIC_MODE_DYNAMIC

    val px = (ListThumbnailSize.value * density.density).roundToInt()

    val currentLyricText = remember(lyricsModel, currentPosition) {
        if (lyricsModel is SemanticLyrics.SyncedLyrics && lyricsModel.text.isNotEmpty()) {
            val lines = lyricsModel.text
            var idx = -1
            for (i in lines.indices) {
                if (lines[i].start <= currentPosition.toULong()) {
                    idx = i
                } else break
            }
            if (idx >= 0 && idx < lines.size) {
                lines[idx].text.trim().takeIf { it.isNotBlank() }
            } else null
        } else null
    }

    // Only show the buffering spinner when the player is genuinely buffering AND supposed to be playing.
    // Avoid showing it during intentional silent gaps (e.g. between tracks when paused or on STATE_READY).
    val showBufferingSpinner = remember(playbackState, isPlaying, isWaitingForNetwork, error) {
        error == null && (
            isWaitingForNetwork ||
            (playbackState == STATE_BUFFERING && isPlaying)
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // ── Artwork + loading/error overlay ──────────────────────────────────
        BoxWithConstraints(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .size(48.dp)
        ) {
            AsyncImage(
                model = mediaMetadata.getThumbnailModel(px, px),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = error != null || showBufferingSpinner,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius)
                        )
                        .fillMaxSize()
                ) {
                    if (showBufferingSpinner) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else if (error != null) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        // ── Title + artist / lyrics ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Text(
                text = mediaMetadata.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            AnimatedContent(
                targetState = currentLyricText,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + slideInVertically { height -> height / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutVertically { height -> -height / 2 })
                },
                label = "MiniPlayerLyricAnimation"
            ) { targetLyric ->
                if (showLyricInMiniPlayer && !targetLyric.isNullOrBlank()) {
                    // ── Lyric line ─────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .then(
                                if (isDynamicMode) Modifier          // expand freely
                                else Modifier.heightIn(min = 36.dp)  // static: ~3 lines reserved
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Subtitles,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(13.dp)
                                .padding(end = 2.dp)
                        )
                        Text(
                            text = targetLyric,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            // Dynamic: wrap long lines. Static: single line with ellipsis (room already reserved).
                            maxLines = if (isDynamicMode) Int.MAX_VALUE else 3,
                            overflow = if (isDynamicMode) TextOverflow.Clip else TextOverflow.Ellipsis,
                            lineHeight = 16.sp,
                        )
                    }
                } else {
                    // ── Artist fallback ────────────────────────────────────
                    Text(
                        text = mediaMetadata.artists.joinToString { it.name },
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .then(
                                // Keep height consistent with lyric area when Static mode is on
                                if (!isDynamicMode && showLyricInMiniPlayer)
                                    Modifier.heightIn(min = 36.dp)
                                else Modifier
                            )
                    )
                }
            }
        }
    }
}
