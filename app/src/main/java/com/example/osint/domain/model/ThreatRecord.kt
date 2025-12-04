package com.example.osint.domain.model

data class ThreatRecord(
    val value: String,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)
