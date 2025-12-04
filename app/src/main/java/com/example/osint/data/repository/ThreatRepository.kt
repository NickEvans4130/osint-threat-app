package com.example.osint.data.repository

import com.example.osint.data.db.*
import com.example.osint.domain.model.ThreatRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ThreatRepository(private val database: ThreatDatabase) {

    // URL operations
    suspend fun findUrlByValue(url: String): MaliciousUrl? = withContext(Dispatchers.IO) {
        database.maliciousUrlDao().findByUrl(url)
    }

    suspend fun getUrlCountBySource(source: String): Int = withContext(Dispatchers.IO) {
        database.maliciousUrlDao().getCountBySource(source)
    }

    suspend fun getUrlLastUpdateTime(source: String): Long? = withContext(Dispatchers.IO) {
        database.maliciousUrlDao().getLastUpdateTime(source)
    }

    // Domain operations
    suspend fun findDomainByValue(domain: String): MaliciousDomain? = withContext(Dispatchers.IO) {
        database.maliciousDomainDao().findByDomain(domain)
    }

    suspend fun getDomainCountBySource(source: String): Int = withContext(Dispatchers.IO) {
        database.maliciousDomainDao().getCountBySource(source)
    }

    suspend fun getDomainLastUpdateTime(source: String): Long? = withContext(Dispatchers.IO) {
        database.maliciousDomainDao().getLastUpdateTime(source)
    }

    // IP operations
    suspend fun findIpByValue(ip: String): MaliciousIp? = withContext(Dispatchers.IO) {
        database.maliciousIpDao().findByIp(ip)
    }

    suspend fun getIpCountBySource(source: String): Int = withContext(Dispatchers.IO) {
        database.maliciousIpDao().getCountBySource(source)
    }

    suspend fun getIpLastUpdateTime(source: String): Long? = withContext(Dispatchers.IO) {
        database.maliciousIpDao().getLastUpdateTime(source)
    }

    // Hash operations
    suspend fun findHashByValue(hash: String): MaliciousHash? = withContext(Dispatchers.IO) {
        database.maliciousHashDao().findByHash(hash)
    }

    suspend fun getHashCountBySource(source: String): Int = withContext(Dispatchers.IO) {
        database.maliciousHashDao().getCountBySource(source)
    }

    suspend fun getHashLastUpdateTime(source: String): Long? = withContext(Dispatchers.IO) {
        database.maliciousHashDao().getLastUpdateTime(source)
    }

    // Save feed records
    suspend fun saveFeedRecords(source: String, records: List<ThreatRecord>): Int = withContext(Dispatchers.IO) {
        val urls = mutableListOf<MaliciousUrl>()
        val domains = mutableListOf<MaliciousDomain>()
        val ips = mutableListOf<MaliciousIp>()
        val hashes = mutableListOf<MaliciousHash>()

        records.forEach { record ->
            when {
                record.value.startsWith("http://") || record.value.startsWith("https://") -> {
                    // It's a URL
                    urls.add(MaliciousUrl(record.value, record.source, record.timestamp))

                    // Also extract and save domain
                    try {
                        val url = URL(record.value)
                        val domain = url.host
                        if (domain.isNotBlank()) {
                            domains.add(MaliciousDomain(domain, record.source, record.timestamp))
                        }
                    } catch (e: Exception) {
                        // Invalid URL, skip domain extraction
                    }
                }
                record.value.matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) -> {
                    // It's an IP
                    ips.add(MaliciousIp(record.value, record.source, record.timestamp))
                }
                record.value.matches(Regex("[a-fA-F0-9]{32,64}")) -> {
                    // It's a hash (MD5, SHA1, or SHA256)
                    hashes.add(MaliciousHash(record.value.lowercase(), record.source, record.timestamp))
                }
                else -> {
                    // Assume it's a domain
                    domains.add(MaliciousDomain(record.value, record.source, record.timestamp))
                }
            }
        }

        // Delete old records for this source
        if (urls.isNotEmpty()) {
            database.maliciousUrlDao().deleteBySource(source)
            database.maliciousUrlDao().insertAll(urls)
        }
        if (domains.isNotEmpty()) {
            database.maliciousDomainDao().deleteBySource(source)
            database.maliciousDomainDao().insertAll(domains)
        }
        if (ips.isNotEmpty()) {
            database.maliciousIpDao().deleteBySource(source)
            database.maliciousIpDao().insertAll(ips)
        }
        if (hashes.isNotEmpty()) {
            database.maliciousHashDao().deleteBySource(source)
            database.maliciousHashDao().insertAll(hashes)
        }

        records.size
    }
}
