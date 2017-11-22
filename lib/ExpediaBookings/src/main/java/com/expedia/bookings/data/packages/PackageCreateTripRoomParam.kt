package com.expedia.bookings.data.packages

data class PackageCreateTripRoomParam(
        val numOfAdults: Int,
        val infantsInLap: Boolean,
        val childAges: List<Int>
)
