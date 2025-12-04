package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.ThreatResult
import com.example.osint.domain.usecase.ScanUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QRScannerViewModel(
    private val scanUrlUseCase: ScanUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<QRScannerUiState>(QRScannerUiState.Scanning)
    val uiState: StateFlow<QRScannerUiState> = _uiState.asStateFlow()

    fun onQRCodeScanned(text: String) {
        viewModelScope.launch {
            _uiState.value = QRScannerUiState.Processing
            try {
                // If it's a URL, scan it
                if (text.startsWith("http://") || text.startsWith("https://")) {
                    val result = scanUrlUseCase(text)
                    _uiState.value = QRScannerUiState.Success(result)
                } else {
                    _uiState.value = QRScannerUiState.TextFound(text)
                }
            } catch (e: Exception) {
                _uiState.value = QRScannerUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = QRScannerUiState.Scanning
    }
}

sealed class QRScannerUiState {
    object Scanning : QRScannerUiState()
    object Processing : QRScannerUiState()
    data class Success(val result: ThreatResult) : QRScannerUiState()
    data class TextFound(val text: String) : QRScannerUiState()
    data class Error(val message: String) : QRScannerUiState()
}
