package com.example.osint.utils

import android.content.Context
import android.net.Uri
import com.example.osint.domain.model.SecureDeletionMethod
import com.example.osint.domain.model.SecureDeletionProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom
import kotlin.math.min

class SecureFileDeleter(private val context: Context) {

    private val random = SecureRandom()
    private val bufferSize = 64 * 1024 // 64KB buffer for efficient I/O

    suspend fun secureDeleteFile(
        file: File,
        method: SecureDeletionMethod
    ): Flow<SecureDeletionProgress> = flow {
        val startTime = System.currentTimeMillis()
        val fileSize = file.length()
        val totalPasses = method.passes

        withContext(Dispatchers.IO) {
            try {
                RandomAccessFile(file, "rws").use { raf ->
                    for (pass in 0 until totalPasses) {
                        raf.seek(0)
                        var bytesWritten = 0L

                        while (bytesWritten < fileSize) {
                            val remaining = fileSize - bytesWritten
                            val chunkSize = min(bufferSize.toLong(), remaining).toInt()
                            val buffer = ByteArray(chunkSize)

                            // Fill buffer based on pass pattern
                            fillBufferForPass(buffer, pass, method)

                            // Write buffer to file
                            raf.write(buffer)
                            bytesWritten += chunkSize

                            // Emit progress
                            val elapsed = System.currentTimeMillis() - startTime
                            val bytesPerMs = if (elapsed > 0) bytesWritten.toDouble() / elapsed else 0.0
                            val remainingBytes = (fileSize * (totalPasses - pass)) - bytesWritten
                            val estimatedRemaining = if (bytesPerMs > 0) (remainingBytes / bytesPerMs).toLong() else 0L

                            emit(
                                SecureDeletionProgress(
                                    currentFile = file.name,
                                    currentPass = pass,
                                    totalPasses = totalPasses,
                                    bytesProcessed = bytesWritten,
                                    totalBytes = fileSize,
                                    filesDeleted = 0,
                                    totalFiles = 1,
                                    elapsedTimeMs = elapsed,
                                    estimatedTimeRemainingMs = estimatedRemaining
                                )
                            )
                        }

                        // Sync to ensure data is written to disk
                        raf.fd.sync()
                    }
                }

                // After overwriting, delete the file
                file.delete()

            } catch (e: Exception) {
                throw SecureDeletionException("Failed to securely delete file: ${e.message}", e)
            }
        }
    }

    suspend fun secureDeleteFiles(
        files: List<File>,
        method: SecureDeletionMethod
    ): Flow<SecureDeletionProgress> = flow {
        val startTime = System.currentTimeMillis()
        val totalFiles = files.size
        val totalBytes = files.sumOf { it.length() }
        var filesDeleted = 0
        var totalBytesProcessed = 0L

        for (file in files) {
            if (!file.exists()) {
                continue
            }

            val fileSize = file.length()
            val totalPasses = method.passes

            try {
                withContext(Dispatchers.IO) {
                    RandomAccessFile(file, "rws").use { raf ->
                        for (pass in 0 until totalPasses) {
                            raf.seek(0)
                            var bytesWritten = 0L

                            while (bytesWritten < fileSize) {
                                val remaining = fileSize - bytesWritten
                                val chunkSize = min(bufferSize.toLong(), remaining).toInt()
                                val buffer = ByteArray(chunkSize)

                                fillBufferForPass(buffer, pass, method)
                                raf.write(buffer)
                                bytesWritten += chunkSize
                                totalBytesProcessed += chunkSize

                                // Emit progress
                                val elapsed = System.currentTimeMillis() - startTime
                                val bytesPerMs = if (elapsed > 0) totalBytesProcessed.toDouble() / elapsed else 0.0
                                val remainingBytes = totalBytes - totalBytesProcessed
                                val estimatedRemaining = if (bytesPerMs > 0) (remainingBytes / bytesPerMs).toLong() else 0L

                                emit(
                                    SecureDeletionProgress(
                                        currentFile = file.name,
                                        currentPass = pass,
                                        totalPasses = totalPasses,
                                        bytesProcessed = bytesWritten,
                                        totalBytes = fileSize,
                                        filesDeleted = filesDeleted,
                                        totalFiles = totalFiles,
                                        elapsedTimeMs = elapsed,
                                        estimatedTimeRemainingMs = estimatedRemaining
                                    )
                                )
                            }

                            raf.fd.sync()
                        }
                    }

                    file.delete()
                }

                filesDeleted++

                // Emit completion for this file
                emit(
                    SecureDeletionProgress(
                        currentFile = file.name,
                        currentPass = totalPasses - 1,
                        totalPasses = totalPasses,
                        bytesProcessed = fileSize,
                        totalBytes = fileSize,
                        filesDeleted = filesDeleted,
                        totalFiles = totalFiles,
                        elapsedTimeMs = System.currentTimeMillis() - startTime,
                        estimatedTimeRemainingMs = 0L
                    )
                )

            } catch (e: Exception) {
                // Continue with next file on error
            }
        }
    }

