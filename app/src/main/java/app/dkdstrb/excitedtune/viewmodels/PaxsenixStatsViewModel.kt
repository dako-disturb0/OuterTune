package app.dkdstrb.excitedtune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.paxsenix.PaxsenixLyrics
import com.dd3boh.paxsenix.models.PaxsenixStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaxsenixStatsViewModel @Inject constructor() : ViewModel() {

    private val _stats = MutableStateFlow<PaxsenixStats?>(null)
    val stats = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun fetchStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            PaxsenixLyrics.getStats()
                .onSuccess { stats ->
                    _stats.value = stats
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to fetch stats"
                }
            _isLoading.value = false
        }
    }
}
