package com.example.osint.domain.model

data class FileMetadata(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String?,
    val sha256Hash: String,
    val imageMetadata: ImageMetadata?,
    val documentMetadata: DocumentMetadata?
)

data class ImageMetadata(
    val width: Int?,
    val height: Int?,
    val make: String?,
    val model: String?,
    val dateTime: String?,
    val orientation: String?,
    val software: String?,
    val gpsLatitude: Double?,
    val gpsLongitude: Double?,
    val gpsAltitude: Double?,
    val flash: String?,
    val focalLength: String?,
    val exposureTime: String?,
    val aperture: String?,
    val iso: String?,
    val whiteBalance: String?,
    val allTags: Map<String, String>
)

data class DocumentMetadata(
    val author: String?,
    val title: String?,
    val subject: String?,
    val keywords: String?,
    val creator: String?,
    val producer: String?,
    val creationDate: String?,
    val modificationDate: String?,
    val pageCount: Int?,
    val application: String?,
    val allProperties: Map<String, String>
)
