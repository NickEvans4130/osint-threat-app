package com.example.osint.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest

object FileHashUtils {

    suspend fun calculateSHA256(inputStream: InputStream): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var bytesRead: Int

        inputStream.use { stream ->
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        digest.digest().joinToString("") { "%02x".format(it) }
    }
}
