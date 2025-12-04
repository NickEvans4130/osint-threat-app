package com.example.osint.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osint.domain.model.FileMetadata
import com.example.osint.domain.usecase.ParseImageMetadataUseCase
import com.example.osint.domain.usecase.StripImageExifUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetadataInspectorViewModel(
    private val parseImageMetadataUseCase: ParseImageMetadataUseCase,
    private val stripImageExifUseCase: StripImageExifUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MetadataInspectorUiState>(MetadataInspectorUiState.Initial)
    val uiState: StateFlow<MetadataInspectorUiState> = _uiState.asStateFlow()

    private val _strippedFileUri = MutableStateFlow<Uri?>(null)
    val strippedFileUri: StateFlow<Uri?> = _strippedFileUri.asStateFlow()

    fun parseFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = MetadataInspectorUiState.Loading
            try {
                val metadata = parseImageMetadataUseCase(uri)
                _uiState.value = MetadataInspectorUiState.Success(metadata)
            } catch (e: Exception) {
                _uiState.value = MetadataInspectorUiState.Error(e.message ?: "Failed to parse metadata")
            }
        }
    }

    fun stripExif(uri: Uri) {
        viewModelScope.launch {
            try {
                val strippedUri = stripImageExifUseCase(uri)
                _strippedFileUri.value = strippedUri
            } catch (e: Exception) {
                _uiState.value = MetadataInspectorUiState.Error("Failed to strip EXIF data: ${e.message}")
            }
        }
    }

    fun clearStrippedFile() {
        _strippedFileUri.value = null
    }

    fun reset() {
        _uiState.value = MetadataInspectorUiState.Initial
        _strippedFileUri.value = null
    }
}

sealed class MetadataInspectorUiState {
    object Initial : MetadataInspectorUiState()
    object Loading : MetadataInspectorUiState()
    data class Success(val metadata: FileMetadata) : MetadataInspectorUiState()
    data class Error(val message: String) : MetadataInspectorUiState()
}
