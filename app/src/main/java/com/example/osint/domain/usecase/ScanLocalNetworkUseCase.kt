package com.example.osint.domain.usecase

import com.example.osint.data.repository.NetworkScannerRepository
import com.example.osint.domain.model.NetworkHost
import com.example.osint.domain.model.NetworkScanProgress
import com.example.osint.domain.model.SubnetInfo
import com.example.osint.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScanLocalNetworkUseCase(
    private val repository: NetworkScannerRepository,
    private val probeHostUseCase: ProbeHostUseCase
) {
    private val concurrencyLimit = Semaphore(20) // Max 20 concurrent scans

    operator fun invoke(ports: List<Int> = listOf(80, 443, 22, 3389)): Flow<NetworkScanProgress> = channelFlow {
        // Get subnet info
        val subnetInfo = repository.getCurrentSubnet()
            ?: throw IllegalStateException("Unable to determine network subnet")

        // Generate IP range
        val ipRange = NetworkUtils.generateIpRange(
            subnetInfo.ipRange.first,
            subnetInfo.ipRange.second
        )

        val totalHosts = ipRange.size
        val foundHosts = mutableListOf<NetworkHost>()
        var scannedCount = 0
        val mutex = Mutex()

        // Emit initial progress
        send(
            NetworkScanProgress(
                totalHosts = totalHosts,
                scannedHosts = 0,
                currentIp = null,
                foundHosts = emptyList()
            )
        )

        withContext(Dispatchers.IO) {
            // Scan hosts in batches
            ipRange.chunked(20).forEach { batch ->
                val results = batch.map { ip ->
                    async {
                        concurrencyLimit.acquire()
                        try {
                            val host = probeHostUseCase(ip, ports)

                            mutex.withLock {
                                if (host.isAlive) {
                                    foundHosts.add(host)
                                }
                                scannedCount++

                                // Emit progress after scanning
                                send(
                                    NetworkScanProgress(
                                        totalHosts = totalHosts,
                                        scannedHosts = scannedCount,
                                        currentIp = if (scannedCount < totalHosts) ip else null,
                                        foundHosts = foundHosts.toList()
                                    )
                                )
                            }
                        } finally {
                            concurrencyLimit.release()
                        }
                    }
                }

                results.awaitAll()
            }
        }

        // Emit final progress
        send(
            NetworkScanProgress(
                totalHosts = totalHosts,
                scannedHosts = totalHosts,
                currentIp = null,
                foundHosts = foundHosts.toList()
            )
        )
    }
}
