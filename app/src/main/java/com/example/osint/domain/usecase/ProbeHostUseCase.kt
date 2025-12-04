package com.example.osint.domain.usecase

import com.example.osint.data.repository.NetworkScannerRepository
import com.example.osint.domain.model.NetworkHost

class ProbeHostUseCase(
    private val repository: NetworkScannerRepository
) {
    suspend operator fun invoke(
        ipAddress: String,
        ports: List<Int> = listOf(80, 443, 22, 3389)
    ): NetworkHost {
        return repository.probeHost(ipAddress, ports)
    }
}
