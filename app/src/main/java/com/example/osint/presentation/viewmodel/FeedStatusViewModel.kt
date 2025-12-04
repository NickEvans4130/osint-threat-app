package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.FeedInfo
import com.example.osint.domain.usecase.GetFeedStatusUseCase
import com.example.osint.domain.usecase.RefreshFeedsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedStatusViewModel(
    private val getFeedStatusUseCase: GetFeedStatusUseCase,
    private val refreshFeedsUseCase: RefreshFeedsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedStatusUiState>(FeedStatusUiState.Loading)
    val uiState: StateFlow<FeedStatusUiState> = _uiState.asStateFlow()

    init {
        loadFeedStatus()
    }

    fun loadFeedStatus() {
        viewModelScope.launch {
            _uiState.value = FeedStatusUiState.Loading
            try {
                val feeds = getFeedStatusUseCase()
                _uiState.value = FeedStatusUiState.Success(feeds, isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = FeedStatusUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refreshAllFeeds() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is FeedStatusUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            }

            try {
                val result = refreshFeedsUseCase.refreshAllFeeds()
                result.onSuccess {
                    loadFeedStatus()
                }.onFailure { e ->
                    _uiState.value = FeedStatusUiState.Error(e.message ?: "Failed to refresh feeds")
                }
            } catch (e: Exception) {
                _uiState.value = FeedStatusUiState.Error(e.message ?: "Failed to refresh feeds")
            }
        }
    }

    fun refreshFeed(feedSource: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is FeedStatusUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            }

            try {
                val result = refreshFeedsUseCase(feedSource)
                result.onSuccess {
                    loadFeedStatus()
                }.onFailure { e ->
                    _uiState.value = FeedStatusUiState.Error(e.message ?: "Failed to refresh feed")
                }
            } catch (e: Exception) {
                _uiState.value = FeedStatusUiState.Error(e.message ?: "Failed to refresh feed")
            }
        }
    }
}

sealed class FeedStatusUiState {
    object Loading : FeedStatusUiState()
    data class Success(val feeds: List<FeedInfo>, val isRefreshing: Boolean) : FeedStatusUiState()
    data class Error(val message: String) : FeedStatusUiState()
}
