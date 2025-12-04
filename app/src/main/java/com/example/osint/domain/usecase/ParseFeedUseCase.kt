package com.example.osint.domain.usecase

import com.example.osint.data.feeds.*
import com.example.osint.domain.model.ThreatRecord

class ParseFeedUseCase {
    operator fun invoke(feedSource: String, content: String): List<ThreatRecord> {
        val parser = when (feedSource.lowercase()) {
            "urlhaus" -> UrlHausParser()
            "phishtank" -> PhishTankParser()
            "openphish" -> OpenPhishParser()
            "blocklist" -> BlocklistParser()
            "firehol" -> FireHolParser()
            else -> throw IllegalArgumentException("Unknown feed source: $feedSource")
        }

        return parser.parse(content, feedSource)
    }
}
