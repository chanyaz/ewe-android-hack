package com.expedia.bookings.data.trips

import com.google.gson.annotations.SerializedName

data class TripFolder(
        val tripFolderId: String,
        val title: String,
        val startTime: TripFolderDateTime,
        val endTime: TripFolderDateTime,
        val bookingStatus: TripFolderState,
        val timing: TripFolderTiming,
        val lobs: List<TripFolderProduct>
)

data class TripFolderDateTime(
        val raw: String,
        val epochSeconds: Long,
        val timeZoneOffsetSeconds: Int
)

enum class TripFolderState {
    BOOKED,
    CANCELLED
}

enum class TripFolderTiming {
    UPCOMING,
    PAST
}

enum class TripFolderProduct {
    @SerializedName("Hotel")
    HOTEL,
    @SerializedName("Flight")
    FLIGHT,
    @SerializedName("Car")
    CAR,
    @SerializedName("Activity")
    ACTIVITY,
    @SerializedName("Rail")
    RAIL,
    @SerializedName("Cruise")
    CRUISE
}
