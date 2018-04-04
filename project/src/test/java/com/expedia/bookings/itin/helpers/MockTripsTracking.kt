package com.expedia.bookings.itin.helpers

import com.expedia.bookings.tracking.ITripsTracking

class MockTripsTracking : ITripsTracking {

    var trackItinLxCalled = false
    var trackHotelItinPricingRewardsClicked = false

    override fun trackHotelItinPricingRewardsClick() {
        trackHotelItinPricingRewardsClicked = true
    }

    override fun trackItinLx(trip: HashMap<String, String?>) {
        trackItinLxCalled = true
    }
}
