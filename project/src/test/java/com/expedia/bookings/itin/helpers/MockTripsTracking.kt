package com.expedia.bookings.itin.helpers

import com.expedia.bookings.tracking.ITripsTracking

class MockTripsTracking : ITripsTracking {

    var trackTripListVisited = false
    var trackTripFolderAbTest = false

    var trackItinHotelViewReceiptCalled = false
    var trackHotelItinPricingRewardsClicked = false
    var trackHotelTaxiClick = false
    var trackHotelItinViewRewardsCalled = false

    var trackItinLxCalled = false
    var mapClicked = false
    var directionClicked = false
    var trackRedeemVoucherCalled = false

    override fun trackItinLxDetailsMap() {
        mapClicked = true
    }

    override fun trackItinLxRedeemVoucher() {
        trackRedeemVoucherCalled = true
    }

    override fun trackItinLxDetailsDirections() {
        directionClicked = true
    }

    override fun trackItinHotelViewReceipt() {
        trackItinHotelViewReceiptCalled = true
    }

    override fun trackHotelItinPricingRewardsClick() {
        trackHotelItinPricingRewardsClicked = true
    }

    override fun trackItinLx(trip: HashMap<String, String?>) {
        trackItinLxCalled = true
    }

    override fun trackTripListVisit(tabPosition: Int) {
        trackTripListVisited = true
    }

    override fun trackTripFolderAbTest() {
        trackTripFolderAbTest = true
    }

    override fun trackHotelTaxiCardClick() {
        trackHotelTaxiClick = true
    }

    override fun trackItinHotelViewRewards() {
        trackHotelItinViewRewardsCalled = true
    }
}
