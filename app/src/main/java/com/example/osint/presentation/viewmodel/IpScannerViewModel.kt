package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.ThreatResult
import com.example.osint.domain.usecase.ScanIpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IpScannerViewModel(
    private val scanIpUseCase: ScanIpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<IpScannerUiState>(IpScannerUiState.Initial)
    val uiState: StateFlow<IpScannerUiState> = _uiState.asStateFlow()

    fun scanIp(ip: String) {
        if (ip.isBlank()) {
            _uiState.value = IpScannerUiState.Error("Please enter an IP address")
            return
        }

        viewModelScope.launch {
            _uiState.value = IpScannerUiState.Loading
            try {
                val result = scanIpUseCase(ip)
                _uiState.value = IpScannerUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = IpScannerUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = IpScannerUiState.Initial
    }
}

sealed class IpScannerUiState {
    object Initial : IpScannerUiState()
    object Loading : IpScannerUiState()
    data class Success(val result: ThreatResult) : IpScannerUiState()
    data class Error(val message: String) : IpScannerUiState()
}
