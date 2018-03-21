package com.expedia.bookings.itin.tripstore.data

data class ItinLx(
        val uniqueID: String?,
        val activityLocation: ActivityLocation?,
        val activityId: String?,
        val travelerCount: String?,
        val price: LxPrice?
)

data class ActivityLocation (
        val city: String?
)

data class LxPrice (
        val base: String?
)
