package com.expedia.bookings.tracking

interface ITripsTracking {
    fun trackHotelItinPricingRewardsClick()
    fun trackItinLx(trip: HashMap<String, String?>)
    fun trackTripFolderAbTest()
    fun trackTripListVisit(tabPosition: Int)
    fun trackItinHotelViewReceipt()
    fun trackHotelTaxiCardClick()
    fun trackItinHotelViewRewards()
    fun trackItinLxDetailsMap()
    fun trackItinLxDetailsDirections()
}
