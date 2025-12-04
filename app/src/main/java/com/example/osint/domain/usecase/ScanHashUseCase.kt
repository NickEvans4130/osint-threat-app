package com.example.osint.domain.usecase

import com.example.osint.data.repository.ThreatRepository
import com.example.osint.domain.model.QueryType
import com.example.osint.domain.model.ThreatResult

class ScanHashUseCase(
    private val repository: ThreatRepository,
    private val computeRiskScoreUseCase: ComputeRiskScoreUseCase
) {
    suspend operator fun invoke(hash: String): ThreatResult {
        val foundInFeeds = mutableListOf<String>()
        val normalizedHash = hash.lowercase()

        // Check if hash exists in malicious hashes
        val hashRecord = repository.findHashByValue(normalizedHash)
        if (hashRecord != null) {
            foundInFeeds.add("Hash: ${hashRecord.source}")
        }

        val isThreat = foundInFeeds.isNotEmpty()
        val riskScore = if (isThreat) {
            computeRiskScoreUseCase(
                foundInFeedCount = foundInFeeds.size,
                domain = null
            )
        } else null

        return ThreatResult(
            query = hash,
            queryType = QueryType.HASH,
            isThreat = isThreat,
            foundInFeeds = foundInFeeds,
            hashMatch = hashRecord != null,
            riskScore = riskScore
        )
    }
}
