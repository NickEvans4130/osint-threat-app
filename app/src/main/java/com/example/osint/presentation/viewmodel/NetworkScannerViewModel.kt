package com.example.osint.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.NetworkHost
import com.example.osint.domain.model.NetworkScanProgress
import com.example.osint.domain.model.SubnetInfo
import com.example.osint.domain.usecase.ScanLocalNetworkUseCase
import com.example.osint.data.repository.NetworkScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NetworkScannerViewModel(
    private val scanLocalNetworkUseCase: ScanLocalNetworkUseCase,
    private val repository: NetworkScannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkScannerUiState>(NetworkScannerUiState.Initial)
    val uiState: StateFlow<NetworkScannerUiState> = _uiState.asStateFlow()

    private val _selectedHost = MutableStateFlow<NetworkHost?>(null)
    val selectedHost: StateFlow<NetworkHost?> = _selectedHost.asStateFlow()

    private var currentSubnetInfo: SubnetInfo? = null

    init {
        loadSubnetInfo()
    }

    fun loadSubnetInfo() {
        viewModelScope.launch {
            try {
                val subnetInfo = repository.getCurrentSubnet()
                if (subnetInfo != null) {
                    currentSubnetInfo = subnetInfo
                    _uiState.value = NetworkScannerUiState.Ready(subnetInfo)
                } else {
                    _uiState.value = NetworkScannerUiState.Error("Unable to detect network. Make sure Wi-Fi is enabled.")
                }
            } catch (e: Exception) {
                _uiState.value = NetworkScannerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun startScan(ports: List<Int> = listOf(80, 443, 22, 3389)) {
        if (currentSubnetInfo == null) {
            _uiState.value = NetworkScannerUiState.Error("Network information not available")
            return
        }

        viewModelScope.launch {
            _uiState.value = NetworkScannerUiState.Scanning(
                NetworkScanProgress(
                    totalHosts = 0,
                    scannedHosts = 0,
                    currentIp = null,
                    foundHosts = emptyList()
                ),
                currentSubnetInfo!!
            )

            scanLocalNetworkUseCase(ports)
                .catch { e ->
                    _uiState.value = NetworkScannerUiState.Error(e.message ?: "Scan failed")
                }
                .collect { progress ->
                    _uiState.value = NetworkScannerUiState.Scanning(progress, currentSubnetInfo!!)

                    if (progress.scannedHosts == progress.totalHosts && progress.totalHosts > 0) {
                        _uiState.value = NetworkScannerUiState.Complete(
                            progress.foundHosts,
                            currentSubnetInfo!!
                        )
                    }
                }
        }
    }

    fun selectHost(host: NetworkHost?) {
        _selectedHost.value = host
    }

    fun reset() {
        _selectedHost.value = null
        loadSubnetInfo()
    }
}

sealed class NetworkScannerUiState {
    object Initial : NetworkScannerUiState()
    data class Ready(val subnetInfo: SubnetInfo) : NetworkScannerUiState()
    data class Scanning(
        val progress: NetworkScanProgress,
        val subnetInfo: SubnetInfo
    ) : NetworkScannerUiState()
    data class Complete(
        val hosts: List<NetworkHost>,
        val subnetInfo: SubnetInfo
    ) : NetworkScannerUiState()
    data class Error(val message: String) : NetworkScannerUiState()
}
