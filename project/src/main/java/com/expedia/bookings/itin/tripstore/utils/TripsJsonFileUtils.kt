package com.expedia.bookings.itin.tripstore.utils

import java.io.File
import java.security.MessageDigest

class TripsJsonFileUtils(private val tripsDirectory: File) : ITripsJsonFileUtils {
    private val LOGGING_TAG = "TRIPS_JSON_FILE_UTILS"
    private val TEMP_FILE = "TEMP_FILE"

    @Synchronized
    override fun writeTripToFile(filename: String?, content: String?) {
        try {
            if (tripsDirectory.exists() && filename != null && filename.isNotEmpty() && content != null && content.isNotEmpty()) {
                val hashedFileName = hashString(filename)
                val tempFile = File(tripsDirectory, TEMP_FILE)
                val tripFile = File(tripsDirectory, hashedFileName)

                tempFile.writeText(content)
                tempFile.renameTo(tripFile)
            }
        } catch (e: Exception) {
            println("$LOGGING_TAG Exception occurred while writing into file : ${e.printStackTrace()}")
        }
    }

    @Synchronized
    override fun readTripFromFile(filename: String?): String? {
        try {
            if (tripsDirectory.exists() && filename != null && filename.isNotEmpty()) {
                val hashedFileName = hashString(filename)
                val tripFile = File(tripsDirectory, hashedFileName)

                return tripFile.readText()
            }
        } catch (e: Exception) {
            println("$LOGGING_TAG Exception occurred while reading from file : ${e.printStackTrace()}")
        }
        return null
    }

    @Synchronized
    override fun readTripsFromFile(): List<String> {
        val retList = mutableListOf<String>()
        try {
            if (tripsDirectory.exists()) {
                val tripFiles = tripsDirectory.listFiles()
                tripFiles.forEach { file ->
                    retList.add(file.readText())
                }
                return retList
            }
        } catch (e: Exception) {
            println("$LOGGING_TAG Exception occurred while reading from file : ${e.printStackTrace()}")
        }
        return retList
    }

    @Synchronized
    override fun deleteTripFile(filename: String?): Boolean {
        try {
            if (tripsDirectory.exists() && filename != null && filename.isNotEmpty()) {
                val hashedFileName = hashString(filename)
                val tripFile = File(tripsDirectory, hashedFileName)

                return tripFile.delete()
            }
        } catch (e: Exception) {
            println("$LOGGING_TAG Exception occurred while deleting file : ${e.printStackTrace()}")
        }
        return false
    }

    @Synchronized
    override fun deleteTripStore() {
        try {
            if (tripsDirectory.exists()) {
                val tripFiles = tripsDirectory.listFiles()
                tripFiles.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            println("$LOGGING_TAG Exception occurred while deleting file : ${e.printStackTrace()}")
        }
    }

    @Synchronized
    fun hashString(input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
                .getInstance("SHA-1")
                .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach { byte ->
            val i = byte.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }
        return result.toString()
    }
}
