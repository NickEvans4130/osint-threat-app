package com.example.osint.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import com.example.osint.domain.model.SubnetInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket

object NetworkUtils {

    suspend fun getCurrentSubnetInfo(context: Context): SubnetInfo? = withContext(Dispatchers.IO) {
        try {
            // Try modern approach first (Android 10+)
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork)

            if (linkProperties != null) {
                val linkAddress = linkProperties.linkAddresses.firstOrNull {
                    it.address is Inet4Address && !it.address.isLoopbackAddress
                }

                if (linkAddress != null) {
                    val localIp = linkAddress.address.hostAddress ?: return@withContext null
                    val prefixLength = linkAddress.prefixLength
                    val subnetMask = cidrToSubnetMask(prefixLength)

                    val networkAddress = calculateNetworkAddress(localIp, subnetMask)
                    val ipRange = calculateIpRange(networkAddress, subnetMask)
                    val totalHosts = calculateTotalHosts(subnetMask)

                    return@withContext SubnetInfo(
                        localIpAddress = localIp,
                        subnetMask = subnetMask,
                        networkAddress = networkAddress,
                        cidrNotation = "$networkAddress/$prefixLength",
                        ipRange = ipRange,
                        totalHosts = totalHosts
                    )
                }
            }

            // Fallback to legacy WiFi manager approach
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcpInfo = wifiManager.dhcpInfo

            if (dhcpInfo.ipAddress != 0) {
                val localIp = intToIpAddress(dhcpInfo.ipAddress)
                val subnetMask = intToIpAddress(dhcpInfo.netmask)

                if (!localIp.isNullOrBlank() && !subnetMask.isNullOrBlank()) {
                    val networkAddress = calculateNetworkAddress(localIp, subnetMask)
                    val cidr = calculateCIDR(subnetMask)
                    val ipRange = calculateIpRange(networkAddress, subnetMask)
                    val totalHosts = calculateTotalHosts(subnetMask)

                    return@withContext SubnetInfo(
                        localIpAddress = localIp,
                        subnetMask = subnetMask,
                        networkAddress = networkAddress,
                        cidrNotation = "$networkAddress/$cidr",
                        ipRange = ipRange,
                        totalHosts = totalHosts
                    )
                }
            }

            // Last resort: enumerate network interfaces
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (address is Inet4Address && !address.isLoopbackAddress) {
                            val localIp = address.hostAddress ?: continue
                            val interfaceAddresses = networkInterface.interfaceAddresses
                            val interfaceAddress = interfaceAddresses.firstOrNull {
                                it.address is Inet4Address
                            }

                            if (interfaceAddress != null) {
                                val prefixLength = interfaceAddress.networkPrefixLength.toInt()
                                val subnetMask = cidrToSubnetMask(prefixLength)
                                val networkAddress = calculateNetworkAddress(localIp, subnetMask)
                                val ipRange = calculateIpRange(networkAddress, subnetMask)
                                val totalHosts = calculateTotalHosts(subnetMask)

                                return@withContext SubnetInfo(
                                    localIpAddress = localIp,
                                    subnetMask = subnetMask,
                                    networkAddress = networkAddress,
                                    cidrNotation = "$networkAddress/$prefixLength",
                                    ipRange = ipRange,
                                    totalHosts = totalHosts
                                )
                            }
                        }
                    }
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun probeHost(
        ipAddress: String,
        ports: List<Int> = listOf(80, 443, 22, 3389),
        timeoutMs: Int = 1000
    ): Pair<Boolean, List<Int>> = withContext(Dispatchers.IO) {
        val openPorts = mutableListOf<Int>()
        var isAlive = false

        for (port in ports) {
            try {
                Socket().use { socket ->
                    socket.connect(
                        java.net.InetSocketAddress(ipAddress, port),
                        timeoutMs
                    )
                    openPorts.add(port)
                    isAlive = true
                }
            } catch (e: Exception) {
                // Port closed or timeout
            }
        }

        Pair(isAlive, openPorts)
    }

    suspend fun grabBanner(
        ipAddress: String,
        port: Int,
        timeoutMs: Int = 1000
    ): String? = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.soTimeout = timeoutMs
                socket.connect(
                    java.net.InetSocketAddress(ipAddress, port),
                    timeoutMs
                )

                val input = socket.getInputStream().bufferedReader()
                val output = socket.getOutputStream().bufferedWriter()

                // Try to grab banner (some services send banner immediately)
                if (input.ready()) {
                    return@withContext input.readLine()
                }

                // Send HTTP request for HTTP ports
                if (port == 80 || port == 443 || port == 8080) {
                    output.write("GET / HTTP/1.0\r\n\r\n")
                    output.flush()
                    return@withContext input.readLine()
                }

                // For SSH, just read the banner
                if (port == 22) {
                    return@withContext input.readLine()
                }

                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun intToIpAddress(ip: Int): String? {
        return try {
            val bytes = ByteArray(4)
            bytes[0] = (ip and 0xff).toByte()
            bytes[1] = (ip shr 8 and 0xff).toByte()
            bytes[2] = (ip shr 16 and 0xff).toByte()
            bytes[3] = (ip shr 24 and 0xff).toByte()
            InetAddress.getByAddress(bytes).hostAddress
        } catch (e: Exception) {
            null
        }
    }

    private fun cidrToSubnetMask(cidr: Int): String {
        val mask = (0xffffffff.toInt() shl (32 - cidr))
        return "${(mask shr 24) and 0xff}.${(mask shr 16) and 0xff}.${(mask shr 8) and 0xff}.${mask and 0xff}"
    }

    private fun calculateNetworkAddress(ip: String, mask: String): String {
        val ipParts = ip.split(".").map { it.toInt() }
        val maskParts = mask.split(".").map { it.toInt() }

        val networkParts = ipParts.zip(maskParts) { ipPart, maskPart ->
            ipPart and maskPart
        }

        return networkParts.joinToString(".")
    }

    private fun calculateCIDR(mask: String): Int {
        val maskParts = mask.split(".").map { it.toInt() }
        return maskParts.sumOf { Integer.bitCount(it) }
    }

    private fun calculateIpRange(networkAddress: String, mask: String): Pair<String, String> {
        val networkParts = networkAddress.split(".").map { it.toInt() }
        val maskParts = mask.split(".").map { it.toInt() }

        val broadcastParts = networkParts.zip(maskParts) { netPart, maskPart ->
            netPart or (255 - maskPart)
        }

        val firstHost = networkParts.toMutableList()
        firstHost[3] = firstHost[3] + 1

        val lastHost = broadcastParts.toMutableList()
        lastHost[3] = lastHost[3] - 1

        return Pair(
            firstHost.joinToString("."),
            lastHost.joinToString(".")
        )
    }

    private fun calculateTotalHosts(mask: String): Int {
        val cidr = calculateCIDR(mask)
        val hostBits = 32 - cidr
        return (1 shl hostBits) - 2 // Subtract network and broadcast addresses
    }

    fun generateIpRange(startIp: String, endIp: String): List<String> {
        val ips = mutableListOf<String>()
        val startParts = startIp.split(".").map { it.toInt() }
        val endParts = endIp.split(".").map { it.toInt() }

        // For simplicity, assuming same /24 network
        val base = "${startParts[0]}.${startParts[1]}.${startParts[2]}"
        val start = startParts[3]
        val end = endParts[3]

        for (i in start..end) {
            ips.add("$base.$i")
        }

        return ips
    }
}
