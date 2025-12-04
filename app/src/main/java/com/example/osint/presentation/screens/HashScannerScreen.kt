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
import androidx.compose.ui.unit.dp
import com.example.osint.presentation.viewmodel.HashScannerUiState
import com.example.osint.presentation.viewmodel.HashScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashScannerScreen(
    viewModel: HashScannerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var hashInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hash Scanner") },
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
                value = hashInput,
                onValueChange = { hashInput = it },
                label = { Text("Enter File Hash (MD5/SHA1/SHA256)") },
                placeholder = { Text("e99a18c428cb38d5f260853678922e03") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.scanHash(hashInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is HashScannerUiState.Loading
            ) {
                Text("Scan Hash")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is HashScannerUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HashScannerUiState.Success -> {
                    ThreatResultCard(result = state.result)
                }
                is HashScannerUiState.Error -> {
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
                is HashScannerUiState.Initial -> {
                    // Show nothing
                }
            }
        }
    }
}
