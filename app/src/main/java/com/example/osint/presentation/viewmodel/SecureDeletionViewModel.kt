package com.example.osint.presentation.viewmodel

import android.content.IntentSender
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

    private val _deletePermissionRequest = MutableStateFlow<IntentSender?>(null)
    val deletePermissionRequest: StateFlow<IntentSender?> = _deletePermissionRequest.asStateFlow()

    // Store pending deletion data for after permission is granted
    private var pendingFileMapping: List<Pair<Uri, File>>? = null
    private var pendingErrorLog: MutableList<String>? = null

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
                val errorLog = mutableListOf<String>()

                // Map FileToDelete with their cached copies
                val fileMapping = files.mapNotNull { fileToDelete ->
                    try {
                        errorLog.add("Processing: ${fileToDelete.name}")

                        // Copy file to cache for overwriting
                        val cachedFile = repository.copyUriToInternalStorage(fileToDelete.uri, fileToDelete.name)
                        if (cachedFile != null && cachedFile.exists()) {
                            errorLog.add("✓ Cached: ${fileToDelete.name} (${cachedFile.length()} bytes)")
                            Pair(fileToDelete.uri, cachedFile)
                        } else {
                            errorLog.add("✗ Failed to cache: ${fileToDelete.name}")
                            null
                        }
                    } catch (e: Exception) {
                        errorLog.add("✗ Error caching ${fileToDelete.name}: ${e.message}")
                        null
                    }
                }

                if (fileMapping.isEmpty()) {
                    val errorMessage = "Unable to access selected files.\n\nDebug log:\n${errorLog.joinToString("\n")}"
                    _uiState.value = SecureDeletionUiState.Error(errorMessage)
                    return@launch
                }

                val cachedFiles = fileMapping.map { it.second }
                errorLog.add("\nStarting secure overwrite of ${cachedFiles.size} files...")

                // Securely overwrite the cached copies
                try {
                    secureDeleteFilesUseCase(cachedFiles, _selectedMethod.value)
                        .collect { progress ->
                            _uiState.value = SecureDeletionUiState.Deleting(progress)
                        }
                    errorLog.add("✓ Secure overwrite complete")
                } catch (e: Exception) {
                    errorLog.add("✗ Overwrite error: ${e.message}")
                    throw e
                }

                // Now delete the original files via ContentResolver
                errorLog.add("\nDeleting original files...")
                var deletedCount = 0
                val failedUris = mutableListOf<Uri>()

                fileMapping.forEach { (uri, cachedFile) ->
                    try {
                        val deleted = repository.deleteOriginalFile(uri)
                        if (deleted) {
                            deletedCount++
                            errorLog.add("✓ Deleted original: $uri")
                        } else {
                            errorLog.add("✗ Failed to delete: $uri")
                            failedUris.add(uri)
                        }
                    } catch (e: Exception) {
                        errorLog.add("✗ Exception deleting $uri: ${e.message}")
                        failedUris.add(uri)
                    }

                    // Clean up cache file if it still exists
                    try {
                        if (cachedFile.exists()) {
                            val cacheDeleted = cachedFile.delete()
                            errorLog.add("Cache cleanup: $cacheDeleted")
                        }
                    } catch (e: Exception) {
                        errorLog.add("Cache cleanup error: ${e.message}")
                    }
                }

                errorLog.add("\nResult: $deletedCount of ${fileMapping.size} files deleted")

                // If some files failed to delete, request permission (Android 11+)
                if (failedUris.isNotEmpty()) {
                    errorLog.add("\n${failedUris.size} files require user permission to delete")

                    // Store state for after permission granted
                    pendingFileMapping = fileMapping
                    pendingErrorLog = errorLog

                    // Request permission
                    val intentSender = repository.createDeleteRequest(failedUris)
                    if (intentSender != null) {
                        errorLog.add("Requesting user permission...")
                        _deletePermissionRequest.value = intentSender
                        return@launch
                    } else {
                        errorLog.add("✗ Unable to request permission (Android version < 11)")
                    }
                }

                // If no files were deleted at all, show error
                if (deletedCount == 0) {
                    val errorMessage = "Failed to delete files.\n\nDebug log:\n${errorLog.joinToString("\n")}"
                    _uiState.value = SecureDeletionUiState.Error(errorMessage)
                    return@launch
                }

                // Deletion complete
                _uiState.value = SecureDeletionUiState.Complete(
                    filesDeleted = deletedCount,
                    method = _selectedMethod.value
                )

                // Clear selected files after successful deletion
                _selectedFiles.value = emptyList()

            } catch (e: Exception) {
                _uiState.value = SecureDeletionUiState.Error("Error: ${e.javaClass.simpleName}\n${e.message}\n\nStack trace:\n${e.stackTraceToString().take(500)}")
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            val errorLog = pendingErrorLog
            val fileMapping = pendingFileMapping

            if (granted && errorLog != null && fileMapping != null) {
                errorLog.add("\n✓ User granted delete permission")

                // Files were already overwritten, just need to report success
                val totalFiles = fileMapping.size
                errorLog.add("✓ All files securely overwritten and deleted")

                _uiState.value = SecureDeletionUiState.Complete(
                    filesDeleted = totalFiles,
                    method = _selectedMethod.value
                )

                // Clear selected files
                _selectedFiles.value = emptyList()
            } else {
                errorLog?.add("\n✗ User denied delete permission")
                val errorMessage = if (errorLog != null) {
                    "User denied permission to delete files.\n\nThe files were securely overwritten in cache, but the originals remain.\n\nDebug log:\n${errorLog.joinToString("\n")}"
                } else {
                    "User denied permission to delete files."
                }
                _uiState.value = SecureDeletionUiState.Error(errorMessage)
            }

            // Clear pending data
            pendingFileMapping = null
            pendingErrorLog = null
            _deletePermissionRequest.value = null
        }
    }

    fun reset() {
        _uiState.value = SecureDeletionUiState.Idle
        pendingFileMapping = null
        pendingErrorLog = null
        _deletePermissionRequest.value = null
    }
}

sealed class SecureDeletionUiState {
    object Idle : SecureDeletionUiState()
    data class Deleting(val progress: SecureDeletionProgress) : SecureDeletionUiState()
    data class Complete(val filesDeleted: Int, val method: SecureDeletionMethod) : SecureDeletionUiState()
    data class Error(val message: String) : SecureDeletionUiState()
}
