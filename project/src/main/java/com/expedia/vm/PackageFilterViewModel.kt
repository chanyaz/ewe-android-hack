package com.expedia.vm

import android.content.Context
import com.expedia.bookings.tracking.PackagesTracking

class PackageFilterViewModel(context:Context): AbstractHotelFilterViewModel(context) {
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

    override fun trackHotelFilterNeighbourhood() {
        PackagesTracking().trackHotelFilterNeighbourhood()
    }

    override fun trackHotelRefineRating(rating: String) {
        PackagesTracking().trackHotelRefineRating(rating)
    }

    override fun sortItemToRemove(): Sort {
        return Sort.DEALS
    }

    override fun showHotelFavorite(): Boolean {
        return false
    }
}
