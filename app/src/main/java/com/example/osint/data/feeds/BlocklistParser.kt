package com.example.osint.data.feeds

import com.example.osint.domain.model.ThreatRecord

class BlocklistParser : FeedParser {
    override fun parse(content: String, source: String): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        val timestamp = System.currentTimeMillis()

        content.lines().forEach { line ->
            val ip = line.trim()
            if (ip.isNotBlank() && !ip.startsWith("#") && isValidIp(ip)) {
                records.add(ThreatRecord(ip, source, timestamp))
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
