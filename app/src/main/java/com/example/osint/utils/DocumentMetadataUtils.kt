package com.example.osint.utils

import com.example.osint.domain.model.DocumentMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ooxml.POIXMLDocument
import org.apache.poi.ooxml.POIXMLProperties
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xslf.usermodel.XMLSlideShow
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentMetadataUtils {

    suspend fun extractDocumentMetadata(inputStream: InputStream, mimeType: String?): DocumentMetadata = withContext(Dispatchers.IO) {
        try {
            when {
                mimeType?.contains("wordprocessingml") == true || mimeType?.contains("msword") == true -> {
                    extractWordMetadata(inputStream)
                }
                mimeType?.contains("spreadsheetml") == true || mimeType?.contains("excel") == true -> {
                    extractExcelMetadata(inputStream)
                }
                mimeType?.contains("presentationml") == true || mimeType?.contains("powerpoint") == true -> {
                    extractPowerPointMetadata(inputStream)
                }
                else -> {
                    DocumentMetadata(
                        author = null,
                        title = null,
                        subject = null,
                        keywords = null,
                        creator = null,
                        producer = null,
                        creationDate = null,
                        modificationDate = null,
                        pageCount = null,
                        application = null,
                        allProperties = emptyMap()
                    )
                }
            }
        } catch (e: Exception) {
            DocumentMetadata(
                author = null,
                title = null,
                subject = null,
                keywords = null,
                creator = null,
                producer = null,
                creationDate = null,
                modificationDate = null,
                pageCount = null,
                application = null,
                allProperties = mapOf("Error" to e.message.orEmpty())
            )
        }
    }

    private fun extractWordMetadata(inputStream: InputStream): DocumentMetadata {
        val document = XWPFDocument(inputStream)
        val properties = document.properties
        return extractPOIXMLMetadata(properties, document.bodyElements.size)
    }

    private fun extractExcelMetadata(inputStream: InputStream): DocumentMetadata {
        val workbook = XSSFWorkbook(inputStream)
        val properties = workbook.properties
        val pageCount = workbook.numberOfSheets
        return extractPOIXMLMetadata(properties, pageCount)
    }

    private fun extractPowerPointMetadata(inputStream: InputStream): DocumentMetadata {
        val presentation = XMLSlideShow(inputStream)
        val properties = presentation.properties
        val pageCount = presentation.slides.size
        return extractPOIXMLMetadata(properties, pageCount)
    }

    private fun extractPOIXMLMetadata(properties: POIXMLProperties, pageCount: Int?): DocumentMetadata {
        val coreProps = properties.coreProperties
        val extendedProps = properties.extendedProperties

        val allProps = mutableMapOf<String, String>()

        coreProps.creator?.let { allProps["Creator"] = it }
        coreProps.title?.let { allProps["Title"] = it }
        coreProps.subject?.let { allProps["Subject"] = it }
        coreProps.keywords?.let { allProps["Keywords"] = it }
        coreProps.description?.let { allProps["Description"] = it }
        coreProps.category?.let { allProps["Category"] = it }
        coreProps.revision?.let { allProps["Revision"] = it }

        val creationDate = coreProps.created?.let { formatDate(it) }
        val modificationDate = coreProps.modified?.let { formatDate(it) }

        creationDate?.let { allProps["Creation Date"] = it }
        modificationDate?.let { allProps["Modification Date"] = it }

        val application = try {
            extendedProps?.underlyingProperties?.application
        } catch (e: Exception) {
            null
        }

        application?.let { allProps["Application"] = it }
        pageCount?.let { allProps["Page Count"] = it.toString() }

        return DocumentMetadata(
            author = coreProps.creator,
            title = coreProps.title,
            subject = coreProps.subject,
            keywords = coreProps.keywords,
            creator = coreProps.creator,
            producer = null,
            creationDate = creationDate,
            modificationDate = modificationDate,
            pageCount = pageCount,
            application = application,
            allProperties = allProps
        )
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}
