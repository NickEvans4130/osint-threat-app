package com.example.osint.domain.usecase

import android.net.Uri
import com.example.osint.data.repository.MetadataRepository
import com.example.osint.domain.model.FileMetadata

class ParseImageMetadataUseCase(
    private val repository: MetadataRepository
) {
    suspend operator fun invoke(uri: Uri): FileMetadata {
        return repository.parseFileMetadata(uri)
    }
}
