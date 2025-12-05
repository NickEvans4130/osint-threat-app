package com.example.osint.data.repository

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.result.IntentSenderRequest
import com.example.osint.domain.model.FileToDelete
import com.example.osint.domain.model.SecureDeletionMethod
import com.example.osint.domain.model.SecureDeletionProgress
import com.example.osint.domain.model.SecureDeletionResult
import com.example.osint.utils.SecureFileDeleter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.withContext
import java.io.File

class SecureDeletionRepository(private val context: Context) {

    private val deleter = SecureFileDeleter(context)

    suspend fun getFileInfo(uri: Uri): FileToDelete? = withContext(Dispatchers.IO) {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

                    val name = if (nameIndex != -1) it.getString(nameIndex) else "Unknown"
                    val size = if (sizeIndex != -1) it.getLong(sizeIndex) else 0L
                    val path = uri.path ?: ""

                    return@withContext FileToDelete(uri, name, size, path)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun secureDeleteFile(
        file: File,
        method: SecureDeletionMethod
    ): Flow<SecureDeletionProgress> {
        return deleter.secureDeleteFile(file, method)
    }

    suspend fun secureDeleteFiles(
        files: List<File>,
        method: SecureDeletionMethod
    ): Flow<SecureDeletionProgress> {
        return deleter.secureDeleteFiles(files, method)
    }

    suspend fun performSecureDeletion(
        files: List<File>,
        method: SecureDeletionMethod
    ): SecureDeletionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val totalSize = files.sumOf { it.length() }
        val failedFiles = mutableListOf<String>()
        var successCount = 0

        try {
            val lastProgress = deleter.secureDeleteFiles(files, method)
                .catch { e ->
                    // Handle errors
                }
                .lastOrNull()

            successCount = lastProgress?.filesDeleted ?: 0

            // Identify failed files
            files.forEach { file ->
                if (file.exists()) {
                    failedFiles.add(file.name)
                }
            }

            SecureDeletionResult(
                success = failedFiles.isEmpty(),
                filesDeleted = successCount,
                totalSizeBytes = totalSize,
                durationMs = System.currentTimeMillis() - startTime,
                method = method,
                failedFiles = failedFiles
            )
        } catch (e: Exception) {
            SecureDeletionResult(
                success = false,
                filesDeleted = successCount,
                totalSizeBytes = totalSize,
                durationMs = System.currentTimeMillis() - startTime,
                method = method,
                failedFiles = files.map { it.name },
                error = e.message
            )
        }
    }

    fun getFileFromUri(uri: Uri): File? {
        return deleter.getFileFromUri(uri)
    }

    suspend fun copyUriToInternalStorage(uri: Uri, fileName: String): File? = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SecureDeletion", "Copying URI to cache: $uri")

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.util.Log.e("SecureDeletion", "Failed to open input stream for: $uri")
                return@withContext null
            }

            val outputFile = File(context.cacheDir, "secure_delete_$fileName")
            android.util.Log.d("SecureDeletion", "Output file: ${outputFile.absolutePath}")

            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    val bytesCopied = input.copyTo(output)
                    android.util.Log.d("SecureDeletion", "Copied $bytesCopied bytes")
                }
            }

            if (outputFile.exists() && outputFile.length() > 0) {
                android.util.Log.d("SecureDeletion", "✓ Cache file created: ${outputFile.length()} bytes")
                outputFile
            } else {
                android.util.Log.e("SecureDeletion", "✗ Cache file invalid or empty")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SecureDeletion", "Error copying to cache: ${e.message}", e)
            null
        }
    }

    suspend fun deleteOriginalFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SecureDeletion", "Attempting to delete URI: $uri")

            // Try direct deletion first
            val result = context.contentResolver.delete(uri, null, null)
            android.util.Log.d("SecureDeletion", "Delete result: $result rows")

            if (result > 0) {
                return@withContext true
            }

            // If direct deletion failed and we're on Android 11+, we need user permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.util.Log.d("SecureDeletion", "Direct delete failed, Android 11+ requires user permission")
                // We can't request permission from repository, need to handle in UI layer
                return@withContext false
            }

            false
        } catch (e: SecurityException) {
            android.util.Log.e("SecureDeletion", "SecurityException: App lacks permission to delete this file", e)
            false
        } catch (e: Exception) {
            android.util.Log.e("SecureDeletion", "Error deleting file: ${e.message}", e)
            false
        }
    }

    fun createDeleteRequest(uris: List<Uri>): IntentSender? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                pendingIntent.intentSender
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SecureDeletion", "Error creating delete request: ${e.message}", e)
            null
        }
    }
}
