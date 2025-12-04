package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.ThreatResult
import com.example.osint.domain.usecase.ScanHashUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HashScannerViewModel(
    private val scanHashUseCase: ScanHashUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HashScannerUiState>(HashScannerUiState.Initial)
    val uiState: StateFlow<HashScannerUiState> = _uiState.asStateFlow()

    fun scanHash(hash: String) {
        if (hash.isBlank()) {
            _uiState.value = HashScannerUiState.Error("Please enter a hash")
            return
        }

        viewModelScope.launch {
            _uiState.value = HashScannerUiState.Loading
            try {
                val result = scanHashUseCase(hash)
                _uiState.value = HashScannerUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = HashScannerUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = HashScannerUiState.Initial
    }
}

sealed class HashScannerUiState {
    object Initial : HashScannerUiState()
    object Loading : HashScannerUiState()
    data class Success(val result: ThreatResult) : HashScannerUiState()
    data class Error(val message: String) : HashScannerUiState()
}
