package com.example.osint.domain.model

data class ThreatResult(
    val query: String,
    val queryType: QueryType,
    val isThreat: Boolean,
    val foundInFeeds: List<String>,
    val domain: String? = null,
    val resolvedIp: String? = null,
    val urlMatch: Boolean = false,
    val domainMatch: Boolean = false,
    val ipMatch: Boolean = false,
    val hashMatch: Boolean = false,
    val riskScore: RiskScoreResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class QueryType {
    URL, IP, HASH, DOMAIN
}
