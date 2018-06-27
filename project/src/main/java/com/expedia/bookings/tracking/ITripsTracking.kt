package com.expedia.bookings.tracking

interface ITripsTracking {
    fun trackHotelItinPricingRewardsPageLoad(trip: HashMap<String, String?>)
    fun trackHotelItinPricingRewardsClick()
    fun trackItinLx(trip: HashMap<String, String?>)
    fun trackTripFolderAbTest()
    fun trackTripListUpcomingTabVisit()
    fun trackTripListPastTabVisit()
    fun trackTripListCancelledTabVisit()
    fun trackItinHotelViewReceipt()
    fun trackHotelTaxiCardClick()
    fun trackItinCarDetailsDirections()
    fun trackItinCarDetailsMap()
    fun trackItinHotelViewRewards()
    fun trackItinLxDetailsMap()
    fun trackItinLxDetailsDirections()
    fun trackItinLxRedeemVoucher()
    fun trackItinLxCallSupplierClicked()
    fun trackItinLxMoreHelpClicked()
    fun trackItinLxMoreHelpPageLoad(trip: HashMap<String, String?>)
    fun trackItinLxDetailsCallClicked()
    fun trackItinExpandedMapZoomIn()
    fun trackItinExpandedMapZoomOut()
    fun trackItinExpandedMapZoomPan()
    fun trackItinMapDirectionsButton()
    fun trackItinLxCallCustomerSupportClicked()
    fun trackItinLxCustomerServiceLinkClicked()
    fun trackItinCarMoreHelpClicked()
    fun trackItinCarCallSupportClicked()
    fun trackItinCarCallCustomerSupportClicked()
    fun trackItinCarCustomerServiceLinkClicked()
    fun trackItinCarShareIconClicked()
    fun trackItinCarDetailsCallClicked()
    fun trackItinLxShareIconClicked()
    fun trackItinCarDetailsPageLoad(trip: HashMap<String, String?>)
    fun trackItinCarMoreHelpPageLoad(trip: HashMap<String, String?>)
    fun trackItinLobPriceSummaryButtonClick(lob: String)
    fun trackItinLobAdditionalInfoButtonClick(lob: String)
}
