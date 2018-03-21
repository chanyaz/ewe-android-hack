package com.expedia.bookings.itin.tripstore.data

data class ItinLx(
        val uniqueID: String?,
        val activityLocation: ActivityLocation?
)

data class ActivityLocation (
        val city: String?
)
