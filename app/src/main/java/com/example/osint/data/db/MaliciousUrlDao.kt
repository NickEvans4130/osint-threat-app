package com.example.osint.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MaliciousUrlDao {
    @Query("SELECT * FROM malicious_urls WHERE value = :url LIMIT 1")
    suspend fun findByUrl(url: String): MaliciousUrl?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(urls: List<MaliciousUrl>)

    @Query("DELETE FROM malicious_urls WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Query("SELECT COUNT(*) FROM malicious_urls")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM malicious_urls WHERE source = :source")
    suspend fun getCountBySource(source: String): Int

    @Query("SELECT MAX(timestamp) FROM malicious_urls WHERE source = :source")
    suspend fun getLastUpdateTime(source: String): Long?
}
