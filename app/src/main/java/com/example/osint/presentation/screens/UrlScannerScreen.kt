package com.example.osint.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.osint.domain.model.Severity
import com.example.osint.presentation.viewmodel.UrlScannerUiState
import com.example.osint.presentation.viewmodel.UrlScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlScannerScreen(
    viewModel: UrlScannerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var urlInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URL Scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Enter URL") },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.scanUrl(urlInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is UrlScannerUiState.Loading
            ) {
                Text("Scan URL")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is UrlScannerUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UrlScannerUiState.Success -> {
                    ThreatResultCard(result = state.result)
                }
                is UrlScannerUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is UrlScannerUiState.Initial -> {
                    // Show nothing
                }
            }
        }
    }
}

@Composable
fun ThreatResultCard(result: com.example.osint.domain.model.ThreatResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isThreat) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (result.isThreat) "THREAT DETECTED" else "NO THREAT FOUND",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (result.isThreat) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (result.riskScore != null) {
                Text(
                    text = "Risk Score: ${result.riskScore.score}/100",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Severity: ${result.riskScore.severity}",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (result.riskScore.severity) {
                        Severity.HIGH -> MaterialTheme.colorScheme.error
                        Severity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        Severity.LOW -> MaterialTheme.colorScheme.primary
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reasons:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                result.riskScore.reasons.forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (result.foundInFeeds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Found in feeds:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                result.foundInFeeds.forEach { feed ->
                    Text(
                        text = "• $feed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (result.domain != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Domain: ${result.domain}", style = MaterialTheme.typography.bodyMedium)
            }

            if (result.resolvedIp != null) {
                Text(text = "Resolved IP: ${result.resolvedIp}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (result.urlMatch) {
                Text(text = "✓ URL match found", style = MaterialTheme.typography.bodySmall)
            }
            if (result.domainMatch) {
                Text(text = "✓ Domain match found", style = MaterialTheme.typography.bodySmall)
            }
            if (result.ipMatch) {
                Text(text = "✓ IP match found", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
