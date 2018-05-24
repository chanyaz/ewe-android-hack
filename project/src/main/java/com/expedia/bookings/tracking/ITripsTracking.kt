package com.expedia.bookings.tracking

interface ITripsTracking {
    fun trackHotelItinPricingRewardsPageLoad(trip: HashMap<String, String?>)
    fun trackHotelItinPricingRewardsClick()
    fun trackItinLx(trip: HashMap<String, String?>)
    fun trackTripFolderAbTest()
    fun trackTripListVisit(tabPosition: Int)
    fun trackItinHotelViewReceipt()
    fun trackHotelTaxiCardClick()
    fun trackItinHotelViewRewards()
    fun trackItinLxDetailsMap()
    fun trackItinLxDetailsDirections()
    fun trackItinLxRedeemVoucher()
    fun trackItinLxCallSupportClicked()
    fun trackItinLxMoreHelpClicked()
    fun trackItinLxMoreHelpPageLoad(trip: HashMap<String, String?>)
    fun trackItinExpandedMapZoomIn()
    fun trackItinExpandedMapZoomOut()
    fun trackItinExpandedMapZoomPan()
    fun trackItinMapDirectionsButton()
}
