package com.example.osint.data.feeds

import com.example.osint.domain.model.ThreatRecord

class FireHolParser : FeedParser {
    override fun parse(content: String, source: String): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        val timestamp = System.currentTimeMillis()

        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                // FireHOL uses CIDR notation (e.g., 192.168.1.0/24)
                // Extract the IP part before the slash
                val ip = if (trimmed.contains("/")) {
                    trimmed.substringBefore("/")
                } else {
                    trimmed
                }

                if (isValidIp(ip)) {
                    records.add(ThreatRecord(ip, source, timestamp))
                }
            }
        }

        return records
    }

    private fun isValidIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            part.toIntOrNull()?.let { it in 0..255 } ?: false
        }
    }
}
