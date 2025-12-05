package com.example.osint.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.osint.domain.model.FileMetadata
import com.example.osint.presentation.viewmodel.MetadataInspectorUiState
import com.example.osint.presentation.viewmodel.MetadataInspectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataInspectorScreen(
    viewModel: MetadataInspectorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val strippedFileUri by viewModel.strippedFileUri.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.parseFile(it) }
    }

    LaunchedEffect(strippedFileUri) {
        if (strippedFileUri != null) {
            viewModel.clearStrippedFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metadata Inspector") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MetadataInspectorUiState.Initial -> {
                    MetadataInitialState(onSelectFile = { filePicker.launch("*/*") })
                }
                is MetadataInspectorUiState.Loading -> {
                    MetadataLoadingState()
                }
                is MetadataInspectorUiState.Success -> {
                    MetadataSuccessState(
                        metadata = state.metadata,
                        onSelectNewFile = { filePicker.launch("*/*") },
                        onStripExif = { viewModel.stripExif(Uri.parse(state.metadata.filePath)) }
                    )
                }
                is MetadataInspectorUiState.Error -> {
                    MetadataErrorState(
                        message = state.message,
                        onRetry = { filePicker.launch("*/*") }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataInitialState(onSelectFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Select a File",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose an image or document to inspect metadata",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Files")
        }
    }
}

@Composable
private fun MetadataLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analyzing metadata...")
        }
    }
}

@Composable
private fun MetadataErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Select Another File")
        }
    }
}

@Composable
private fun MetadataSuccessState(
    metadata: FileMetadata,
    onSelectNewFile: () -> Unit,
    onStripExif: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FileInfoCard(metadata)

        if (metadata.imageMetadata != null) {
            ImagePreviewCard(metadata.filePath)
            ImageMetadataCard(metadata.imageMetadata)

            if (hasGpsData(metadata.imageMetadata)) {
                GpsWarningCard(metadata.imageMetadata)
            }

            Button(
                onClick = onStripExif,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Strip EXIF Data")
            }
        }

        if (metadata.documentMetadata != null) {
            DocumentMetadataCard(metadata.documentMetadata)
        }

        OutlinedButton(
            onClick = onSelectNewFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Another File")
        }
    }
}

@Composable
private fun FileInfoCard(metadata: FileMetadata) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "File Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            MetadataRow("File Name", metadata.fileName)
            MetadataRow("File Size", formatFileSize(metadata.fileSize))
            MetadataRow("MIME Type", metadata.mimeType ?: "Unknown")
            MetadataRow("SHA-256", metadata.sha256Hash, isMonospace = true)
        }
    }
}

@Composable
private fun ImagePreviewCard(filePath: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = Uri.parse(filePath),
                contentDescription = "Image preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun ImageMetadataCard(imageMetadata: com.example.osint.domain.model.ImageMetadata) {
    var expanded by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Image Metadata (EXIF)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    imageMetadata.width?.let { MetadataRow("Width", "$it px") }
                    imageMetadata.height?.let { MetadataRow("Height", "$it px") }
                    imageMetadata.make?.let { MetadataRow("Camera Make", it, isSensitive = true) }
                    imageMetadata.model?.let { MetadataRow("Camera Model", it, isSensitive = true) }
                    imageMetadata.dateTime?.let { MetadataRow("Date/Time", it, isSensitive = true) }
                    imageMetadata.orientation?.let { MetadataRow("Orientation", it) }
                    imageMetadata.software?.let { MetadataRow("Software", it, isSensitive = true) }
                    imageMetadata.flash?.let { MetadataRow("Flash", it) }
                    imageMetadata.focalLength?.let { MetadataRow("Focal Length", it) }
                    imageMetadata.exposureTime?.let { MetadataRow("Exposure Time", it) }
                    imageMetadata.aperture?.let { MetadataRow("Aperture", it) }
                    imageMetadata.iso?.let { MetadataRow("ISO", it) }
                    imageMetadata.whiteBalance?.let { MetadataRow("White Balance", it) }

                    if (imageMetadata.allTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All EXIF Tags",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        imageMetadata.allTags.forEach { (key, value) ->
                            MetadataRow(key, value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GpsWarningCard(imageMetadata: com.example.osint.domain.model.ImageMetadata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GPS Location Found!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            imageMetadata.gpsLatitude?.let { lat ->
                imageMetadata.gpsLongitude?.let { lon ->
                    MetadataRow("Latitude", lat.toString(), isSensitive = true)
                    MetadataRow("Longitude", lon.toString(), isSensitive = true)
                    imageMetadata.gpsAltitude?.let {
                        MetadataRow("Altitude", "$it meters", isSensitive = true)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coordinates: $lat, $lon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This image contains your exact location! Strip EXIF data before sharing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentMetadataCard(documentMetadata: com.example.osint.domain.model.DocumentMetadata) {
    var expanded by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Document Metadata",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    documentMetadata.title?.let { MetadataRow("Title", it) }
                    documentMetadata.author?.let { MetadataRow("Author", it, isSensitive = true) }
                    documentMetadata.subject?.let { MetadataRow("Subject", it) }
                    documentMetadata.keywords?.let { MetadataRow("Keywords", it) }
                    documentMetadata.creator?.let { MetadataRow("Creator", it, isSensitive = true) }
                    documentMetadata.producer?.let { MetadataRow("Producer", it) }
                    documentMetadata.creationDate?.let { MetadataRow("Created", it, isSensitive = true) }
                    documentMetadata.modificationDate?.let { MetadataRow("Modified", it, isSensitive = true) }
                    documentMetadata.pageCount?.let { MetadataRow("Page Count", it.toString()) }
                    documentMetadata.application?.let { MetadataRow("Application", it, isSensitive = true) }

                    if (documentMetadata.allProperties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All Properties",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        documentMetadata.allProperties.forEach { (key, value) ->
                            MetadataRow(key, value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
    isSensitive: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(140.dp),
            color = if (isSensitive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isMonospace) {
                MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (isSensitive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun hasGpsData(imageMetadata: com.example.osint.domain.model.ImageMetadata): Boolean {
    return imageMetadata.gpsLatitude != null && imageMetadata.gpsLongitude != null
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
