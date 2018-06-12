package com.expedia.bookings.itin.tripstore.utils

interface ITripsJsonFileUtils {
    fun writeTripToFile(filename: String?, content: String?)
    fun readTripFromFile(filename: String?): String?
    fun deleteTripFile(filename: String?): Boolean
    fun deleteTripStore()
    fun readTripsFromFile(): List<String>
}
