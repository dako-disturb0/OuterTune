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
import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.media3.common.C
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import app.dkdstrb.excitedtune.LocalMenuState
import app.dkdstrb.excitedtune.LocalPlayerConnection
import app.dkdstrb.excitedtune.R
import app.dkdstrb.excitedtune.constants.DEFAULT_PLAYER_BACKGROUND
import app.dkdstrb.excitedtune.constants.DarkMode
import app.dkdstrb.excitedtune.constants.DarkModeKey
import app.dkdstrb.excitedtune.constants.PlayerBackgroundStyle
import app.dkdstrb.excitedtune.constants.PlayerBackgroundStyleKey
import app.dkdstrb.excitedtune.constants.PlayerHorizontalPadding
import app.dkdstrb.excitedtune.constants.QueuePeekHeight
import app.dkdstrb.excitedtune.constants.SeekIncrement
import app.dkdstrb.excitedtune.constants.SeekIncrementKey
import app.dkdstrb.excitedtune.constants.ShowInlineInfoKey
import app.dkdstrb.excitedtune.constants.ShowLyricsKey
import app.dkdstrb.excitedtune.constants.SwipeToSkipKey
import app.dkdstrb.excitedtune.extensions.isPowerSaver
import app.dkdstrb.excitedtune.extensions.metadata
import app.dkdstrb.excitedtune.extensions.supportsWideScreen
import app.dkdstrb.excitedtune.extensions.tabMode
import app.dkdstrb.excitedtune.extensions.togglePlayPause
import app.dkdstrb.excitedtune.extensions.toggleRepeatMode
import app.dkdstrb.excitedtune.playback.PlayerConnection
import app.dkdstrb.excitedtune.playback.QueueBoard
import app.dkdstrb.excitedtune.ui.component.BottomSheet
import app.dkdstrb.excitedtune.ui.component.BottomSheetState
import app.dkdstrb.excitedtune.ui.component.button.IconButton
import app.dkdstrb.excitedtune.ui.component.button.ResizableIconButton
import app.dkdstrb.excitedtune.ui.component.collapsedAnchor
import app.dkdstrb.excitedtune.ui.component.dismissedAnchor
import app.dkdstrb.excitedtune.ui.component.rememberBottomSheetState
import app.dkdstrb.excitedtune.ui.menu.PlayerMenu
import app.dkdstrb.excitedtune.ui.theme.extractGradientColors
import app.dkdstrb.excitedtune.ui.utils.SnapLayoutInfoProvider
import app.dkdstrb.excitedtune.ui.utils.expressiveClickable
import app.dkdstrb.excitedtune.utils.coilCoroutine
import app.dkdstrb.excitedtune.utils.makeTimeString
import app.dkdstrb.excitedtune.utils.rememberEnumPreference
import app.dkdstrb.excitedtune.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.max

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val TAG = "BottomSheetPlayer"
    Log.v(TAG, "PLR-1")

    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val queueBoard by playerConnection.service.queueBoard.collectAsState()

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = DEFAULT_PLAYER_BACKGROUND
    )

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    val qbInit by playerConnection.service.qbInit.collectAsState()

    LaunchedEffect(qbInit, queueBoard.masterQueues.toList()) {
        Log.d(TAG, "Queues changed. qbInit = $qbInit")
        if (qbInit && !queueBoard.masterQueues.isEmpty() && state.isDismissed) {
            Log.d(TAG, "Triggering sheet collapseSoft")
            state.collapseSoft()
        }
    }


    BottomSheet(
        state = state,
        modifier = modifier,
        background = {
            PlayerBackground(
                playerConnection = playerConnection,
                playerBackground = playerBackground,
                showLyrics = showLyrics,
                useDarkTheme = useDarkTheme,
            )
        },
        collapsedBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        onDismiss = {
            playerConnection.softKillPlayer()
        },
        collapsedContent = {
            MiniPlayer()
        }
    ) {
        Log.v(TAG, "PLR-3.0")

        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && !context.tabMode() && context.supportsWideScreen()) {
            LandscapePlayer(state, navController, queueBoard)
        } else {
            PortraitPlayer(state, navController, queueBoard)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortraitPlayer(
    playerSheetState: BottomSheetState,
    navController: NavController,
    queueBoard: QueueBoard,
    enableQueueSheet: Boolean = true,
) {
    val TAG = "BottomSheetPlayer"
    Log.v(TAG, "PLR-3.1b")

    val playerConnection = LocalPlayerConnection.current ?: return

    val dismissedBound = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = playerSheetState.expandedBound,
        collapsedBound = dismissedBound + (QueuePeekHeight * 1.2f),
        initialAnchor = collapsedAnchor,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .padding(bottom = queueSheetState.collapsedBound)
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .nestedScroll(playerSheetState.preUpPostDownNestedScrollConnection)
        ) {
            Log.v(TAG, "PLR-3.2b")
            val mediaMetadata by playerConnection.mediaMetadata.collectAsState()


            val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
            val canSkipNext by playerConnection.canSkipNext.collectAsState()

            val swipeToSkip by rememberPreference(SwipeToSkipKey, defaultValue = false)
            val previousMediaMetadata = if (swipeToSkip && playerConnection.player.hasPreviousMediaItem()) {
                val previousIndex = playerConnection.player.previousMediaItemIndex
                playerConnection.player.getMediaItemAt(previousIndex).metadata
            } else null


            val nextMediaMetadata = if (swipeToSkip && playerConnection.player.hasNextMediaItem()) {
                val nextIndex = playerConnection.player.nextMediaItemIndex
                playerConnection.player.getMediaItemAt(nextIndex).metadata
            } else null

            val mediaItems = listOfNotNull(previousMediaMetadata, mediaMetadata, nextMediaMetadata)
            val currentMediaIndex = mediaItems.indexOf(mediaMetadata)


            var sliderPosition by remember {
                mutableStateOf<Long?>(null)
            }


            if (!swipeToSkip) {
                Thumbnail(
                    modifier = Modifier
                        .animateContentSize(),
                    sliderPositionProvider = { sliderPosition },
                    showLyricsOnClick = true,
                    customMediaMetadata = mediaMetadata
                )
            } else {
                val thumbnailLazyGridState = rememberLazyGridState()
                val currentItem by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemIndex } }
                val itemScrollOffset by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemScrollOffset } }

                LaunchedEffect(itemScrollOffset) {
                    if (!thumbnailLazyGridState.isScrollInProgress || itemScrollOffset != 0) return@LaunchedEffect

                    if (currentItem > currentMediaIndex)
                        playerConnection.player.seekToNext()
                    else if (currentItem < currentMediaIndex)
                        playerConnection.player.seekToPreviousMediaItem()
                }

                LaunchedEffect(mediaMetadata, canSkipPrevious, canSkipNext) {
                    val index = maxOf(0, currentMediaIndex)
                    if (playerSheetState.isExpanded)
                        thumbnailLazyGridState.animateScrollToItem(index)
                    else
                        thumbnailLazyGridState.scrollToItem(index)
                }

                val horizontalLazyGridItemWidthFactor = 1f
                val thumbnailSnapLayoutInfoProvider = remember(thumbnailLazyGridState) {
                    SnapLayoutInfoProvider(
                        lazyGridState = thumbnailLazyGridState,
                        positionInLayout = { layoutSize, itemSize ->
                            (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                        }
                    )
                }
                val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

                LazyHorizontalGrid(
                    state = thumbnailLazyGridState,
                    rows = GridCells.Fixed(1),
                    flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                    userScrollEnabled = playerSheetState.isExpanded,
                    modifier = Modifier.padding(vertical = QueuePeekHeight / 2)
                ) {
                    items(
                        items = mediaItems,
                        key = { it.id }
                    ) {
                        Thumbnail(
                            modifier = Modifier
                                .width(horizontalLazyGridItemWidth)
                                .animateContentSize(),
                            sliderPositionProvider = { sliderPosition },
                            showLyricsOnClick = true,
                            customMediaMetadata = it
                        )
                    }
                }
            }
        }

        ControlsContent(playerSheetState, queueSheetState, navController, queueBoard)

        Spacer(Modifier.height(20.dp))

    }

    if (enableQueueSheet) {
        QueueSheet(
            state = queueSheetState,
            playerBottomSheetState = playerSheetState,
            onTerminate = {
                playerSheetState.dismiss()
                queueBoard.detachedHead = false
            },
            navController = navController
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LandscapePlayer(
    playerSheetState: BottomSheetState,
    navController: NavController,
    queueBoard: QueueBoard,
    enableQueueSheet: Boolean = true,
) {
    val TAG = "BottomSheetPlayer"

    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val swipeToSkip by rememberPreference(SwipeToSkipKey, defaultValue = false)
    val previousMediaMetadata = if (swipeToSkip && playerConnection.player.hasPreviousMediaItem()) {
        val previousIndex = playerConnection.player.previousMediaItemIndex
        playerConnection.player.getMediaItemAt(previousIndex).metadata
    } else null

    val nextMediaMetadata = if (swipeToSkip && playerConnection.player.hasNextMediaItem()) {
        val nextIndex = playerConnection.player.nextMediaItemIndex
        playerConnection.player.getMediaItemAt(nextIndex).metadata
    } else null

    val mediaItems = listOfNotNull(previousMediaMetadata, mediaMetadata, nextMediaMetadata)
    val currentMediaIndex = mediaItems.indexOf(mediaMetadata)

    val showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    val dismissedBound = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = playerSheetState.expandedBound,
        collapsedBound = dismissedBound,
        initialAnchor = dismissedAnchor,
    )

    val vPadding = max(
        WindowInsets.safeDrawing.getTop(LocalDensity.current),
        WindowInsets.safeDrawing.getBottom(LocalDensity.current)
    )
    val vPaddingDp = with(LocalDensity.current) { vPadding.toDp() }
    val verticalInsets = WindowInsets(left = 0.dp, top = vPaddingDp, right = 0.dp, bottom = vPaddingDp)
    Row(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).add(verticalInsets)
            )
            .fillMaxSize()
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .nestedScroll(playerSheetState.preUpPostDownNestedScrollConnection)
        ) {
            Log.v(TAG, "PLR-3.1a")
            if (!swipeToSkip) {
                Thumbnail(
                    sliderPositionProvider = { sliderPosition },
                    modifier = Modifier.animateContentSize(),
                    showLyricsOnClick = true,
                    customMediaMetadata = mediaMetadata
                )
            } else {
                val thumbnailLazyGridState = rememberLazyGridState()
                val currentItem by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemIndex } }
                val itemScrollOffset by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemScrollOffset } }

                LaunchedEffect(itemScrollOffset) {
                    if (!thumbnailLazyGridState.isScrollInProgress || itemScrollOffset != 0) return@LaunchedEffect
                    if (currentItem > currentMediaIndex)
                        playerConnection.player.seekToNext()
                    else if (currentItem < currentMediaIndex)
                        playerConnection.player.seekToPreviousMediaItem()
                }

                LaunchedEffect(mediaMetadata, canSkipPrevious, canSkipNext) {
                    val index = maxOf(0, currentMediaIndex)
                    if (playerSheetState.isExpanded)
                        thumbnailLazyGridState.animateScrollToItem(index)
                    else
                        thumbnailLazyGridState.scrollToItem(index)
                }

                val horizontalLazyGridItemWidthFactor = 1f
                val thumbnailSnapLayoutInfoProvider = remember(thumbnailLazyGridState) {
                    SnapLayoutInfoProvider(
                        lazyGridState = thumbnailLazyGridState,
                        positionInLayout = { layoutSize, itemSize ->
                            (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                        }
                    )
                }
                val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

                LazyHorizontalGrid(
                    state = thumbnailLazyGridState,
                    rows = GridCells.Fixed(1),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                    userScrollEnabled = playerSheetState.isExpanded && swipeToSkip
                ) {
                    items(
                        items = mediaItems,
                        key = { it.id }
                    ) {
                        Thumbnail(
                            sliderPositionProvider = { sliderPosition },
                            modifier = Modifier
                                .width(horizontalLazyGridItemWidth)
                                .animateContentSize(),
                            showLyricsOnClick = true,
                            customMediaMetadata = it
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(if (showLyrics) 0.65f else 1f, false)
                .animateContentSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            Spacer(Modifier.weight(1f))
            ControlsContent(playerSheetState, queueSheetState, navController, queueBoard, context.supportsWideScreen())
            Spacer(Modifier.weight(1f))
        }
    }

    if (enableQueueSheet) {
        QueueSheet(
            state = queueSheetState,
            playerBottomSheetState = playerSheetState,
            onTerminate = {
                playerSheetState.dismiss()
                queueBoard.detachedHead = false
            },
            navController = navController
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audio Quality Badge  (kHz / kbps)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Builds the "Show Inline Info" label: "<bitrate>kbps [<buffer>]% <codec>",
 * e.g. "256kbps [73]% AAC". The buffer percentage tells how much of the
 * stream is already fetched and playable without further network access.
 */
private fun inlinePlaybackInfo(bitrate: Int?, mimeType: String?, bufferedPercent: Int): String {
    val kbps = if (bitrate != null && bitrate > 0) "${bitrate / 1000}kbps" else "--kbps"
    val codec = when {
        mimeType == null -> "--"
        mimeType.contains("mp4a") || mimeType.contains("m4a") -> "AAC"
        mimeType.contains("opus") || mimeType.contains("webm") -> "Opus"
        mimeType.contains("flac") -> "FLAC"
        mimeType.contains("mp3") || mimeType.contains("mpeg") -> "MP3"
        mimeType.contains("wav") || mimeType.contains("raw") -> "WAV"
        mimeType.contains("amr") -> "AMR"
        else -> mimeType.substringAfter('/').uppercase()
    }
    return "$kbps [$bufferedPercent]% $codec"
}

@Composable
fun AudioQualityBadge(
    sampleRate: Int?,
    bitrate: Int?,
    onBackgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val kbpsStr = if (bitrate != null && bitrate > 0) "${bitrate / 1000} Kbps" else null
    val khzStr = if (sampleRate != null && sampleRate > 0) {
        val khz = sampleRate / 1000f
        if (khz == khz.toLong().toFloat()) "${khz.toInt()} kHz" else "${"%.1f".format(khz)} kHz"
    } else null

    val label = listOfNotNull(kbpsStr, khzStr).joinToString(" • ")
    if (label.isBlank()) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(onBackgroundColor.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = onBackgroundColor.copy(alpha = 0.85f),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Buttons (Like + More)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ActionButtons(
    playerSheetState: BottomSheetState,
    navController: NavController,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current

    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    // Like button
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        ResizableIconButton(
            icon = if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp),
            onClick = playerConnection::toggleLike
        )
    }

    Spacer(modifier = Modifier.width(8.dp))

    // More / menu button
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        ResizableIconButton(
            icon = Icons.Rounded.MoreVert,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center),
            onClick = {
                menuState.show {
                    PlayerMenu(
                        mediaMetadata = mediaMetadata,
                        navController = navController,
                        playerBottomSheetState = playerSheetState,
                        onDismiss = menuState::dismiss
                    )
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Controls Content — M3 Expressive redesign
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlsContent(
    playerSheetState: BottomSheetState,
    queueSheetState: BottomSheetState,
    navController: NavController,
    queueBoard: QueueBoard,
    showQueueHint: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentFormat by playerConnection.currentFormat.collectAsState()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    // M3 Expressive: play button morphs between pill shapes
    val playPauseRoundness by animateDpAsState(
        targetValue = if (isPlaying) 20.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "playPauseRoundness"
    )
    val playButtonWidth by animateDpAsState(
        targetValue = if (isPlaying) 100.dp else 80.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "playButtonWidth"
    )

    val seekIncrement by rememberEnumPreference(
        key = SeekIncrementKey,
        defaultValue = SeekIncrement.OFF
    )
    val showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)
    val showInlineInfo by rememberPreference(ShowInlineInfoKey, defaultValue = false)

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val onBackgroundColor =
        if (useDarkTheme) MaterialTheme.colorScheme.onSurface
        else {
            val c = MaterialTheme.colorScheme.secondary
            c.copy(alpha = 1f, red = c.red - 0.2f, green = c.green - 0.2f, blue = c.blue - 0.2f)
        }

    val playbackState by playerConnection.playbackState.collectAsState()
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }
    var position by remember(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var bufferedPercent by remember(playbackState) {
        mutableIntStateOf(playerConnection.player.bufferedPercentage)
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(500)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
                bufferedPercent = playerConnection.player.bufferedPercentage
            }
        }
    }

    var sliderPosition by remember { mutableStateOf<Long?>(null) }

    BoxWithConstraints {
        val maxW = maxWidth
        val compactWidth = maxW < 400.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            // ── Top row: action buttons for compact/landscape ──────────────
            if (compactWidth) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding, vertical = 8.dp)
                ) {
                    ActionButtons(playerSheetState, navController)
                }
            }

            // ── Title + Artist + Badge row ─────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Song title
                    Text(
                        text = mediaMetadata?.title ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = onBackgroundColor,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .basicMarquee(iterations = 1, initialDelayMillis = 3000)
                            .clickable(enabled = mediaMetadata?.album != null) {
                                navController.navigate("album/${mediaMetadata?.album!!.id}")
                                playerSheetState.collapseSoft()
                            }
                    )

                    Spacer(Modifier.height(2.dp))

                    // Artist row
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        mediaMetadata?.artists?.fastForEachIndexed { index, artist ->
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = onBackgroundColor.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .basicMarquee(iterations = 1, initialDelayMillis = 5000)
                                    .clickable(enabled = artist.id != null) {
                                        navController.navigate("artist/${artist.id}")
                                        playerSheetState.collapseSoft()
                                    }
                            )
                            if (index != mediaMetadata?.artists?.lastIndex) {
                                Text(
                                    text = ", ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onBackgroundColor.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    // Centered Audio quality badge (Kbps • kHz)
                    Spacer(Modifier.height(6.dp))
                    AudioQualityBadge(
                        sampleRate = currentFormat?.sampleRate,
                        bitrate = currentFormat?.bitrate,
                        onBackgroundColor = onBackgroundColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Action buttons for portrait (inline with title)
                if (!compactWidth) {
                    Spacer(Modifier.width(12.dp))
                    ActionButtons(playerSheetState, navController)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Seek slider (Material 3 default style) ─────────────────────
            Slider(
                value = (sliderPosition ?: position).toFloat(),
                valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                onValueChange = { sliderPosition = it.toLong() },
                onValueChangeFinished = {
                    sliderPosition?.let {
                        playerConnection.player.seekTo(it)
                        position = it
                    }
                    sliderPosition = null
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                },
                modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
            )

            // ── Position / inline info / Duration labels ───────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding + 2.dp)
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = onBackgroundColor.copy(alpha = 0.6f),
                    maxLines = 1,
                )
                if (showInlineInfo) {
                    Text(
                        text = inlinePlaybackInfo(
                            bitrate = currentFormat?.bitrate,
                            mimeType = currentFormat?.mimeType,
                            bufferedPercent = bufferedPercent,
                        ),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = onBackgroundColor.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = onBackgroundColor.copy(alpha = 0.6f),
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Playback controls row ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding - 8.dp)
            ) {
                val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

                // Shuffle
                ResizableIconButton(
                    icon = if (shuffleModeEnabled) R.drawable.shuffle_on else R.drawable.shuffle_off,
                    modifier = Modifier.size(28.dp),
                    color = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else onBackgroundColor.copy(alpha = 0.6f),
                    enabled = playerConnection.player.currentMediaItem != null,
                    onClick = {
                        playerConnection.triggerShuffle()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                )

                // Skip previous
                ResizableIconButton(
                    icon = Icons.Rounded.SkipPrevious,
                    enabled = canSkipPrevious,
                    modifier = Modifier.size(36.dp),
                    color = onBackgroundColor,
                    onClick = {
                        if (playerConnection.player.currentMediaItem == null) queueBoard.setCurrQueue()
                        playerConnection.player.seekToPrevious()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                )

                // Seek back (optional)
                if (seekIncrement != SeekIncrement.OFF) {
                    ResizableIconButton(
                        icon = Icons.Rounded.FastRewind,
                        modifier = Modifier.size(28.dp),
                        color = onBackgroundColor,
                        enabled = playerConnection.player.currentMediaItem != null,
                        onClick = {
                            playerConnection.player.seekTo(
                                playerConnection.player.currentPosition - seekIncrement.millisec
                            )
                        }
                    )
                }

                // ── PLAY / PAUSE pill button (M3 Expressive) ──────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(playButtonWidth)
                        .height(56.dp)
                        .clip(RoundedCornerShape(playPauseRoundness))
                        .background(MaterialTheme.colorScheme.primary)
                        .expressiveClickable(pressedScale = 0.90f) {
                            if (playerConnection.player.currentMediaItem == null) {
                                queueBoard.setCurrQueue()
                                playerConnection.player.togglePlayPause()
                            } else if (playbackState == STATE_ENDED) {
                                playerConnection.player.seekTo(0, 0)
                                playerConnection.player.playWhenReady = true
                            } else {
                                playerConnection.player.togglePlayPause()
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        }
                ) {
                    AnimatedContent(
                        targetState = if (playbackState == STATE_ENDED) 2 else if (isPlaying) 1 else 0,
                        transitionSpec = {
                            fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                        },
                        label = "playPauseIcon"
                    ) { state ->
                        Image(
                            imageVector = when (state) {
                                2 -> Icons.Rounded.Replay
                                1 -> Icons.Rounded.Pause
                                else -> Icons.Rounded.PlayArrow
                            },
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Seek forward (optional)
                if (seekIncrement != SeekIncrement.OFF) {
                    ResizableIconButton(
                        icon = Icons.Rounded.FastForward,
                        modifier = Modifier.size(28.dp),
                        color = onBackgroundColor,
                        enabled = playerConnection.player.currentMediaItem != null,
                        onClick = {
                            playerConnection.player.seekTo(
                                playerConnection.player.currentPosition + seekIncrement.millisec
                            )
                        }
                    )
                }

                // Skip next
                ResizableIconButton(
                    icon = Icons.Rounded.SkipNext,
                    enabled = canSkipNext,
                    modifier = Modifier.size(36.dp),
                    color = onBackgroundColor,
                    onClick = {
                        playerConnection.player.seekToNext()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                )

                // Repeat
                ResizableIconButton(
                    icon = when (repeatMode) {
                        REPEAT_MODE_OFF -> R.drawable.repeat_off
                        REPEAT_MODE_ALL -> R.drawable.repeat_on
                        REPEAT_MODE_ONE -> R.drawable.repeat_one
                        else -> throw IllegalStateException()
                    },
                    modifier = Modifier.size(28.dp),
                    color = if (repeatMode != REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else onBackgroundColor.copy(alpha = 0.6f),
                    enabled = playerConnection.player.currentMediaItem != null,
                    onClick = {
                        playerConnection.player.toggleRepeatMode()
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                )
            }

            // ── Queue hint for landscape ───────────────────────────────────
            if (showQueueHint) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(QueuePeekHeight)
                        .fillMaxWidth()
                        .clickable {
                            queueSheetState.expandSoft()
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        }
                ) {
                    IconButton(onClick = {
                        queueSheetState.expandSoft()
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ExpandLess,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
