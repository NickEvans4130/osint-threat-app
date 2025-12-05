package com.example.osint.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavPage {
    THREAT_INTEL,
    NETWORK,
    PRIVACY,
    SETTINGS
}

@Composable
fun MainScreen(
    onNavigateToUrlScanner: () -> Unit,
    onNavigateToIpScanner: () -> Unit,
    onNavigateToHashScanner: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    onNavigateToNetworkScanner: () -> Unit,
    onNavigateToNetworkMap: () -> Unit,
    onNavigateToMetadataInspector: () -> Unit,
    onNavigateToFeedStatus: () -> Unit
) {
    var selectedPage by remember { mutableStateOf(BottomNavPage.THREAT_INTEL) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                    label = { Text("Threat Intel") },
                    selected = selectedPage == BottomNavPage.THREAT_INTEL,
                    onClick = { selectedPage = BottomNavPage.THREAT_INTEL }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.NetworkCheck, contentDescription = null) },
                    label = { Text("Network") },
                    selected = selectedPage == BottomNavPage.NETWORK,
                    onClick = { selectedPage = BottomNavPage.NETWORK }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text("Privacy") },
                    selected = selectedPage == BottomNavPage.PRIVACY,
                    onClick = { selectedPage = BottomNavPage.PRIVACY }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = selectedPage == BottomNavPage.SETTINGS,
                    onClick = { selectedPage = BottomNavPage.SETTINGS }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedPage) {
                BottomNavPage.THREAT_INTEL -> ThreatIntelPage(
                    onNavigateToUrlScanner = onNavigateToUrlScanner,
                    onNavigateToIpScanner = onNavigateToIpScanner,
                    onNavigateToHashScanner = onNavigateToHashScanner,
                    onNavigateToQRScanner = onNavigateToQRScanner
                )
                BottomNavPage.NETWORK -> NetworkToolsPage(
                    onNavigateToNetworkScanner = onNavigateToNetworkScanner,
                    onNavigateToNetworkMap = onNavigateToNetworkMap
                )
                BottomNavPage.PRIVACY -> PrivacyToolsPage(
                    onNavigateToMetadataInspector = onNavigateToMetadataInspector
                )
                BottomNavPage.SETTINGS -> SettingsPage(
                    onNavigateToFeedStatus = onNavigateToFeedStatus
                )
            }
        }
    }
}
