package com.expedia.bookings.data.trips

data class TripFolder(
        val tripFolderId: String,
        val title: String,
        val startTime: TripFolderDateTime,
        val endTime: TripFolderDateTime,
        val state: TripFolderState,
        val timing: TripFolderTiming,
        val lobs: List<String>
)

data class TripFolderDateTime(
        val raw: String,
        val epochSeconds: Int,
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
