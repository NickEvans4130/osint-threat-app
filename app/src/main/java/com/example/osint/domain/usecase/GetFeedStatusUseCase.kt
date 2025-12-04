package com.example.osint.domain.usecase

import com.example.osint.data.repository.ThreatRepository
import com.example.osint.domain.model.FeedInfo
import com.example.osint.domain.model.FeedType

class GetFeedStatusUseCase(
    private val repository: ThreatRepository
) {
    suspend operator fun invoke(): List<FeedInfo> {
        return listOf(
            FeedInfo(
                name = "URLhaus",
                source = "urlhaus",
                type = FeedType.URL,
                recordCount = repository.getUrlCountBySource("urlhaus"),
                lastUpdateTime = repository.getUrlLastUpdateTime("urlhaus")
            ),
            FeedInfo(
                name = "PhishTank",
                source = "phishtank",
                type = FeedType.URL,
                recordCount = repository.getUrlCountBySource("phishtank"),
                lastUpdateTime = repository.getUrlLastUpdateTime("phishtank")
            ),
            FeedInfo(
                name = "OpenPhish",
                source = "openphish",
                type = FeedType.URL,
                recordCount = repository.getUrlCountBySource("openphish"),
                lastUpdateTime = repository.getUrlLastUpdateTime("openphish")
            ),
            FeedInfo(
                name = "AbuseIPDB",
                source = "abuseipdb",
                type = FeedType.IP,
                recordCount = repository.getIpCountBySource("abuseipdb"),
                lastUpdateTime = repository.getIpLastUpdateTime("abuseipdb")
            ),
            FeedInfo(
                name = "FireHOL",
                source = "firehol",
                type = FeedType.IP,
                recordCount = repository.getIpCountBySource("firehol"),
                lastUpdateTime = repository.getIpLastUpdateTime("firehol")
            )
        )
    }
}
