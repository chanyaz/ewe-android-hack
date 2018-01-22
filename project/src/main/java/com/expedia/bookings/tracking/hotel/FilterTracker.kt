package com.expedia.bookings.tracking.hotel

interface FilterTracker {
    fun trackHotelSortBy(sortBy: String)
    fun trackHotelFilterVIP(vipOnly: Boolean)
    fun trackHotelFilterPriceSlider()
    fun trackHotelFilterByName()
    fun trackClearFilter()
    fun trackHotelFilterNeighborhood()
    fun trackHotelRefineRating(rating: String)
    fun trackHotelFilterAmenity(amenity: String)
}
