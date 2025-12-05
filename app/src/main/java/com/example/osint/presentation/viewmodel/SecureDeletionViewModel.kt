package com.example.osint.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.data.repository.SecureDeletionRepository
import com.example.osint.domain.model.FileToDelete
import com.example.osint.domain.model.SecureDeletionMethod
import com.example.osint.domain.model.SecureDeletionProgress
import com.example.osint.domain.usecase.SecureDeleteFilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SecureDeletionViewModel(
    private val secureDeleteFilesUseCase: SecureDeleteFilesUseCase,
    private val repository: SecureDeletionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SecureDeletionUiState>(SecureDeletionUiState.Idle)
    val uiState: StateFlow<SecureDeletionUiState> = _uiState.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<FileToDelete>>(emptyList())
    val selectedFiles: StateFlow<List<FileToDelete>> = _selectedFiles.asStateFlow()

    private val _selectedMethod = MutableStateFlow(SecureDeletionMethod.DOD_5220_22_M)
    val selectedMethod: StateFlow<SecureDeletionMethod> = _selectedMethod.asStateFlow()

    fun addFile(uri: Uri) {
        viewModelScope.launch {
            val fileInfo = repository.getFileInfo(uri)
            if (fileInfo != null) {
                _selectedFiles.value = _selectedFiles.value + fileInfo
            }
        }
    }

    fun removeFile(file: FileToDelete) {
        _selectedFiles.value = _selectedFiles.value.filter { it.uri != file.uri }
    }

    fun clearFiles() {
        _selectedFiles.value = emptyList()
        _uiState.value = SecureDeletionUiState.Idle
    }

    fun selectMethod(method: SecureDeletionMethod) {
        _selectedMethod.value = method
    }

    fun startSecureDeletion() {
        val files = _selectedFiles.value
        if (files.isEmpty()) {
            _uiState.value = SecureDeletionUiState.Error("No files selected")
            return
        }

        viewModelScope.launch {
            _uiState.value = SecureDeletionUiState.Deleting(
                SecureDeletionProgress(
                    currentFile = "",
                    currentPass = 0,
                    totalPasses = _selectedMethod.value.passes,
                    bytesProcessed = 0L,
                    totalBytes = files.sumOf { it.sizeBytes },
                    filesDeleted = 0,
                    totalFiles = files.size,
                    elapsedTimeMs = 0L,
                    estimatedTimeRemainingMs = 0L
                )
            )

            try {
                // Convert FileToDelete to actual Files
                val actualFiles = files.mapNotNull { fileToDelete ->
                    // First try to get file from URI
                    val file = repository.getFileFromUri(fileToDelete.uri)
                    if (file != null && file.exists()) {
                        file
                    } else {
                        // If URI doesn't give us direct file access, copy to cache
                        repository.copyUriToInternalStorage(fileToDelete.uri, fileToDelete.name)
                    }
                }

                if (actualFiles.isEmpty()) {
                    _uiState.value = SecureDeletionUiState.Error("Unable to access selected files")
                    return@launch
                }

                secureDeleteFilesUseCase(actualFiles, _selectedMethod.value)
                    .collect { progress ->
                        _uiState.value = SecureDeletionUiState.Deleting(progress)
                    }

                // Deletion complete
                _uiState.value = SecureDeletionUiState.Complete(
                    filesDeleted = actualFiles.size,
                    method = _selectedMethod.value
                )

                // Clear selected files after successful deletion
                _selectedFiles.value = emptyList()

            } catch (e: Exception) {
                _uiState.value = SecureDeletionUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun reset() {
        _uiState.value = SecureDeletionUiState.Idle
    }
}

sealed class SecureDeletionUiState {
    object Idle : SecureDeletionUiState()
    data class Deleting(val progress: SecureDeletionProgress) : SecureDeletionUiState()
    data class Complete(val filesDeleted: Int, val method: SecureDeletionMethod) : SecureDeletionUiState()
    data class Error(val message: String) : SecureDeletionUiState()
}
