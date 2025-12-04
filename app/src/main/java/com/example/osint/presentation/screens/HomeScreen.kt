package com.example.osint.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToUrlScanner: () -> Unit,
    onNavigateToIpScanner: () -> Unit,
    onNavigateToHashScanner: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    onNavigateToFeedStatus: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pocket Threat Intel Scanner") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Offline OSINT Threat Scanner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            ScannerCard(
                title = "Scan URL",
                description = "Check URLs against threat intelligence feeds",
                icon = Icons.Default.Link,
                onClick = onNavigateToUrlScanner
            )

            ScannerCard(
                title = "Scan IP Address",
                description = "Lookup IP addresses in malicious IP databases",
                icon = Icons.Default.Computer,
                onClick = onNavigateToIpScanner
            )

            ScannerCard(
                title = "Scan File Hash",
                description = "Check file hashes against known malware",
                icon = Icons.Default.Fingerprint,
                onClick = onNavigateToHashScanner
            )

            ScannerCard(
                title = "Scan QR Code",
                description = "Scan and analyze QR codes for threats",
                icon = Icons.Default.QrCode,
                onClick = onNavigateToQRScanner
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onNavigateToFeedStatus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Storage, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Feed Status & Updates")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
