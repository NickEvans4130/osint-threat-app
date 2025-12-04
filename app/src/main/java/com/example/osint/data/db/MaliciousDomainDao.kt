package com.example.osint.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MaliciousDomainDao {
    @Query("SELECT * FROM malicious_domains WHERE value = :domain LIMIT 1")
    suspend fun findByDomain(domain: String): MaliciousDomain?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(domains: List<MaliciousDomain>)

    @Query("DELETE FROM malicious_domains WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Query("SELECT COUNT(*) FROM malicious_domains")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM malicious_domains WHERE source = :source")
    suspend fun getCountBySource(source: String): Int

    @Query("SELECT MAX(timestamp) FROM malicious_domains WHERE source = :source")
    suspend fun getLastUpdateTime(source: String): Long?
}
