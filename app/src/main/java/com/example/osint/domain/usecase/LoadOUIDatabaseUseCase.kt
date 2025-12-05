package com.example.osint.domain.usecase

import com.example.osint.data.repository.DeviceRepository

class LoadOUIDatabaseUseCase(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke() {
        deviceRepository.initOUIDatabase()
    }
}
