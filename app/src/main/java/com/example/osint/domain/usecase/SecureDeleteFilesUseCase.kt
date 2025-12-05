package com.example.osint.domain.usecase

import com.example.osint.data.repository.SecureDeletionRepository
import com.example.osint.domain.model.SecureDeletionMethod
import com.example.osint.domain.model.SecureDeletionProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

class SecureDeleteFilesUseCase(
    private val repository: SecureDeletionRepository
) {
    suspend operator fun invoke(
        files: List<File>,
        method: SecureDeletionMethod
    ): Flow<SecureDeletionProgress> {
        return repository.secureDeleteFiles(files, method)
    }
}
