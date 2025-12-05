package com.example.osint.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.osint.domain.model.DeviceInfo
import com.example.osint.domain.model.DeviceType
import com.example.osint.presentation.viewmodel.NetworkMapUiState
import com.example.osint.presentation.viewmodel.NetworkMapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkMapScreen(
    viewModel: NetworkMapViewModel,
    onNavigateToDeviceDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Map") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                is NetworkMapUiState.Loading -> {
                    NetworkMapLoadingState()
                }
                is NetworkMapUiState.Empty -> {
                    NetworkMapEmptyState()
                }
                is NetworkMapUiState.Success -> {
                    DeviceGridContent(
                        devices = state.devices,
                        onDeviceClick = onNavigateToDeviceDetail
                    )
                }
                is NetworkMapUiState.Error -> {
                    NetworkMapErrorState(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun NetworkMapLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading devices...")
        }
    }
}

@Composable
private fun NetworkMapEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.DevicesOther,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No Devices Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Run a network scan first to discover devices",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NetworkMapErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
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
        }
    }
}

@Composable
private fun DeviceGridContent(
    devices: List<DeviceInfo>,
    onDeviceClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { it.ipAddress }
        ) { device ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn()
            ) {
                DeviceTile(
                    device = device,
                    onClick = { onDeviceClick(device.ipAddress) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceTile(
    device: DeviceInfo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = getDeviceIcon(device.deviceType),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (device.isOnline) Color.Green else Color.Gray,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = device.ipAddress,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                device.hostname?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = device.vendor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                if (device.openPorts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ports: ${device.openPorts.take(3).joinToString(", ")}${if (device.openPorts.size > 3) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

private fun getDeviceIcon(deviceType: DeviceType): ImageVector {
    return when (deviceType) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.LAPTOP -> Icons.Default.Laptop
        DeviceType.PHONE -> Icons.Default.Smartphone
        DeviceType.TABLET -> Icons.Default.Tablet
        DeviceType.PRINTER -> Icons.Default.Print
        DeviceType.IOT_DEVICE -> Icons.Default.Sensors
        DeviceType.SERVER -> Icons.Default.Dns
        DeviceType.UNKNOWN -> Icons.Default.DeviceUnknown
    }
}
