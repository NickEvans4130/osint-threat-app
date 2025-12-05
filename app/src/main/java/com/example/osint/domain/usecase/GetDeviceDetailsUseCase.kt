package com.example.osint.domain.usecase

import com.example.osint.data.repository.DeviceRepository
import com.example.osint.domain.model.DeviceInfo

class GetDeviceDetailsUseCase(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(ipAddress: String): DeviceInfo? {
        return deviceRepository.getDeviceByIp(ipAddress)
    }
}
