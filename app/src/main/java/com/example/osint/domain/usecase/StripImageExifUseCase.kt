package com.example.osint.domain.usecase

import android.net.Uri
import com.example.osint.data.repository.MetadataRepository

class StripImageExifUseCase(
    private val repository: MetadataRepository
) {
    suspend operator fun invoke(uri: Uri): Uri? {
        return repository.stripImageExif(uri)
    }
}
