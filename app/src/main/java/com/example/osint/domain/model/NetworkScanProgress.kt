package com.example.osint.domain.model

data class NetworkScanProgress(
    val totalHosts: Int,
    val scannedHosts: Int,
    val currentIp: String?,
    val foundHosts: List<NetworkHost>
) {
    val progressPercentage: Int
        get() = if (totalHosts > 0) (scannedHosts * 100) / totalHosts else 0
}
