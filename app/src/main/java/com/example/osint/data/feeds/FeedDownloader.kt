package com.example.osint.data.feeds

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class FeedDownloader(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val feedUrls = mapOf(
        "urlhaus" to "https://urlhaus.abuse.ch/downloads/csv_recent/",
        "phishtank" to "http://data.phishtank.com/data/online-valid.csv",
        "openphish" to "https://openphish.com/feed.txt",
        "abuseipdb" to "https://api.abuseipdb.com/api/v2/blacklist",
        "firehol" to "https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level1.netset"
    )

    suspend fun downloadFeed(feedSource: String): String = withContext(Dispatchers.IO) {
        val url = feedUrls[feedSource] ?: throw IllegalArgumentException("Unknown feed source: $feedSource")

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Failed to download feed: ${response.code}")
        }

        val content = response.body?.string() ?: throw Exception("Empty response body")

        // Validate file size (max 100MB)
        if (content.length > 100 * 1024 * 1024) {
            throw Exception("Feed file too large")
        }

        // Save to cache dir temporarily
        val cacheFile = File(context.cacheDir, "${feedSource}_${System.currentTimeMillis()}.tmp")
        cacheFile.writeText(content)

        // Read content and delete file
        val result = cacheFile.readText()
        cacheFile.delete()

        result
    }
}
