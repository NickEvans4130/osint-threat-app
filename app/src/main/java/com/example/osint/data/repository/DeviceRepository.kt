package com.example.osint.data.repository

import android.content.Context
import com.example.osint.domain.model.DeviceInfo
import com.example.osint.domain.model.NetworkHost
import com.example.osint.utils.DeviceTypeResolver
import com.example.osint.utils.OUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository(private val context: Context) {

    private val scannedDevices = mutableListOf<NetworkHost>()

    suspend fun initOUIDatabase() {
        OUIParser.loadOUIDatabase(context)
    }

    fun saveScannedDevices(devices: List<NetworkHost>) {
        scannedDevices.clear()
        scannedDevices.addAll(devices)
    }

    suspend fun getDeviceInfo(networkHost: NetworkHost): DeviceInfo = withContext(Dispatchers.IO) {
        val macAddress = OUIParser.getMacAddress(context, networkHost.ipAddress)
        val vendor = OUIParser.lookupVendor(macAddress)
        val deviceType = DeviceTypeResolver.resolveDeviceType(vendor, networkHost.openPorts)
        val hostname = getHostname(networkHost.ipAddress)

        DeviceInfo(
            ipAddress = networkHost.ipAddress,
            macAddress = macAddress,
            hostname = hostname,
            vendor = vendor,
            deviceType = deviceType,
            isOnline = networkHost.isAlive,
            openPorts = networkHost.openPorts,
            banners = networkHost.banners,
            scanTimestamp = networkHost.scanTimestamp
        )
    }

    suspend fun getAllDevices(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        scannedDevices.map { getDeviceInfo(it) }
    }

    suspend fun getDeviceByIp(ipAddress: String): DeviceInfo? = withContext(Dispatchers.IO) {
        val host = scannedDevices.firstOrNull { it.ipAddress == ipAddress }
        host?.let { getDeviceInfo(it) }
    }

    private fun getHostname(ipAddress: String): String? {
        return try {
            val addr = java.net.InetAddress.getByName(ipAddress)
            val hostname = addr.hostName
            if (hostname != ipAddress) hostname else null
        } catch (e: Exception) {
            null
        }
    }
}
