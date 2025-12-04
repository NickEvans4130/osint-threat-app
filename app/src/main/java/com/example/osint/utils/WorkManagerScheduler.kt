package com.example.osint.utils

import android.content.Context
import androidx.work.*
import com.example.osint.data.worker.FeedRefreshWorker
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val FEED_REFRESH_WORK_NAME = "feed_refresh_work"

    fun scheduleDailyFeedRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<FeedRefreshWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FEED_REFRESH_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    fun triggerManualRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val manualWorkRequest = OneTimeWorkRequestBuilder<FeedRefreshWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(manualWorkRequest)
    }

    fun cancelScheduledWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(FEED_REFRESH_WORK_NAME)
    }
}
