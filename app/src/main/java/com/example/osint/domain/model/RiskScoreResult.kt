package com.example.osint.domain.model

data class RiskScoreResult(
    val score: Int,
    val severity: Severity,
    val reasons: List<String>
)

enum class Severity {
    LOW,     // 0-29
    MEDIUM,  // 30-59
    HIGH     // 60-100
}
