package com.expedia.bookings.itin.tripstore.data

data class Time(
        val raw: String?,
        val localized: String?,
        val localizedShortDate: String?,
        val localizedMediumDate: String?,
        val localizedFullDate: String?,
        val localizedLongDate: String?
)
