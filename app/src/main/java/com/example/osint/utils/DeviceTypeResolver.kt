package com.example.osint.utils

import com.example.osint.domain.model.DeviceType

object DeviceTypeResolver {

    fun resolveDeviceType(vendor: String, openPorts: List<Int>): DeviceType {
        val vendorLower = vendor.lowercase()

        return when {
            isRouter(vendorLower, openPorts) -> DeviceType.ROUTER
            isPhone(vendorLower) -> DeviceType.PHONE
            isTablet(vendorLower) -> DeviceType.TABLET
            isLaptop(vendorLower) -> DeviceType.LAPTOP
            isPrinter(vendorLower, openPorts) -> DeviceType.PRINTER
            isServer(openPorts) -> DeviceType.SERVER
            isIoTDevice(vendorLower, openPorts) -> DeviceType.IOT_DEVICE
            else -> DeviceType.UNKNOWN
        }
    }

    private fun isRouter(vendor: String, openPorts: List<Int>): Boolean {
        val routerVendors = listOf(
            "cisco", "linksys", "netgear", "tp-link", "d-link", "asus", "ubiquiti"
        )
        val hasRouterPorts = openPorts.any { it in listOf(80, 443, 8080) }
        return routerVendors.any { vendor.contains(it) } && hasRouterPorts
    }

    private fun isPhone(vendor: String): Boolean {
        val phoneVendors = listOf(
            "apple", "samsung", "google", "htc", "huawei", "xiaomi", "oneplus", "oppo", "vivo"
        )
        val phoneKeywords = listOf("iphone", "galaxy", "pixel", "android")
        return phoneVendors.any { vendor.contains(it) } || phoneKeywords.any { vendor.contains(it) }
    }

    private fun isTablet(vendor: String): Boolean {
        val tabletKeywords = listOf("ipad", "tablet")
        return tabletKeywords.any { vendor.contains(it) }
    }

    private fun isLaptop(vendor: String): Boolean {
        val laptopVendors = listOf(
            "dell", "hp", "lenovo", "asus", "acer", "msi", "microsoft", "apple"
        )
        return laptopVendors.any { vendor.contains(it) }
    }

    private fun isPrinter(vendor: String, openPorts: List<Int>): Boolean {
        val printerVendors = listOf("hp", "canon", "epson", "brother", "xerox")
        val printerPorts = listOf(515, 631, 9100)
        return printerVendors.any { vendor.contains(it) } || openPorts.any { it in printerPorts }
    }

    private fun isServer(openPorts: List<Int>): Boolean {
        val serverPorts = listOf(22, 3389, 5900, 3306, 5432, 27017, 6379)
        return openPorts.any { it in serverPorts }
    }

    private fun isIoTDevice(vendor: String, openPorts: List<Int>): Boolean {
        val iotVendors = listOf("raspberry", "philips", "nest", "ring", "ecobee", "amazon")
        val iotPorts = listOf(1883, 8883, 5683)
        return iotVendors.any { vendor.contains(it) } || openPorts.any { it in iotPorts }
    }
}
