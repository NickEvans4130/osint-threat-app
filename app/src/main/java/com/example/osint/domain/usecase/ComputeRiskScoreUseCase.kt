package com.example.osint.domain.usecase

import com.example.osint.domain.model.RiskScoreResult
import com.example.osint.domain.model.Severity

class ComputeRiskScoreUseCase {
    operator fun invoke(
        foundInFeedCount: Int,
        domain: String? = null,
        domainAge: Int? = null
    ): RiskScoreResult {
        var score = 0
        val reasons = mutableListOf<String>()

        // +50 if found in any feed
        if (foundInFeedCount > 0) {
            score += 50
            reasons.add("Found in threat intelligence feed")
        }

        // +10 per additional feed
        if (foundInFeedCount > 1) {
            val additionalScore = (foundInFeedCount - 1) * 10
            score += additionalScore
            reasons.add("Found in ${foundInFeedCount} feeds (+${additionalScore} points)")
        }

        // +5 if domain age < 30 days (if provided)
        if (domainAge != null && domainAge < 30) {
            score += 5
            reasons.add("Domain is less than 30 days old (+5 points)")
        }

        // Cap score at 100
        if (score > 100) score = 100

        val severity = when {
            score < 30 -> Severity.LOW
            score < 60 -> Severity.MEDIUM
            else -> Severity.HIGH
        }

        return RiskScoreResult(
            score = score,
            severity = severity,
            reasons = reasons
        )
    }
}
