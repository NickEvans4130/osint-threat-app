package com.example.osint.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MaliciousIpDao {
    @Query("SELECT * FROM malicious_ips WHERE value = :ip LIMIT 1")
    suspend fun findByIp(ip: String): MaliciousIp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ips: List<MaliciousIp>)

    @Query("DELETE FROM malicious_ips WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Query("SELECT COUNT(*) FROM malicious_ips")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM malicious_ips WHERE source = :source")
    suspend fun getCountBySource(source: String): Int

    @Query("SELECT MAX(timestamp) FROM malicious_ips WHERE source = :source")
    suspend fun getLastUpdateTime(source: String): Long?
}
