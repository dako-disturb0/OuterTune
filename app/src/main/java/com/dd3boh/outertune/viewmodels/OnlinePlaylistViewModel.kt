package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.utils.reportException
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    database: MusicDatabase
) : ViewModel() {
    private val playlistId = savedStateHandle.get<String>("playlistId") ?: ""

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val playlistSongs = MutableStateFlow<List<SongItem>>(emptyList())
    var continuation: String? = null
    val dbPlaylist = database.playlistByBrowseId(playlistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isLoading = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            YouTube.playlist(playlistId).onSuccess { page ->
                playlist.value = page.playlist
                playlistSongs.value = page.songs
                continuation = page.continuation
            }.onFailure {
                reportException(it)
            }
            isLoading.value = false
        }
    }

    fun loadMoreSongs() {
        continuation?.let {
            isLoading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    getContinuation(it)
                } finally {
                    isLoading.value = false
                }
            }
        }
    }

    fun loadRemainingSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            try {
                while (continuation != null) {
                    getContinuation(continuation!!)
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    suspend fun getContinuation(continuation: String) {
        val continuationPage = YouTube.playlistContinuation(continuation).getOrElse { e ->
            reportException(e)
            this.continuation = null
            return
        }
        playlistSongs.value = playlistSongs.value + continuationPage.songs
        this.continuation = continuationPage.continuation
    }
}
