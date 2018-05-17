package com.expedia.bookings.data.hotels.shortlist

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
        var roomConfiguration: String? = null)
