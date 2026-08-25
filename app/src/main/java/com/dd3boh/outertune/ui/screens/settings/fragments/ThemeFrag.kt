/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DEFAULT_PLAYER_BACKGROUND
import com.dd3boh.outertune.constants.DarkMode
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.DynamicThemeKey
import com.dd3boh.outertune.constants.AuroraThemeKey
import com.dd3boh.outertune.constants.HighContrastKey
import com.dd3boh.outertune.constants.PlayerBackgroundStyle
import com.dd3boh.outertune.constants.PlayerBackgroundStyleKey
import com.dd3boh.outertune.constants.PlayerThumbnailAutoCropKey
import com.dd3boh.outertune.constants.PlayerThumbnailRoundnessKey
import com.dd3boh.outertune.constants.PlayerThumbnailSizeKey
import com.dd3boh.outertune.constants.PlayerTimelineType
import com.dd3boh.outertune.constants.PureBlackKey
import com.dd3boh.outertune.constants.ShowInlineInfoKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference

@Composable
fun ColumnScope.ThemeAppFrag() {
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (highContrastCompat, onHccChange) = rememberPreference(HighContrastKey, defaultValue = false)
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (auroraTheme, onAuroraThemeChange) = rememberPreference(AuroraThemeKey, defaultValue = false)

    SwitchPreference(
        title = { Text(stringResource(R.string.aurora_theme)) },
        description = stringResource(R.string.aurora_theme_description),
        icon = { Icon(Icons.Rounded.AutoAwesome, null) },
        checked = auroraTheme,
        onCheckedChange = onAuroraThemeChange
    )

    SwitchPreference(
        title = { Text(stringResource(R.string.enable_dynamic_theme)) },
        icon = { Icon(Icons.Rounded.Palette, null) },
        checked = dynamicTheme,
        onCheckedChange = onDynamicThemeChange
    )
    AnimatedVisibility(!dynamicTheme) {
        SwitchPreference(
            title = { Text(stringResource(R.string.high_contrast)) },
            description = stringResource(R.string.high_contrast_description),
            icon = { Icon(Icons.Rounded.Contrast, null) },
            checked = highContrastCompat,
            onCheckedChange = onHccChange
        )
    }
    EnumListPreference(
        title = { Text(stringResource(R.string.dark_theme)) },
        icon = { Icon(Icons.Rounded.DarkMode, null) },
        selectedValue = darkMode,
        onValueSelected = onDarkModeChange,
        valueText = {
            when (it) {
                DarkMode.ON -> stringResource(R.string.dark_theme_on)
                DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
            }
        }
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.pure_black)) },
        icon = { Icon(Icons.Rounded.Contrast, null) },
        checked = pureBlack,
        onCheckedChange = onPureBlackChange
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.ThemePlayerFrag() {
    PlayerCustomizationFrag()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.PlayerCustomizationFrag() {
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = DEFAULT_PLAYER_BACKGROUND
    )
    val (showInlineInfo, onShowInlineInfoChange) = rememberPreference(
        key = ShowInlineInfoKey,
        defaultValue = false
    )
    val (thumbnailSize, onThumbnailSizeChange) = rememberPreference(
        key = PlayerThumbnailSizeKey,
        defaultValue = 0.85f
    )
    val (thumbnailRoundness, onThumbnailRoundnessChange) = rememberPreference(
        key = PlayerThumbnailRoundnessKey,
        defaultValue = 16
    )
    val (thumbnailAutoCrop, onThumbnailAutoCropChange) = rememberPreference(
        key = PlayerThumbnailAutoCropKey,
        defaultValue = false
    )

    // 1. Live Interactive Visual Preview Component ("Visualisasi Contoh Ukurannya")
    LiveVisualPreviewCard(
        playerBackground = playerBackground,
        thumbnailSize = thumbnailSize,
        thumbnailRoundness = thumbnailRoundness,
        thumbnailAutoCrop = thumbnailAutoCrop
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Player Background Style Card
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            EnumListPreference(
                title = { Text(stringResource(R.string.player_background_style)) },
                icon = { Icon(Icons.Rounded.BlurOn, null) },
                selectedValue = playerBackground,
                onValueSelected = onPlayerBackgroundChange,
                valueText = {
                    when (it) {
                        PlayerBackgroundStyle.DYNAMIC_LIGHT -> stringResource(R.string.player_background_dynamic_light)
                        PlayerBackgroundStyle.COLOR_PALETTE -> stringResource(R.string.player_background_from_image)
                    }
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Thumbnail Customization Card (Size, Roundness Slider & Manual Text Input, Auto-Crop)
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.thumbnail_size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Thumbnail Size Slider (50% to 100% / 0.5f to 1.0f)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LinearScale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.thumbnail_size),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${(thumbnailSize * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = thumbnailSize.coerceIn(0.5f, 1.0f),
                    onValueChange = { onThumbnailSizeChange(it) },
                    valueRange = 0.5f..1.0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Thumbnail Roundness Controls (Slider + Manual OutlinedTextField)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.RoundedCorner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.thumbnail_roundness),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${thumbnailRoundness.coerceIn(0, 100)} dp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Slider(
                        value = thumbnailRoundness.coerceIn(0, 100).toFloat(),
                        onValueChange = { onThumbnailRoundnessChange(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f)
                    )

                    var roundnessText by remember(thumbnailRoundness) {
                        mutableStateOf(thumbnailRoundness.toString())
                    }
                    var isRoundnessError by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = roundnessText,
                        onValueChange = { input ->
                            roundnessText = input
                            val parsed = input.toIntOrNull()
                            if (parsed != null && parsed in 0..100) {
                                isRoundnessError = false
                                onThumbnailRoundnessChange(parsed)
                            } else {
                                isRoundnessError = true
                            }
                        },
                        label = { Text("dp") },
                        isError = isRoundnessError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(90.dp)
                    )
                }
            }

            // Auto-Crop Thumbnail (optional)
            SwitchPreference(
                title = { Text(stringResource(R.string.auto_crop_thumbnail)) },
                description = stringResource(R.string.auto_crop_thumbnail_description),
                icon = { Icon(Icons.Rounded.AspectRatio, null) },
                checked = thumbnailAutoCrop,
                onCheckedChange = onThumbnailAutoCropChange
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Timeline & Playback Info Card
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.player_timeline_type),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Timeline Style Selector (dropdown so labels are never clipped)
            EnumListPreference(
                title = { Text(stringResource(R.string.player_timeline_type)) },
                icon = { Icon(Icons.Rounded.Timeline, null) },
                selectedValue = PlayerTimelineType.DEFAULT,
                onValueSelected = { },
                valueText = {
                    when (it) {
                        PlayerTimelineType.DEFAULT -> stringResource(R.string.timeline_type_default)
                    }
                }
            )

            // Show Inline Playback Info
            SwitchPreference(
                title = { Text(stringResource(R.string.show_inline_info)) },
                description = stringResource(R.string.show_inline_info_description),
                icon = { Icon(Icons.Rounded.Info, null) },
                checked = showInlineInfo,
                onCheckedChange = onShowInlineInfoChange
            )
        }
    }
}

