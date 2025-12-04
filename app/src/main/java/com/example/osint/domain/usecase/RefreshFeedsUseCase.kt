package com.example.osint.domain.usecase

import com.example.osint.data.feeds.FeedDownloader
import com.example.osint.data.repository.ThreatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RefreshFeedsUseCase(
    private val feedDownloader: FeedDownloader,
    private val repository: ThreatRepository,
    private val parseFeedUseCase: ParseFeedUseCase
) {
    suspend operator fun invoke(feedSource: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Download feed
            val feedContent = feedDownloader.downloadFeed(feedSource)

            // Parse feed
            val threatRecords = parseFeedUseCase(feedSource, feedContent)

            // Save to database
            val recordCount = repository.saveFeedRecords(feedSource, threatRecords)

            Result.success(recordCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAllFeeds(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableMapOf<String, Int>()
            val feeds = listOf(
                "urlhaus",
                "phishtank",
                "openphish",
                "blocklist",
                "firehol"
            )

            feeds.forEach { feedSource ->
                try {
                    val result = invoke(feedSource)
                    result.onSuccess { count ->
                        results[feedSource] = count
                    }
                } catch (e: Exception) {
                    results[feedSource] = 0
                }
            }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
