package com.example.osint.domain.usecase

import com.example.osint.data.repository.ThreatRepository
import com.example.osint.domain.model.QueryType
import com.example.osint.domain.model.ThreatResult
import java.net.InetAddress
import java.net.URL

class ScanUrlUseCase(
    private val repository: ThreatRepository,
    private val computeRiskScoreUseCase: ComputeRiskScoreUseCase
) {
    suspend operator fun invoke(urlString: String): ThreatResult {
        val foundInFeeds = mutableListOf<String>()
        var urlMatch = false
        var domainMatch = false
        var ipMatch = false
        var domain: String? = null
        var resolvedIp: String? = null

        try {
            // Parse URL and extract domain
            val url = URL(urlString)
            domain = url.host

            // Check if URL exists in malicious URLs
            val urlRecord = repository.findUrlByValue(urlString)
            if (urlRecord != null) {
                foundInFeeds.add("URL: ${urlRecord.source}")
                urlMatch = true
            }

            // Check if domain exists in malicious domains
            if (domain != null) {
                val domainRecord = repository.findDomainByValue(domain)
                if (domainRecord != null) {
                    foundInFeeds.add("Domain: ${domainRecord.source}")
                    domainMatch = true
                }

                // Resolve IP
                try {
                    val address = InetAddress.getByName(domain)
                    resolvedIp = address.hostAddress

                    // Check if IP exists in malicious IPs
                    if (resolvedIp != null) {
                        val ipRecord = repository.findIpByValue(resolvedIp)
                        if (ipRecord != null) {
                            foundInFeeds.add("IP: ${ipRecord.source}")
                            ipMatch = true
                        }
                    }
                } catch (e: Exception) {
                    // DNS resolution failed, continue without IP check
                }
            }

            val isThreat = foundInFeeds.isNotEmpty()
            val riskScore = if (isThreat) {
                computeRiskScoreUseCase(
                    foundInFeedCount = foundInFeeds.size,
                    domain = domain
                )
            } else null

            return ThreatResult(
                query = urlString,
                queryType = QueryType.URL,
                isThreat = isThreat,
                foundInFeeds = foundInFeeds,
                domain = domain,
                resolvedIp = resolvedIp,
                urlMatch = urlMatch,
                domainMatch = domainMatch,
                ipMatch = ipMatch,
                riskScore = riskScore
            )
        } catch (e: Exception) {
            return ThreatResult(
                query = urlString,
                queryType = QueryType.URL,
                isThreat = false,
                foundInFeeds = emptyList(),
                domain = domain,
                resolvedIp = resolvedIp
            )
        }
    }
}
