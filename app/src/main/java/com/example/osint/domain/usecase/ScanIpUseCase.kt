package com.example.osint.domain.usecase

import com.example.osint.data.repository.ThreatRepository
import com.example.osint.domain.model.QueryType
import com.example.osint.domain.model.ThreatResult

class ScanIpUseCase(
    private val repository: ThreatRepository,
    private val computeRiskScoreUseCase: ComputeRiskScoreUseCase
) {
    suspend operator fun invoke(ip: String): ThreatResult {
        val foundInFeeds = mutableListOf<String>()

        // Check if IP exists in malicious IPs
        val ipRecord = repository.findIpByValue(ip)
        if (ipRecord != null) {
            foundInFeeds.add("IP: ${ipRecord.source}")
        }

        val isThreat = foundInFeeds.isNotEmpty()
        val riskScore = if (isThreat) {
            computeRiskScoreUseCase(
                foundInFeedCount = foundInFeeds.size,
                domain = null
            )
        } else null

        return ThreatResult(
            query = ip,
            queryType = QueryType.IP,
            isThreat = isThreat,
            foundInFeeds = foundInFeeds,
            ipMatch = ipRecord != null,
            riskScore = riskScore
        )
    }
}
