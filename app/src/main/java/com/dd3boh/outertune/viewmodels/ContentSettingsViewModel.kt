package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.lyrics.LyricsHelper
import com.dd3boh.paxsenix.PaxsenixLyrics
import com.dd3boh.paxsenix.models.PaxsenixStats
import javax.inject.Inject

sealed interface PaxsenixStatsState {
    data object Loading : PaxsenixStatsState
    data class Success(val stats: PaxsenixStats) : PaxsenixStatsState
    data object Error : PaxsenixStatsState
}

@HiltViewModel
class ContentSettingsViewModel @Inject constructor(
    private val lyricsHelper: LyricsHelper,
    private val database: MusicDatabase,
) : ViewModel() {
    private val _paxsenixStatsState = MutableStateFlow<PaxsenixStatsState>(PaxsenixStatsState.Loading)
    val paxsenixStatsState = _paxsenixStatsState.asStateFlow()

    fun fetchPaxsenixStats() {
        _paxsenixStatsState.value = PaxsenixStatsState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            PaxsenixLyrics.getStats()
                .onSuccess { _paxsenixStatsState.value = PaxsenixStatsState.Success(it) }
                .onFailure { _paxsenixStatsState.value = PaxsenixStatsState.Error }
        }
    }

    fun clearLyricsCache() {
        viewModelScope.launch(Dispatchers.IO) {
            lyricsHelper.clearCache()
            database.query {
                clearAllLyrics()
            }
        }
    }
}
