package com.example.osint.data.feeds

import com.example.osint.domain.model.ThreatRecord

interface FeedParser {
    fun parse(content: String, source: String): List<ThreatRecord>
}
