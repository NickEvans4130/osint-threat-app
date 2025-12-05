package com.example.osint.domain.model

enum class SecureDeletionMethod(
    val displayName: String,
    val passes: Int,
    val description: String,
    val securityLevel: SecurityLevel
) {
    SINGLE_PASS(
        displayName = "Single Pass (Fast)",
        passes = 1,
        description = "Overwrite with zeros once. Fast but offers basic security. Suitable for non-sensitive files.",
        securityLevel = SecurityLevel.BASIC
    ),
    DOD_5220_22_M(
        displayName = "DoD 5220.22-M (3 Pass)",
        passes = 3,
        description = "US Department of Defense standard. Overwrites with 0s, 1s, and random data. Good balance of speed and security.",
        securityLevel = SecurityLevel.STANDARD
    ),
    RANDOM_DATA(
        displayName = "Random Data (7 Pass)",
        passes = 7,
        description = "Seven passes of random data. Excellent security for most use cases.",
        securityLevel = SecurityLevel.HIGH
    ),
    GUTMANN(
        displayName = "Gutmann (35 Pass)",
        passes = 35,
        description = "Peter Gutmann's method. 35 passes including complex patterns. Maximum security but very slow. Overkill for modern drives.",
        securityLevel = SecurityLevel.MAXIMUM
    )
}

enum class SecurityLevel {
    BASIC,
    STANDARD,
    HIGH,
    MAXIMUM
}

data class SecureDeletionProgress(
    val currentFile: String,
    val currentPass: Int,
    val totalPasses: Int,
    val bytesProcessed: Long,
    val totalBytes: Long,
    val filesDeleted: Int,
    val totalFiles: Int,
    val elapsedTimeMs: Long,
    val estimatedTimeRemainingMs: Long
) {
    val overallProgress: Float
        get() = if (totalFiles == 0) 0f
        else (filesDeleted.toFloat() + (bytesProcessed.toFloat() / totalBytes)) / totalFiles

    val passProgress: Float
        get() = if (totalBytes == 0L) 0f else bytesProcessed.toFloat() / totalBytes

    val currentPassNumber: Int
        get() = currentPass + 1
}

data class SecureDeletionResult(
    val success: Boolean,
    val filesDeleted: Int,
    val totalSizeBytes: Long,
    val durationMs: Long,
    val method: SecureDeletionMethod,
    val failedFiles: List<String> = emptyList(),
    val error: String? = null
)

data class FileToDelete(
    val uri: android.net.Uri,
    val name: String,
    val sizeBytes: Long,
    val path: String
)
