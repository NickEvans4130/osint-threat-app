package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.ThreatResult
import com.example.osint.domain.usecase.ScanUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UrlScannerViewModel(
    private val scanUrlUseCase: ScanUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UrlScannerUiState>(UrlScannerUiState.Initial)
    val uiState: StateFlow<UrlScannerUiState> = _uiState.asStateFlow()

    fun scanUrl(url: String) {
        if (url.isBlank()) {
            _uiState.value = UrlScannerUiState.Error("Please enter a URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = UrlScannerUiState.Loading
            try {
                val result = scanUrlUseCase(url)
                _uiState.value = UrlScannerUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UrlScannerUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = UrlScannerUiState.Initial
    }
}

sealed class UrlScannerUiState {
    object Initial : UrlScannerUiState()
    object Loading : UrlScannerUiState()
    data class Success(val result: ThreatResult) : UrlScannerUiState()
    data class Error(val message: String) : UrlScannerUiState()
}
