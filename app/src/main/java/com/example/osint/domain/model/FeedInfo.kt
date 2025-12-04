package com.example.osint.domain.model

data class FeedInfo(
    val name: String,
    val source: String,
    val type: FeedType,
    val recordCount: Int,
    val lastUpdateTime: Long?
)

enum class FeedType {
    URL, DOMAIN, IP, HASH
}
