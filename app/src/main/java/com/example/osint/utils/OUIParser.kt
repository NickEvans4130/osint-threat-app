package com.example.osint.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object OUIParser {

    private var ouiDatabase: Map<String, String>? = null

    suspend fun loadOUIDatabase(context: Context): Map<String, String> = withContext(Dispatchers.IO) {
        if (ouiDatabase != null) {
            return@withContext ouiDatabase!!
        }

        val database = mutableMapOf<String, String>()

        try {
            val inputStream = context.assets.open("oui.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(",", limit = 2)
                    if (parts.size == 2) {
                        val mac = parts[0].trim().replace(":", "").uppercase()
                        val vendor = parts[1].trim()
                        database[mac] = vendor
                    }
                }
            }

            ouiDatabase = database
            database
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun lookupVendor(macAddress: String?): String {
        if (macAddress == null || macAddress.length < 8) {
            return "Unknown Vendor"
        }

        val prefix = macAddress.replace(":", "").replace("-", "").uppercase().take(6)
        return ouiDatabase?.get(prefix) ?: "Unknown Vendor"
    }

    suspend fun getMacAddress(context: Context, ipAddress: String): String? = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("ip neigh show $ipAddress")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()

            val macRegex = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})".toRegex()
            val match = macRegex.find(output)
            match?.value
        } catch (e: Exception) {
            null
        }
    }
}