// Live Interactive Preview Component ("Visualisasi Contoh Ukurannya")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveVisualPreviewCard(
    playerBackground: PlayerBackgroundStyle,
    thumbnailSize: Float,
    thumbnailRoundness: Int,
    thumbnailAutoCrop: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RemoveRedEye,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.live_visual_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val previewBgModifier = when (playerBackground) {
                PlayerBackgroundStyle.DYNAMIC_LIGHT -> Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5E48B8),
                            Color(0xFF3A2D78)
                        )
                    )
                )
                PlayerBackgroundStyle.COLOR_PALETTE -> Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B1B9A),
                            Color(0xFF1565C0),
                            Color(0xFF004D40)
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .then(previewBgModifier)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mock Album Art
                    val thumbnailShape = RoundedCornerShape(thumbnailRoundness.coerceIn(0, 100).dp)

                    val albumArtDimension = (160 * thumbnailSize.coerceIn(0.5f, 1.0f)).dp

                    Box(
                        modifier = Modifier
                            .size(albumArtDimension)
                            .clip(thumbnailShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF7E57C2),
                                        Color(0xFF26A69A)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(albumArtDimension * 0.4f)
                        )
                    }

                    // Mock Song Metadata
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "OuterTune Player",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Material 3 Expressive UI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Mock Timeline Track Slider (Material 3 default style)
                    Slider(
                        value = 0.45f,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    // Mock Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.SkipPrevious,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Rounded.Repeat,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }
    }
}

