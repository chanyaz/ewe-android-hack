package com.expedia.bookings.itin.helpers

import com.expedia.bookings.tracking.ITripsTracking

class MockTripsTracking : ITripsTracking {

    var trackItinLxCalled = false
    var trackItinHotelViewReceiptCalled = false

    override fun trackItinHotelViewReceipt() {
        trackItinHotelViewReceiptCalled = true
    }

    var trackHotelItinPricingRewardsClicked = false
    var trackTripListVisited = false
    var trackTripFolderAbTest = false
    var trackHotelTaxiClick = false
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
}
