package com.expedia.bookings.itin.helpers

import com.expedia.bookings.tracking.ITripsTracking

class MockTripsTracking : ITripsTracking {

    var trackTripListVisited = false
    var trackTripFolderAbTest = false

    var trackItinHotelViewReceiptCalled = false
    var trackHotelItinPricingRewardsClicked = false
    var trackHotelItinViewRewards = false
    var trackHotelTaxiClick = false

    var trackItinLxCalled = false

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

    override fun trackItinHotelViewRewardsClick() {
        trackHotelItinViewRewards = true
    }
}
