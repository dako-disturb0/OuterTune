package app.dkdstrb.excitedtune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dkdstrb.excitedtune.constants.LYRIC_FETCH_TIMEOUT
import app.dkdstrb.excitedtune.db.MusicDatabase
import app.dkdstrb.excitedtune.lyrics.LyricsHelper
import app.dkdstrb.excitedtune.lyrics.LyricsResult
import app.dkdstrb.excitedtune.models.MediaMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.akanework.gramophone.logic.utils.SemanticLyrics
import javax.inject.Inject

@HiltViewModel
class LyricsMenuViewModel @Inject constructor(
    private val lyricsHelper: LyricsHelper,
    val database: MusicDatabase,
) : ViewModel() {
    private var job: Job? = null
    val results = MutableStateFlow(emptyList<LyricsResult>())
    val isLoading = MutableStateFlow(false)

    fun search(mediaId: String, title: String, artist: String, duration: Int) {
        isLoading.value = true
        results.value = emptyList()
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            try {
                withTimeoutOrNull(LYRIC_FETCH_TIMEOUT) {
                    lyricsHelper.getAllLyrics(mediaId, title, artist, duration) { result ->
                        results.update {
                            it + result
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Silently ignore fetch errors
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cancelSearch() {
        job?.cancel()
        job = null
    }

    fun refetchLyrics(mediaMetadata: MediaMetadata, onDone: (SemanticLyrics?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Delete from DB so it gets re-fetched from providers
                database.deleteLyricById(mediaMetadata.id)
                withTimeoutOrNull(LYRIC_FETCH_TIMEOUT) {
                    // forceRefresh=true bypasses all caches
                    val lyrics = lyricsHelper.getLyrics(mediaMetadata, forceRefresh = true)
                    onDone(lyrics)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onDone(null)
            }
        }
    }
}
