package com.example.osint.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "malicious_ips")
data class MaliciousIp(
    @PrimaryKey
    val value: String,
    val source: String,
    val timestamp: Long
)
