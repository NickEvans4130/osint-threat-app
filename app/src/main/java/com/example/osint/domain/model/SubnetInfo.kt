package com.example.osint.domain.model

data class SubnetInfo(
    val localIpAddress: String,
    val subnetMask: String,
    val networkAddress: String,
    val cidrNotation: String,
    val ipRange: Pair<String, String>,
    val totalHosts: Int
)