    private fun fillBufferForPass(buffer: ByteArray, pass: Int, method: SecureDeletionMethod) {
        when (method) {
            SecureDeletionMethod.SINGLE_PASS -> {
                buffer.fill(0)
            }
            SecureDeletionMethod.DOD_5220_22_M -> {
                when (pass) {
                    0 -> buffer.fill(0) // All zeros
                    1 -> buffer.fill(0xFF.toByte()) // All ones
                    2 -> random.nextBytes(buffer) // Random data
                }
            }
            SecureDeletionMethod.RANDOM_DATA -> {
                random.nextBytes(buffer)
            }
            SecureDeletionMethod.GUTMANN -> {
                when (pass) {
                    in 0..3 -> random.nextBytes(buffer)
                    4 -> buffer.fill(0x55.toByte())
                    5 -> buffer.fill(0xAA.toByte())
                    in 6..8 -> fillGutmannPattern(buffer, pass - 6)
                    in 9..24 -> fillGutmannPattern(buffer, pass - 9)
                    in 25..27 -> fillGutmannPattern(buffer, pass - 25)
                    in 28..30 -> random.nextBytes(buffer)
                    else -> random.nextBytes(buffer)
                }
            }
        }
    }

    private fun fillGutmannPattern(buffer: ByteArray, patternIndex: Int) {
        val patterns = arrayOf(
            byteArrayOf(0x92.toByte(), 0x49.toByte(), 0x24.toByte()),
            byteArrayOf(0x49.toByte(), 0x24.toByte(), 0x92.toByte()),
            byteArrayOf(0x24.toByte(), 0x92.toByte(), 0x49.toByte()),
            byteArrayOf(0x00, 0x00, 0x00),
            byteArrayOf(0x11, 0x11, 0x11),
            byteArrayOf(0x22, 0x22, 0x22),
            byteArrayOf(0x33, 0x33, 0x33),
            byteArrayOf(0x44, 0x44, 0x44),
            byteArrayOf(0x55, 0x55, 0x55),
            byteArrayOf(0x66, 0x66, 0x66),
            byteArrayOf(0x77, 0x77, 0x77),
            byteArrayOf(0x88.toByte(), 0x88.toByte(), 0x88.toByte()),
            byteArrayOf(0x99.toByte(), 0x99.toByte(), 0x99.toByte()),
            byteArrayOf(0xAA.toByte(), 0xAA.toByte(), 0xAA.toByte()),
            byteArrayOf(0xBB.toByte(), 0xBB.toByte(), 0xBB.toByte()),
            byteArrayOf(0xCC.toByte(), 0xCC.toByte(), 0xCC.toByte())
        )

        val pattern = patterns[patternIndex % patterns.size]
        for (i in buffer.indices) {
            buffer[i] = pattern[i % pattern.size]
        }
    }

    fun getFileFromUri(uri: Uri): File? {
        return try {
            val path = uri.path ?: return null
            File(path)
        } catch (e: Exception) {
            null
        }
    }
}

class SecureDeletionException(message: String, cause: Throwable? = null) : Exception(message, cause)
