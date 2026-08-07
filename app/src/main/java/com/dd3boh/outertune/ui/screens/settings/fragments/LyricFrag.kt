/*
 * Copyright (C) 2025 O‌ute‌rTu‌ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */
package com.dd3boh.outertune.ui.screens.settings.fragments

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TextRotationAngledown
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.EnableBetterLyricsKey
import com.dd3boh.outertune.constants.EnableKugouKey
import com.dd3boh.outertune.constants.EnableLrcLibKey
import com.dd3boh.outertune.constants.EnablePaxsenixKey
import com.dd3boh.outertune.constants.EnableSimpMusicKey
import com.dd3boh.outertune.constants.LyricClickable
import com.dd3boh.outertune.constants.LyricFontSizeKey
import com.dd3boh.outertune.constants.LyricKaraokeEnable
import com.dd3boh.outertune.constants.LyricSourcePrefKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.LyricUpdateSpeed
import com.dd3boh.outertune.constants.LyricsPosition
import com.dd3boh.outertune.constants.LyricsProviderOrderKey
import com.dd3boh.outertune.constants.LyricsTextPositionKey
import com.dd3boh.outertune.constants.MiniPlayerLyricModeKey
import com.dd3boh.outertune.constants.MultilineLrcKey
import com.dd3boh.outertune.constants.Speed
import com.dd3boh.outertune.extensions.move
import com.dd3boh.outertune.lyrics.LyricsHelper
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.ListPreference
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.dialog.CounterDialog
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.dd3boh.outertune.viewmodels.PaxsenixStatsViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ColumnScope.LyricFormatFrag() {
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(
        LyricsTextPositionKey,
        defaultValue = LyricsPosition.CENTER
    )
    val (lyricFontSize, onLyricFontSizeChange) = rememberPreference(LyricFontSizeKey, defaultValue = 20)
    val (showLyricInMiniPlayer, onShowLyricInMiniPlayerChange) = rememberPreference(
        com.dd3boh.outertune.constants.ShowLyricInMiniPlayerKey,
        defaultValue = true
    )
    val (miniPlayerLyricMode, onMiniPlayerLyricModeChange) = rememberPreference(
        MiniPlayerLyricModeKey,
        defaultValue = "static"
    )

    var showFontSizeDialog by remember {
        mutableStateOf(false)
    }

    SwitchPreference(
        title = { Text(stringResource(R.string.lyrics_in_miniplayer_title)) },
        description = stringResource(R.string.lyrics_in_miniplayer_description),
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        checked = showLyricInMiniPlayer,
        onCheckedChange = onShowLyricInMiniPlayerChange
    )

    AnimatedVisibility(
        visible = showLyricInMiniPlayer,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        ListPreference(
            title = { Text(stringResource(R.string.miniplayer_lyric_mode_title)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            selectedValue = miniPlayerLyricMode,
            values = listOf("static", "dynamic"),
            valueText = { mode ->
                when (mode) {
                    "dynamic" -> stringResource(R.string.miniplayer_lyric_mode_dynamic)
                    else      -> stringResource(R.string.miniplayer_lyric_mode_static)
                }
            },
            onValueSelected = onMiniPlayerLyricModeChange
        )
    }

    EnumListPreference(
        title = { Text(stringResource(R.string.lyrics_text_position)) },
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        selectedValue = lyricsPosition,
        onValueSelected = onLyricsPositionChange,
        valueText = {
            when (it) {
                LyricsPosition.LEFT -> stringResource(R.string.left)
                LyricsPosition.CENTER -> stringResource(R.string.center)
                LyricsPosition.RIGHT -> stringResource(R.string.right)
            }
        }
    )
    PreferenceEntry(
        title = { Text(stringResource(R.string.lyrics_font_Size)) },
        description = "$lyricFontSize sp",
        icon = { Icon(Icons.Rounded.TextFields, null) },
        onClick = { showFontSizeDialog = true }
    )


    /**
     * ---------------------------
     * Dialogs
     * ---------------------------
     */


    if (showFontSizeDialog) {
        CounterDialog(
            title = stringResource(R.string.lyrics_font_Size),
            initialValue = lyricFontSize,
            upperBound = 32,
            lowerBound = 8,
            unitDisplay = " pt",
            onDismiss = { showFontSizeDialog = false },
            onConfirm = {
                onLyricFontSizeChange(it)
                showFontSizeDialog = false
            },
            onReset = { onLyricFontSizeChange(20) },
            onCancel = { showFontSizeDialog = false }
        )
    }
}


@Composable
fun ColumnScope.LyricParserFrag() {
    val (multilineLrc, onMultilineLrcChange) = rememberPreference(MultilineLrcKey, defaultValue = true)
    val (lyricTrim, onLyricTrimChange) = rememberPreference(LyricTrimKey, defaultValue = false)

    // multiline lyrics
    SwitchPreference(
        title = { Text(stringResource(R.string.lyrics_multiline_title)) },
        description = stringResource(R.string.lyrics_multiline_description),
        icon = { Icon(Icons.AutoMirrored.Rounded.Sort, null) },
        checked = multilineLrc,
        onCheckedChange = onMultilineLrcChange
    )

    // trim (remove spaces around) lyrics
    SwitchPreference(
        title = { Text(stringResource(R.string.lyrics_trim_title)) },
        icon = { Icon(Icons.Rounded.ContentCut, null) },
        checked = lyricTrim,
        onCheckedChange = onLyricTrimChange
    )
}

/**
 * Provider item data for the drag-and-drop priority list
 */
private data class ProviderItem(
    val name: String,
    val enabledKey: String,
    var enabled: Boolean,
)

@Composable
fun ColumnScope.LyricSourceFrag() {
    val (enableBetterLyrics, onEnableBetterLyricsChange) = rememberPreference(key = EnableBetterLyricsKey, defaultValue = true)
    val (enablePaxsenix, onEnablePaxsenixChange) = rememberPreference(key = EnablePaxsenixKey, defaultValue = true)
    val (enableSimpMusic, onEnableSimpMusicChange) = rememberPreference(key = EnableSimpMusicKey, defaultValue = true)
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)
    val (enableLrcLib, onEnableLrcLibChange) = rememberPreference(key = EnableLrcLibKey, defaultValue = true)
    val (preferLocalLyric, onPreferLocalLyric) = rememberPreference(LyricSourcePrefKey, defaultValue = true)
    val (providerOrder, onProviderOrderChange) = rememberPreference(
        LyricsProviderOrderKey,
        defaultValue = LyricsHelper.DEFAULT_PROVIDER_ORDER.joinToString(",")
    )

    // Build ordered provider list from saved preference
    val defaultOrder = LyricsHelper.DEFAULT_PROVIDER_ORDER
    val savedOrder = remember(providerOrder) {
        if (providerOrder.isBlank()) defaultOrder
        else {
            val orderList = providerOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val remaining = defaultOrder.filter { it !in orderList }
            orderList + remaining
        }
    }

    val providerItems = remember(savedOrder, enableBetterLyrics, enablePaxsenix, enableSimpMusic, enableKugou, enableLrcLib) {
        mutableStateListOf(*savedOrder.map { name ->
            ProviderItem(
                name = name,
                enabledKey = name,
                enabled = when (name) {
                    "Paxsenix (Apple Music)" -> enablePaxsenix
                    "BetterLyrics" -> enableBetterLyrics
                    "SimpMusic" -> enableSimpMusic
                    "Kugou" -> enableKugou
                    "LrcLib" -> enableLrcLib
                    else -> true
                }
            )
        }.toTypedArray())
    }

    // Drag-and-drop state
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        providerItems.move(from.index, to.index)
        onProviderOrderChange(providerItems.joinToString(",") { it.name })
    }

    // Paxsenix API Stats section
    var showStats by rememberSaveable { mutableStateOf(false) }

    // Priority ordering header
    Text(
        text = stringResource(R.string.lyrics_provider_priority),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    Text(
        text = stringResource(R.string.lyrics_provider_priority_desc),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    // Drag-and-drop provider list - using LazyColumn with fixed height
    val listHeight = (providerItems.size * 68).dp
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .height(listHeight)
    ) {
        itemsIndexed(
            items = providerItems,
            key = { _, item -> item.name }
        ) { index, item ->
            ReorderableItem(
                state = reorderableLazyListState,
                key = item.name,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Drag handle
                        Icon(
                            imageVector = Icons.Rounded.DragHandle,
                            contentDescription = stringResource(R.string.drag_to_reorder),
                            modifier = Modifier
                                .draggableHandle()
                                .padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Provider info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "#${index + 1} ${stringResource(R.string.priority)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Enabled indicator
                        if (item.enabled) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Toggle switches (in original order)
    SwitchPreference(
        title = { Text(stringResource(R.string.enable_paxsenix)) },
        description = stringResource(R.string.enable_paxsenix_description),
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        checked = enablePaxsenix,
        onCheckedChange = onEnablePaxsenixChange
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.enable_betterlyrics)) },
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        checked = enableBetterLyrics,
        onCheckedChange = onEnableBetterLyricsChange
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.enable_simpmusic_lyrics)) },
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        checked = enableSimpMusic,
        onCheckedChange = onEnableSimpMusicChange
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.enable_lrclib)) },
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        checked = enableLrcLib,
        onCheckedChange = onEnableLrcLibChange
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.enable_kugou)) },
        icon = { Icon(Icons.Rounded.Lyrics, null) },
        checked = enableKugou,
        onCheckedChange = onEnableKugouChange
    )
    // prioritize local lyric files over all cloud providers
    SwitchPreference(
        title = { Text(stringResource(R.string.lyrics_prefer_local)) },
        description = stringResource(R.string.lyrics_prefer_local_description),
        icon = { Icon(Icons.Rounded.ContentCut, null) },
        checked = preferLocalLyric,
        onCheckedChange = onPreferLocalLyric
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Paxsenix API Status section
    PreferenceEntry(
        title = { Text(stringResource(R.string.paxsenix_api_status)) },
        description = stringResource(R.string.paxsenix_api_status_desc),
        icon = { Icon(Icons.Rounded.Info, null) },
        onClick = { showStats = !showStats }
    )

    AnimatedVisibility(
        visible = showStats,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        PaxsenixStatsCard()
    }
}

