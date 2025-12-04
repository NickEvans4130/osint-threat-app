package com.example.osint.data.feeds

import com.example.osint.domain.model.ThreatRecord

class OpenPhishParser : FeedParser {
    override fun parse(content: String, source: String): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        val timestamp = System.currentTimeMillis()

        content.lines().forEach { line ->
            val url = line.trim()
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                records.add(ThreatRecord(url, source, timestamp))
            }
        }

        return records
    }
}
