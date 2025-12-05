package com.example.osint.domain.usecase

import com.example.osint.data.repository.DeviceRepository
import com.example.osint.domain.model.DeviceInfo

class GetScannedDevicesUseCase(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(): List<DeviceInfo> {
        return deviceRepository.getAllDevices()
    }
}
