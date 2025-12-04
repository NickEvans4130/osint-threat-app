package com.example.osint.data.feeds

import com.example.osint.domain.model.ThreatRecord

class PhishTankParser : FeedParser {
    override fun parse(content: String, source: String): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        val timestamp = System.currentTimeMillis()

        content.lines().forEach { line ->
            if (line.isNotBlank() && !line.startsWith("phish_id") && !line.startsWith("#")) {
                val parts = line.split(",")
                if (parts.size >= 2) {
                    val url = parts[1].trim().removeSurrounding("\"")
                    if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                        records.add(ThreatRecord(url, source, timestamp))
                    }
                }
            }
        }

        return records
    }
}
