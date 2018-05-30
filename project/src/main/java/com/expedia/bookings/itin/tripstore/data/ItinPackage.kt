package com.expedia.bookings.itin.tripstore.data

import com.expedia.bookings.itin.tripstore.extensions.HasProducts

data class ItinPackage(
        val uniqueID: String?,
        override val hotels: List<ItinHotel>?,
        override val flights: List<ItinFlight>?,
        override val cars: List<ItinCar>?,
        override val activities: List<ItinLx>?,
        override val cruises: List<ItinCruise>?,
        override val rails: List<ItinRail>?,
        val price: Price?
) : HasProducts

data class Price(
        val subTotalFormatted: String?,
        val totalFormatted: String?
)
