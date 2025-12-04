package com.example.osint.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.osint.domain.model.DocumentMetadata
import com.example.osint.domain.model.FileMetadata
import com.example.osint.domain.model.ImageMetadata
import com.example.osint.utils.DocumentMetadataUtils
import com.example.osint.utils.ExifUtils
import com.example.osint.utils.FileHashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MetadataRepository(private val context: Context) {

    suspend fun parseFileMetadata(uri: Uri): FileMetadata = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val fileName = getFileName(uri)
        val fileSize = getFileSize(uri)
        val mimeType = contentResolver.getType(uri)

        val hash = contentResolver.openInputStream(uri)?.use { stream ->
            FileHashUtils.calculateSHA256(stream)
        } ?: ""

        val imageMetadata = if (isImageFile(mimeType)) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    ExifUtils.extractImageMetadata(stream)
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val documentMetadata = if (isDocumentFile(mimeType)) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    DocumentMetadataUtils.extractDocumentMetadata(stream, mimeType)
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        FileMetadata(
            fileName = fileName,
            filePath = uri.toString(),
            fileSize = fileSize,
            mimeType = mimeType,
            sha256Hash = hash,
            imageMetadata = imageMetadata,
            documentMetadata = documentMetadata
        )
    }

    suspend fun stripImageExif(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(uri)
            val outputFile = File(context.cacheDir, "stripped_${System.currentTimeMillis()}_$fileName")

            contentResolver.openInputStream(uri)?.use { inputStream ->
                val imageBytes = inputStream.readBytes()
                outputFile.writeBytes(imageBytes)
            }

            val strippedFile = File(context.cacheDir, "final_stripped_${System.currentTimeMillis()}_$fileName")
            outputFile.inputStream().use { input ->
                strippedFile.outputStream().use { output ->
                    ExifUtils.stripExifData(input, output)
                }
            }

            outputFile.delete()
            Uri.fromFile(strippedFile)
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                it.getString(nameIndex)
            } else {
                "unknown"
            }
        } ?: "unknown"
    }

    private fun getFileSize(uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (it.moveToFirst() && sizeIndex >= 0) {
                it.getLong(sizeIndex)
            } else {
                0L
            }
        } ?: 0L
    }

    private fun isImageFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }

    private fun isDocumentFile(mimeType: String?): Boolean {
        return mimeType?.contains("wordprocessingml") == true ||
                mimeType?.contains("spreadsheetml") == true ||
                mimeType?.contains("presentationml") == true ||
                mimeType?.contains("msword") == true ||
                mimeType?.contains("excel") == true ||
                mimeType?.contains("powerpoint") == true ||
                mimeType?.contains("pdf") == true
    }
}
