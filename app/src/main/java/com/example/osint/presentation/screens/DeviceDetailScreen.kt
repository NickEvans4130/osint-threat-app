package com.example.osint.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.osint.domain.model.DeviceInfo
import com.example.osint.domain.model.DeviceType
import com.example.osint.presentation.viewmodel.DeviceDetailUiState
import com.example.osint.presentation.viewmodel.DeviceDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    viewModel: DeviceDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
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
                is DeviceDetailUiState.Loading -> {
                    DeviceDetailLoadingState()
                }
                is DeviceDetailUiState.Success -> {
                    DeviceDetailContent(device = state.device)
                }
                is DeviceDetailUiState.Error -> {
                    DeviceDetailErrorState(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun DeviceDetailLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DeviceDetailErrorState(message: String) {
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
private fun DeviceDetailContent(device: DeviceInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DeviceHeaderCard(device)
        DeviceInfoCard(device)
        if (device.openPorts.isNotEmpty()) {
            OpenPortsCard(device)
        }
        if (device.banners.isNotEmpty()) {
            BannersCard(device)
        }
    }
}

@Composable
private fun DeviceHeaderCard(device: DeviceInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getDeviceIconForDetail(device.deviceType),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = device.ipAddress,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            device.hostname?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = if (device.isOnline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (device.isOnline) "Online" else "Offline",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (device.isOnline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(device: DeviceInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Device Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            DeviceInfoRow("IP Address", device.ipAddress)
            device.macAddress?.let {
                DeviceInfoRow("MAC Address", it)
            }
            DeviceInfoRow("Vendor", device.vendor)
            DeviceInfoRow("Device Type", getDeviceTypeLabel(device.deviceType))
            DeviceInfoRow("Scan Time", formatTimestamp(device.scanTimestamp))
        }
    }
}

@Composable
private fun OpenPortsCard(device: DeviceInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Open Ports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            device.openPorts.forEach { port ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Port $port",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getPortService(port),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BannersCard(device: DeviceInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Service Banners",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            device.banners.forEach { (port, banner) ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Port $port:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = banner,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun getDeviceIconForDetail(deviceType: DeviceType) = when (deviceType) {
    DeviceType.ROUTER -> Icons.Default.Router
    DeviceType.LAPTOP -> Icons.Default.Laptop
    DeviceType.PHONE -> Icons.Default.Smartphone
    DeviceType.TABLET -> Icons.Default.Tablet
    DeviceType.PRINTER -> Icons.Default.Print
    DeviceType.IOT_DEVICE -> Icons.Default.Sensors
    DeviceType.SERVER -> Icons.Default.Dns
    DeviceType.UNKNOWN -> Icons.Default.DeviceUnknown
}

private fun getDeviceTypeLabel(deviceType: DeviceType): String {
    return when (deviceType) {
        DeviceType.ROUTER -> "Router"
        DeviceType.LAPTOP -> "Laptop"
        DeviceType.PHONE -> "Phone"
        DeviceType.TABLET -> "Tablet"
        DeviceType.PRINTER -> "Printer"
        DeviceType.IOT_DEVICE -> "IoT Device"
        DeviceType.SERVER -> "Server"
        DeviceType.UNKNOWN -> "Unknown"
    }
}

private fun getPortService(port: Int): String {
    return when (port) {
        22 -> "SSH"
        80 -> "HTTP"
        443 -> "HTTPS"
        21 -> "FTP"
        23 -> "Telnet"
        25 -> "SMTP"
        53 -> "DNS"
        110 -> "POP3"
        143 -> "IMAP"
        3306 -> "MySQL"
        3389 -> "RDP"
        5432 -> "PostgreSQL"
        8080 -> "HTTP Alt"
        else -> "Unknown"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timestamp))
}
