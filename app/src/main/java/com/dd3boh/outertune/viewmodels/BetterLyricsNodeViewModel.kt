package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.betterlyrics.BetterLyrics
import com.dd3boh.betterlyrics.models.BeteNodeHealthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BetterLyricsNodeViewModel @Inject constructor() : ViewModel() {

    private val _health = MutableStateFlow<BeteNodeHealthResult?>(null)
    val health = _health.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun testNode(nodeUrl: String) {
        if (nodeUrl.isBlank()) {
            _error.value = "Node URL cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            BetterLyrics.checkNodeHealth(nodeUrl)
                .onSuccess { result ->
                    _health.value = result
                }
                .onFailure { e ->
                    _health.value = null
                    _error.value = e.message ?: "Failed to connect to node"
                }
            _isLoading.value = false
        }
    }
}
