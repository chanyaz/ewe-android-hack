package com.expedia.bookings.itin.tripstore.data

data class ItinCruise(
        val uniqueID: String?,
        val destination: String?,
        val cruiseLineName: String?,
        val shipName: String?,
        val departureDateAbbreviated: String?,
        val returnDateAbbreviated: String?,
        val departurePort: Port?,
        val disembarkationPort: Port?,
        val startTime: ItinTime?,
        val endTime: ItinTime?,
        val shipImageUrl: String?
) : ItinLOB

data class Port(
        val portName: String?
)
