package com.example.osint.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MaliciousHashDao {
    @Query("SELECT * FROM malicious_hashes WHERE value = :hash LIMIT 1")
    suspend fun findByHash(hash: String): MaliciousHash?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hashes: List<MaliciousHash>)

    @Query("DELETE FROM malicious_hashes WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Query("SELECT COUNT(*) FROM malicious_hashes")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM malicious_hashes WHERE source = :source")
    suspend fun getCountBySource(source: String): Int

    @Query("SELECT MAX(timestamp) FROM malicious_hashes WHERE source = :source")
    suspend fun getLastUpdateTime(source: String): Long?
}
