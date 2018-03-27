package com.expedia.bookings.itin.tripstore.data

data class ItinPackage(
        val uniqueID: String?,
        val hotels: List<ItinHotel>?,
        val flights: List<ItinFlight>?,
        val cars: List<ItinCar>?,
        val activities: List<ItinActivity>?,
        val cruises: List<ItinCruise>?,
        val rails: List<ItinRail>?,
        val price: Price?
)

data class Price(
        val totalFormatted: String?
)
