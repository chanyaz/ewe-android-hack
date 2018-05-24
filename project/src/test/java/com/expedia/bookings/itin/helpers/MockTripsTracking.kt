package com.expedia.bookings.itin.helpers

import com.expedia.bookings.tracking.ITripsTracking

class MockTripsTracking : ITripsTracking {
    var trackTripListVisited = false
    var trackTripFolderAbTest = false

    var trackItinHotelViewReceiptCalled = false
    var trackHotelItinPricingRewardsClicked = false
    var trackHotelTaxiClick = false
    var trackHotelItinViewRewardsCalled = false
    var trackHotelItinPricingRewardsPageload = false
    var trackItinExpandedMapZoomIn = false
    var trackItinExpandedMapZoomOut = false
    var trackItinExpandedMapZoomPan = false
    var trackItinMapDirectionsButtonCalled = false

    var trackItinLxCalled = false
    var mapClicked = false
    var directionClicked = false
    var trackRedeemVoucherCalled = false
    var trackItinLxCallSupportClicked = false
    var trackItinLxMoreHelpClicked = false
    var trackItinlxMoreHelpPageLoaded = false

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

    override fun trackHotelItinPricingRewardsPageLoad(trip: HashMap<String, String?>) {
        trackHotelItinPricingRewardsPageload = true
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

    override fun trackItinLxCallSupportClicked() {
        trackItinLxCallSupportClicked = true
    }

    override fun trackItinLxMoreHelpClicked() {
        trackItinLxMoreHelpClicked = true
    }

    override fun trackItinLxMoreHelpPageLoad(trip: HashMap<String, String?>) {
        trackItinlxMoreHelpPageLoaded = true
    }

    override fun trackItinExpandedMapZoomIn() {
        trackItinExpandedMapZoomIn = true
    }

    override fun trackItinExpandedMapZoomOut() {
        trackItinExpandedMapZoomOut = true
    }

    override fun trackItinExpandedMapZoomPan() {
        trackItinExpandedMapZoomPan = true
    }

    override fun trackItinMapDirectionsButton() {
        trackItinMapDirectionsButtonCalled = true
    }
}
