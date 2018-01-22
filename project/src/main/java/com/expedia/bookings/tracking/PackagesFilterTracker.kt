package com.expedia.bookings.tracking

import com.expedia.bookings.tracking.hotel.FilterTracker

class PackagesFilterTracker : FilterTracker {
    override fun trackHotelSortBy(sortBy: String) {
        PackagesTracking().trackHotelSortBy(sortBy)
    }

    override fun trackHotelFilterVIP(vipOnly: Boolean) {
        PackagesTracking().trackHotelFilterVIP(vipOnly)
    }

    override fun trackHotelFilterPriceSlider() {
        PackagesTracking().trackHotelFilterPriceSlider()
    }

    override fun trackHotelFilterByName() {
        PackagesTracking().trackHotelFilterByName()
    }

    override fun trackClearFilter() {
        PackagesTracking().trackHotelClearFilter()
    }

    override fun trackHotelFilterNeighborhood() {
        PackagesTracking().trackHotelFilterNeighbourhood()
    }

    override fun trackHotelRefineRating(rating: String) {
        PackagesTracking().trackHotelRefineRating(rating)
    }

    override fun trackHotelFilterAmenity(amenity: String) {
        // not available on package
    }
}
