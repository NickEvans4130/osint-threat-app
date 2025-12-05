package com.example.osint.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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

    fun copyUriToInternalStorage(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val outputFile = File(context.cacheDir, fileName)

            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            outputFile
        } catch (e: Exception) {
            null
        }
    }
}