@Composable
private fun PaxsenixStatsCard(
    viewModel: PaxsenixStatsViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStats()
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.paxsenix_api_status),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.material3.IconButton(onClick = { viewModel.fetchStats() }) {
                    AnimatedContent(targetState = isLoading, label = "refresh") { loading ->
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.refresh))
                        }
                    }
                }
            }

            when {
                error != null -> {
                    Text(
                        text = error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                stats != null -> {
                    val s = stats!!

                    // Overall success rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.overall_success_rate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = s.overallSuccessRate,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Overall progress bar
                    val successPct = s.overallSuccessRate.replace("%", "").trim().toFloatOrNull()?.div(100f) ?: 0f
                    LinearProgressIndicator(
                        progress = { successPct },
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            successPct >= 0.8f -> MaterialTheme.colorScheme.primary
                            successPct >= 0.5f -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )

                    // Stats summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(label = stringResource(R.string.stat_total), value = "${s.totalRequests}")
                        StatChip(label = stringResource(R.string.stat_success), value = "${s.successfulRequests}")
                        StatChip(label = stringResource(R.string.stat_failed), value = "${s.failedRequests}")
                    }

                    // Per-provider stats
                    if (s.providers.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.providers_breakdown),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        s.providers.entries.sortedByDescending {
                            it.value.successRate.replace("%", "").trim().toFloatOrNull() ?: 0f
                        }.forEach { (providerName, providerStats) ->
                            val pct = providerStats.successRate.replace("%", "").trim().toFloatOrNull()?.div(100f) ?: 0f
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = providerName.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${providerStats.hits} hits · ${providerStats.successRate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = when {
                                        pct >= 0.8f -> MaterialTheme.colorScheme.primary
                                        pct >= 0.5f -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }
                }

                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColumnScope.LyricAdvancedFrag() {
    val (lyricUpdateSpeed, onLyricsUpdateSpeedChange) = rememberEnumPreference(LyricUpdateSpeed, Speed.MEDIUM)
    val (lyricsFancy, onLyricsFancyChange) = rememberPreference(LyricKaraokeEnable, false)
    val (syncedLyricsClickable, onSyncedLyricsClickable) = rememberPreference(LyricClickable, defaultValue = true)
    val (preloadKaraokeLyrics, onPreloadKaraokeLyricsChange) = rememberPreference(
        com.dd3boh.outertune.constants.PreloadKaraokeLyricsKey,
        defaultValue = false
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        // clickable lyrics
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_synced_clickable)) },
            icon = { Icon(Icons.Rounded.TouchApp, null) },
            checked = syncedLyricsClickable,
            onCheckedChange = onSyncedLyricsClickable
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.preload_karaoke_lyrics_title)) },
            description = stringResource(R.string.preload_karaoke_lyrics_description),
            icon = { Icon(Icons.Rounded.Refresh, null) },
            checked = preloadKaraokeLyrics,
            onCheckedChange = onPreloadKaraokeLyricsChange
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_karaoke_title)) },
            description = stringResource(R.string.lyrics_karaoke_description),
            icon = { Icon(Icons.Rounded.TextRotationAngledown, null) },
            checked = lyricsFancy,
            onCheckedChange = onLyricsFancyChange
        )

        ListPreference(
            title = { Text(stringResource(R.string.lyrics_karaoke_hz_title)) },
            icon = { Icon(Icons.Rounded.Speed, null) },
            selectedValue = lyricUpdateSpeed,
            onValueSelected = onLyricsUpdateSpeedChange,
            values = Speed.entries,
            valueText = {
                when (it) {
                    Speed.SLOW -> stringResource(R.string.speed_slow)
                    Speed.MEDIUM -> stringResource(R.string.speed_medium)
                    Speed.FAST -> stringResource(R.string.speed_fast)
                }
            },
            isEnabled = lyricsFancy
        )
    }
}