package com.example.osint.data.repository

import android.content.Context
import com.example.osint.domain.model.NetworkHost
import com.example.osint.domain.model.SubnetInfo
import com.example.osint.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NetworkScannerRepository(private val context: Context) {

    suspend fun getCurrentSubnet(): SubnetInfo? {
        return NetworkUtils.getCurrentSubnetInfo(context)
    }

    suspend fun probeHost(ipAddress: String, ports: List<Int>): NetworkHost {
        val (isAlive, openPorts) = NetworkUtils.probeHost(ipAddress, ports)

        val banners = mutableMapOf<Int, String>()
        if (isAlive && openPorts.isNotEmpty()) {
            // Grab banners for open ports
            openPorts.forEach { port ->
                val banner = NetworkUtils.grabBanner(ipAddress, port)
                if (banner != null) {
                    banners[port] = banner
                }
            }
        }

        return NetworkHost(
            ipAddress = ipAddress,
            isAlive = isAlive,
            openPorts = openPorts,
            banners = banners
        )
    }
}
