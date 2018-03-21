package com.expedia.bookings.itin.tripstore.data

data class ItinPackage(
        val uniqueID: String?,
        val hotels: List<ItinHotel>?,
        val flights: List<ItinFlight>?,
        val cars: List<ItinCar>?,
        val lxes: List<ItinLx>?,
        val cruises: List<ItinCruise>?,
        val rails: List<ItinRail>?
)
