package com.example.osint.utils

import androidx.exifinterface.media.ExifInterface
import com.example.osint.domain.model.ImageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object ExifUtils {

    suspend fun extractImageMetadata(inputStream: InputStream): ImageMetadata = withContext(Dispatchers.IO) {
        val exif = ExifInterface(inputStream)
        val allTags = mutableMapOf<String, String>()

        val tagNames = listOf(
            ExifInterface.TAG_IMAGE_WIDTH to "Image Width",
            ExifInterface.TAG_IMAGE_LENGTH to "Image Height",
            ExifInterface.TAG_MAKE to "Make",
            ExifInterface.TAG_MODEL to "Model",
            ExifInterface.TAG_DATETIME to "Date/Time",
            ExifInterface.TAG_DATETIME_ORIGINAL to "Date/Time Original",
            ExifInterface.TAG_DATETIME_DIGITIZED to "Date/Time Digitized",
            ExifInterface.TAG_ORIENTATION to "Orientation",
            ExifInterface.TAG_SOFTWARE to "Software",
            ExifInterface.TAG_GPS_LATITUDE to "GPS Latitude",
            ExifInterface.TAG_GPS_LATITUDE_REF to "GPS Latitude Ref",
            ExifInterface.TAG_GPS_LONGITUDE to "GPS Longitude",
            ExifInterface.TAG_GPS_LONGITUDE_REF to "GPS Longitude Ref",
            ExifInterface.TAG_GPS_ALTITUDE to "GPS Altitude",
            ExifInterface.TAG_GPS_ALTITUDE_REF to "GPS Altitude Ref",
            ExifInterface.TAG_FLASH to "Flash",
            ExifInterface.TAG_FOCAL_LENGTH to "Focal Length",
            ExifInterface.TAG_EXPOSURE_TIME to "Exposure Time",
            ExifInterface.TAG_F_NUMBER to "F-Number",
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY to "ISO",
            ExifInterface.TAG_WHITE_BALANCE to "White Balance",
            ExifInterface.TAG_ARTIST to "Artist",
            ExifInterface.TAG_COPYRIGHT to "Copyright",
            ExifInterface.TAG_USER_COMMENT to "User Comment"
        )

        tagNames.forEach { (tag, displayName) ->
            exif.getAttribute(tag)?.let { value ->
                allTags[displayName] = value
            }
        }

        val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).takeIf { it > 0 }
        val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).takeIf { it > 0 }
        val make = exif.getAttribute(ExifInterface.TAG_MAKE)
        val model = exif.getAttribute(ExifInterface.TAG_MODEL)
        val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
        val orientation = getOrientationString(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
        val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
        val flash = exif.getAttribute(ExifInterface.TAG_FLASH)
        val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
        val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
        val aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
        val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
        val whiteBalance = exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE)

        val latLong = exif.latLong
        val gpsLatitude = latLong?.get(0)
        val gpsLongitude = latLong?.get(1)
        val gpsAltitude = exif.getAltitude(0.0).takeIf { it != 0.0 }

        ImageMetadata(
            width = width,
            height = height,
            make = make,
            model = model,
            dateTime = dateTime,
            orientation = orientation,
            software = software,
            gpsLatitude = gpsLatitude,
            gpsLongitude = gpsLongitude,
            gpsAltitude = gpsAltitude,
            flash = flash,
            focalLength = focalLength,
            exposureTime = exposureTime,
            aperture = aperture,
            iso = iso,
            whiteBalance = whiteBalance,
            allTags = allTags
        )
    }

    suspend fun stripExifData(inputStream: InputStream, outputStream: java.io.OutputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            val exif = ExifInterface(inputStream)

            val tagsToRemove = listOf(
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_SOFTWARE,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_ARTIST,
                ExifInterface.TAG_COPYRIGHT,
                ExifInterface.TAG_USER_COMMENT
            )

            tagsToRemove.forEach { tag ->
                exif.setAttribute(tag, null)
            }

            exif.saveAttributes()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getOrientationString(orientation: Int): String {
        return when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> "Normal"
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> "Flip Horizontal"
            ExifInterface.ORIENTATION_ROTATE_180 -> "Rotate 180°"
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> "Flip Vertical"
            ExifInterface.ORIENTATION_TRANSPOSE -> "Transpose"
            ExifInterface.ORIENTATION_ROTATE_90 -> "Rotate 90° CW"
            ExifInterface.ORIENTATION_TRANSVERSE -> "Transverse"
            ExifInterface.ORIENTATION_ROTATE_270 -> "Rotate 270° CW"
            else -> "Unknown"
        }
    }
}
