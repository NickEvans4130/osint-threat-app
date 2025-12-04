package com.example.osint.data.feeds

import com.example.osint.domain.model.ThreatRecord
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName

class UrlHausParser : FeedParser {
    private val gson = Gson()

    override fun parse(content: String, source: String): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        val timestamp = System.currentTimeMillis()

        try {
            // Try JSON format first
            if (content.trim().startsWith("[") || content.trim().startsWith("{")) {
                return parseJson(content, source, timestamp)
            } else {
                return parseCsv(content, source, timestamp)
            }
        } catch (e: Exception) {
            // If parsing fails, return empty list
            return emptyList()
        }
    }

    private fun parseJson(content: String, source: String, timestamp: Long): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        try {
            val jsonElement = JsonParser.parseString(content)
            if (jsonElement.isJsonArray) {
                val array = jsonElement.asJsonArray
                array.forEach { element ->
                    try {
                        val obj = element.asJsonObject
                        val url = obj.get("url")?.asString
                        if (!url.isNullOrBlank()) {
                            records.add(ThreatRecord(url, source, timestamp))
                        }
                    } catch (e: Exception) {
                        // Skip malformed entries
                    }
                }
            }
        } catch (e: Exception) {
            // JSON parsing failed
        }
        return records
    }

    private fun parseCsv(content: String, source: String, timestamp: Long): List<ThreatRecord> {
        val records = mutableListOf<ThreatRecord>()
        content.lines().forEach { line ->
            if (line.isNotBlank() && !line.startsWith("#") && !line.startsWith("id,")) {
                // URLhaus CSV format: id,dateadded,url,url_status,last_online,threat,...
                // Split by comma but handle quoted fields
                val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                if (parts.size >= 3) {
                    val url = parts[2].trim().removeSurrounding("\"")
                    if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                        records.add(ThreatRecord(url, source, timestamp))
                    }
                }
            }
        }
        return records
    }
}

data class UrlHausEntry(
    @SerializedName("url") val url: String
)
