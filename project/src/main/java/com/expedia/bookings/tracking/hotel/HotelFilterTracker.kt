package com.expedia.bookings.tracking.hotel

class HotelFilterTracker : FilterTracker {
    override fun trackHotelSortBy(sortBy: String) {
        HotelTracking.trackHotelSortBy(sortBy)
    }

    override fun trackHotelFilterVIP(vipOnly: Boolean) {
        HotelTracking.trackLinkHotelFilterVip(vipOnly)
    }

    override fun trackHotelFilterPriceSlider() {
        HotelTracking.trackHotelSortPriceSlider()
    }

    override fun trackHotelFilterByName() {
        HotelTracking.trackLinkHotelFilterByName()
    }

    override fun trackClearFilter() {
        HotelTracking.trackLinkHotelClearFilter()
    }

    override fun trackHotelFilterNeighborhood() {
        HotelTracking.trackLinkHotelFilterNeighbourhood()
    }

    override fun trackHotelRefineRating(rating: String) {
        HotelTracking.trackLinkHotelRefineRating(rating)
    }

    override fun trackHotelFilterAmenity(amenity: String) {
        HotelTracking.trackLinkHotelAmenityFilter(amenity)
    }
}
