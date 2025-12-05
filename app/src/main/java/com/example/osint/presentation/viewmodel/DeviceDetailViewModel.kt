package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.DeviceInfo
import com.example.osint.domain.usecase.GetDeviceDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceDetailViewModel(
    private val getDeviceDetailsUseCase: GetDeviceDetailsUseCase,
    private val ipAddress: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceDetailUiState>(DeviceDetailUiState.Loading)
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    init {
        loadDeviceDetails()
    }

    private fun loadDeviceDetails() {
        viewModelScope.launch {
            _uiState.value = DeviceDetailUiState.Loading
            try {
                val device = getDeviceDetailsUseCase(ipAddress)
                if (device != null) {
                    _uiState.value = DeviceDetailUiState.Success(device)
                } else {
                    _uiState.value = DeviceDetailUiState.Error("Device not found")
                }
            } catch (e: Exception) {
                _uiState.value = DeviceDetailUiState.Error(e.message ?: "Failed to load device details")
            }
        }
    }

    fun refresh() {
        loadDeviceDetails()
    }
}

sealed class DeviceDetailUiState {
    object Loading : DeviceDetailUiState()
    data class Success(val device: DeviceInfo) : DeviceDetailUiState()
    data class Error(val message: String) : DeviceDetailUiState()
}
