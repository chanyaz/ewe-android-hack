package com.expedia.bookings.itin.tripstore.data

data class Itin(
        val tripId: String?,
        val webDetailsURL: String?,
        val tripNumber: String?,
        val title: String?,
        val startTime: Time?,
        val endTime: Time?,
        val bookingStatus: String?,
        val hotels: List<ItinHotel>?,
        val flights: List<ItinFlight>?,
        val activities: List<ItinActivity>?,
        val cars: List<ItinCar>?,
        val cruises: List<ItinCruise>?,
        val rails: List<ItinRail>?,
        val packages: List<ItinPackage>?,
        val rewardList: List<Reward>?
)

data class Reward(
        val totalPoints: String?,
        val basePoints: String?
)
