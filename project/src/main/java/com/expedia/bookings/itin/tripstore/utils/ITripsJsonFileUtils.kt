package com.expedia.bookings.itin.tripstore.utils

interface ITripsJsonFileUtils {
    fun writeToFile(filename: String?, content: String?)
    fun readFromFile(filename: String?): String?
    fun deleteFile(filename: String?): Boolean
    fun deleteAllFiles()
    fun readFromFileDirectory(): List<String>
}
