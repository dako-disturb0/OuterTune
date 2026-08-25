/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings.fragments

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
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
import androidx.compose.ui.graphics.toArgb
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
import com.dd3boh.outertune.constants.PlayerCustomColorPaletteKey
import com.dd3boh.outertune.constants.PlayerThumbnailCrop
import com.dd3boh.outertune.constants.PlayerThumbnailCropKey
import com.dd3boh.outertune.constants.PlayerThumbnailRoundnessKey
import com.dd3boh.outertune.constants.PlayerThumbnailSizeKey
import com.dd3boh.outertune.constants.PlayerTimelineSizeKey
import com.dd3boh.outertune.constants.PlayerTimelineType
import com.dd3boh.outertune.constants.PlayerTimelineTypeKey
import com.dd3boh.outertune.constants.PureBlackKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.PlayerSliderTrack
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
        defaultValue = PlayerBackgroundStyle.FOLLOW_ARTWORK
    )
    val (timelineType, onTimelineTypeChange) = rememberEnumPreference(
        key = PlayerTimelineTypeKey,
        defaultValue = PlayerTimelineType.PIN_BAR
    )
    val (timelineSize, onTimelineSizeChange) = rememberPreference(
        key = PlayerTimelineSizeKey,
        defaultValue = 10
    )
    val (thumbnailSize, onThumbnailSizeChange) = rememberPreference(
        key = PlayerThumbnailSizeKey,
        defaultValue = 0.85f
    )
    val (thumbnailRoundness, onThumbnailRoundnessChange) = rememberPreference(
        key = PlayerThumbnailRoundnessKey,
        defaultValue = 16
    )
    val (thumbnailCrop, onThumbnailCropChange) = rememberEnumPreference(
        key = PlayerThumbnailCropKey,
        defaultValue = PlayerThumbnailCrop.ORIGINAL
    )
    val (customColorPalette, onCustomColorPaletteChange) = rememberPreference(
        key = PlayerCustomColorPaletteKey,
        defaultValue = "#6750A4"
    )

    // 1. Live Interactive Visual Preview Component ("Visualisasi Contoh Ukurannya")
    LiveVisualPreviewCard(
        playerBackground = playerBackground,
        customColorPalette = customColorPalette,
        thumbnailSize = thumbnailSize,
        thumbnailRoundness = thumbnailRoundness,
        thumbnailCrop = thumbnailCrop,
        timelineType = timelineType,
        timelineSize = timelineSize
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Player Background Style & Custom Color Palette Card
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
                it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            }

            EnumListPreference(
                title = { Text(stringResource(R.string.player_background_style)) },
                icon = { Icon(Icons.Rounded.BlurOn, null) },
                selectedValue = playerBackground,
                onValueSelected = onPlayerBackgroundChange,
                valueText = {
                    when (it) {
                        PlayerBackgroundStyle.FOLLOW_ARTWORK -> stringResource(R.string.player_background_follow_artwork)
                        PlayerBackgroundStyle.FOLLOW_THEME -> stringResource(R.string.player_background_follow_theme)
                        PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.player_background_gradient)
                        PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                        PlayerBackgroundStyle.COLOR_PALETTE -> stringResource(R.string.player_background_color_palette)
                        PlayerBackgroundStyle.ANIMATED_GRADIENT -> stringResource(R.string.player_background_animated_gradient)
                        PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.player_background_original)
                    }
                },
                values = availableBackgroundStyles
            )

            // Custom Color Palette Picker when COLOR_PALETTE is selected
            AnimatedVisibility(visible = playerBackground == PlayerBackgroundStyle.COLOR_PALETTE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.color_palette_picker_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val presetColors = listOf(
                        "#6750A4" to "Purple",
                        "#3F51B5" to "Indigo",
                        "#0288D1" to "Blue",
                        "#00838F" to "Cyan",
                        "#2E7D32" to "Green",
                        "#F57F17" to "Amber",
                        "#E65100" to "Orange",
                        "#C2185B" to "Rose",
                        "#37474F" to "Slate",
                        "#121212" to "Dark"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetColors.forEach { (hex, _) ->
                            val color = parseHexColor(hex)
                            val isSelected = customColorPalette.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { onCustomColorPaletteChange(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (color.isDark()) Color.White else Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    var hexInputState by remember(customColorPalette) {
                        mutableStateOf(customColorPalette)
                    }
                    var isHexError by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = hexInputState,
                        onValueChange = { input ->
                            hexInputState = input
                            if (isValidHexColor(input)) {
                                isHexError = false
                                onCustomColorPaletteChange(input)
                            } else {
                                isHexError = true
                            }
                        },
                        label = { Text(stringResource(R.string.color_palette_hex_input)) },
                        isError = isHexError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Thumbnail Customization Card (Size, Roundness Slider & Manual Text Input, Crop Selector)
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

            // Thumbnail Crop Selector (Segmented Buttons)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.AspectRatio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.crop_video_thumbnail),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PlayerThumbnailCrop.entries.forEachIndexed { index, crop ->
                        SegmentedButton(
                            selected = thumbnailCrop == crop,
                            onClick = { onThumbnailCropChange(crop) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = PlayerThumbnailCrop.entries.size
                            )
                        ) {
                            Text(
                                when (crop) {
                                    PlayerThumbnailCrop.ORIGINAL -> stringResource(R.string.crop_thumbnail_original)
                                    PlayerThumbnailCrop.ROUND -> stringResource(R.string.crop_thumbnail_round)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Timeline Customization Card (Type & Size Selectors)
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

            // Timeline Type Selector (Segmented Buttons)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                PlayerTimelineType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = timelineType == type,
                        onClick = { onTimelineTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = PlayerTimelineType.entries.size
                        )
                    ) {
                        Text(
                            text = when (type) {
                                PlayerTimelineType.PIN_BAR -> stringResource(R.string.timeline_type_pin_bar)
                                PlayerTimelineType.WAVY -> stringResource(R.string.timeline_type_wavy)
                                PlayerTimelineType.FAT_BAR -> stringResource(R.string.timeline_type_fat_bar)
                                PlayerTimelineType.DYNAMIC_BAR -> stringResource(R.string.timeline_type_dynamic_bar)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Player Timeline Size Slider (4dp to 24dp)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.player_timeline_size),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${timelineSize.coerceIn(4, 24)} dp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = timelineSize.coerceIn(4, 24).toFloat(),
                    onValueChange = { onTimelineSizeChange(it.toInt()) },
                    valueRange = 4f..24f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Live Interactive Preview Component ("Visualisasi Contoh Ukurannya")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveVisualPreviewCard(
    playerBackground: PlayerBackgroundStyle,
    customColorPalette: String,
    thumbnailSize: Float,
    thumbnailRoundness: Int,
    thumbnailCrop: PlayerThumbnailCrop,
    timelineType: PlayerTimelineType,
    timelineSize: Int,
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
                PlayerBackgroundStyle.FOLLOW_ARTWORK -> Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF38006B),
                            Color(0xFF0039CB),
                            Color(0xFF004D40)
                        )
                    )
                )
                PlayerBackgroundStyle.FOLLOW_THEME -> Modifier.background(
                    MaterialTheme.colorScheme.surfaceContainerHigh
                )
                PlayerBackgroundStyle.GRADIENT -> Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6B1B9A),
                            Color(0xFF1565C0)
                        )
                    )
                )
                PlayerBackgroundStyle.BLUR -> Modifier.background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF5E35B1).copy(alpha = 0.85f),
                            Color(0xFF121212)
                        )
                    )
                )
                PlayerBackgroundStyle.COLOR_PALETTE -> Modifier.background(
                    parseHexColor(customColorPalette)
                )
                PlayerBackgroundStyle.ANIMATED_GRADIENT -> Modifier.background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF8E24AA),
                            Color(0xFF00ACC1),
                            Color(0xFF43A047),
                            Color(0xFF8E24AA)
                        )
                    )
                )
                PlayerBackgroundStyle.DEFAULT -> Modifier.background(
                    MaterialTheme.colorScheme.surfaceContainer
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
                    val thumbnailShape = if (thumbnailCrop == PlayerThumbnailCrop.ROUND) {
                        CircleShape
                    } else {
                        RoundedCornerShape(thumbnailRoundness.coerceIn(0, 100).dp)
                    }

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

                    // Mock Timeline Track Slider
                    val mockSliderState = remember { SliderState(value = 0.45f) }
                    PlayerSliderTrack(
                        sliderState = mockSliderState,
                        timelineType = timelineType,
                        trackHeight = timelineSize.coerceIn(4, 24).dp,
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

// Utility functions for Color Palette parsing & luminance check
private fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF6750A4)): Color {
    return try {
        val cleanHex = hex.trim().removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> android.graphics.Color.parseColor("#FF$cleanHex")
            8 -> android.graphics.Color.parseColor("#$cleanHex")
            else -> defaultColor.toArgb()
        }
        Color(colorInt)
    } catch (_: Exception) {
        defaultColor
    }
}

private fun isValidHexColor(hex: String): Boolean {
    val cleanHex = hex.trim().removePrefix("#")
    return (cleanHex.length == 6 || cleanHex.length == 8) && cleanHex.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
}

private fun Color.isDark(): Boolean {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return luminance < 0.5f
}
