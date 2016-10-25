package com.expedia.vm

import android.content.Context
import com.expedia.bookings.tracking.HotelTracking

class HotelFilterViewModel(context:Context): AbstractHotelFilterViewModel(context) {
    override fun trackHotelSortBy(sortBy: String) {
        HotelTracking().trackHotelSortBy(sortBy)
    }

    override fun trackHotelFilterVIP(vipOnly: Boolean) {
        HotelTracking().trackLinkHotelFilterVip(vipOnly)
    }

    override fun trackHotelFilterPriceSlider() {
        HotelTracking().trackHotelSortPriceSlider()
    }

    override fun trackHotelFilterByName() {
        HotelTracking().trackLinkHotelFilterByName()
    }

    override fun trackClearFilter() {
        HotelTracking().trackLinkHotelClearFilter()
    }

    override fun trackHotelFilterNeighbourhood() {
        HotelTracking().trackLinkHotelFilterNeighbourhood()
    }

    override fun trackHotelRefineRating(rating: String) {
        HotelTracking().trackLinkHotelRefineRating(rating)
    }

    override fun sortItemToRemove(): Sort {
        return Sort.PACKAGE_DISCOUNT
    }

    override fun showHotelFavorite(): Boolean {
        return true
    }
}
