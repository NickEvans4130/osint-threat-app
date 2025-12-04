package com.example.osint.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.osint.data.db.ThreatDatabase
import com.example.osint.data.feeds.FeedDownloader
import com.example.osint.data.repository.ThreatRepository
import com.example.osint.domain.usecase.ComputeRiskScoreUseCase
import com.example.osint.domain.usecase.ParseFeedUseCase
import com.example.osint.domain.usecase.RefreshFeedsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = ThreatDatabase.getDatabase(applicationContext)
            val repository = ThreatRepository(database)
            val feedDownloader = FeedDownloader(applicationContext)
            val parseFeedUseCase = ParseFeedUseCase()
            val refreshFeedsUseCase = RefreshFeedsUseCase(
                feedDownloader = feedDownloader,
                repository = repository,
                parseFeedUseCase = parseFeedUseCase
            )

            val result = refreshFeedsUseCase.refreshAllFeeds()

            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
