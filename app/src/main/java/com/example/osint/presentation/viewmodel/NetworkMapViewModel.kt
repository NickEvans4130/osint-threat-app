package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.DeviceInfo
import com.example.osint.domain.usecase.GetScannedDevicesUseCase
import com.example.osint.domain.usecase.LoadOUIDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkMapViewModel(
    private val getScannedDevicesUseCase: GetScannedDevicesUseCase,
    private val loadOUIDatabaseUseCase: LoadOUIDatabaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkMapUiState>(NetworkMapUiState.Loading)
    val uiState: StateFlow<NetworkMapUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = NetworkMapUiState.Loading
            try {
                loadOUIDatabaseUseCase()
                val devices = getScannedDevicesUseCase()
                if (devices.isEmpty()) {
                    _uiState.value = NetworkMapUiState.Empty
                } else {
                    _uiState.value = NetworkMapUiState.Success(devices)
                }
            } catch (e: Exception) {
                _uiState.value = NetworkMapUiState.Error(e.message ?: "Failed to load devices")
            }
        }
    }
}

sealed class NetworkMapUiState {
    object Loading : NetworkMapUiState()
    object Empty : NetworkMapUiState()
    data class Success(val devices: List<DeviceInfo>) : NetworkMapUiState()
    data class Error(val message: String) : NetworkMapUiState()
}
