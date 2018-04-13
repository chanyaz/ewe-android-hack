package com.expedia.bookings.tracking

interface ITripsTracking {
    fun trackHotelItinPricingRewardsClick()
    fun trackItinLx(trip: HashMap<String, String?>)
}
