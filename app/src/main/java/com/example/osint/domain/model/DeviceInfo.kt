package com.example.osint.domain.model

data class DeviceInfo(
    val ipAddress: String,
    val macAddress: String?,
    val hostname: String?,
    val vendor: String,
    val deviceType: DeviceType,
    val isOnline: Boolean,
    val openPorts: List<Int>,
    val banners: Map<Int, String>,
    val scanTimestamp: Long
)

enum class DeviceType {
    ROUTER,
    LAPTOP,
    PHONE,
    TABLET,
    PRINTER,
    IOT_DEVICE,
    SERVER,
    UNKNOWN
}
