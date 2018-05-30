package com.expedia.bookings.data.hotels.shortlist

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

/**
 * chkIn, chkOut, should be in format yyyyMMdd
 *
 * roomConfiguration should be in format {num adult}|{child 1 age}-{child 2 age}-...-{child n age}
 * example 1 adult = "1", 3 adults, 2 children age 5, 6 = "3|5-6"
 */
data class ShortlistItemMetadata(
        var hotelId: String? = null,
        var chkIn: String? = null,
        var chkOut: String? = null,
        var roomConfiguration: String? = null) {

    @Transient
    private val formatter = DateTimeFormat.forPattern("yyyyMMdd")

    fun getCheckInLocalDate(): LocalDate? {
        if (chkIn.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDate.parse(chkIn, formatter)
        } catch (e: Exception) {
            null
        }
    }

    fun getCheckOutLocalDate(): LocalDate? {
        if (chkOut.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDate.parse(chkOut, formatter)
        } catch (e: Exception) {
            null
        }
    }

    fun getNumberOfAdults(): Int? {
        if (roomConfiguration.isNullOrBlank() || roomConfiguration?.indexOf("|") == 0) {
            return null
        }

        val adultChildrenSplit = roomConfiguration!!.split(delimiters = *arrayOf("|"), ignoreCase = false, limit = 2)
        return if (adultChildrenSplit.isNotEmpty()) {
            adultChildrenSplit[0].toIntOrNull()
        } else {
            null
        }
    }

    fun getChildrenAges(): List<Int> {
        if (roomConfiguration.isNullOrBlank() || roomConfiguration?.indexOf("|") == 0) {
            return emptyList()
        }

        val adultChildrenSplit = roomConfiguration!!.split(delimiters = *arrayOf("|"), ignoreCase = false, limit = 2)
        return if (adultChildrenSplit.size > 1) {
            val childrenAgeList = adultChildrenSplit[1].split("-").map { it.toIntOrNull() }
            if (childrenAgeList.contains(null)) {
                emptyList()
            } else {
                childrenAgeList.map { it!! }
            }
        } else {
            emptyList()
        }
    }
}
