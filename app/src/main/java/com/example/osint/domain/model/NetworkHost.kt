package com.example.osint.domain.model

data class NetworkHost(
    val ipAddress: String,
    val isAlive: Boolean,
    val openPorts: List<Int>,
    val banners: Map<Int, String>,
    val scanTimestamp: Long = System.currentTimeMillis()
)
