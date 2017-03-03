package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.FeatureToggleUtil

class HotelFilterViewModel(context:Context): AbstractHotelFilterViewModel(context) {
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

    override fun trackHotelFilterNeighbourhood() {
        HotelTracking.trackLinkHotelFilterNeighbourhood()
    }

    override fun trackHotelRefineRating(rating: String) {
        HotelTracking.trackLinkHotelRefineRating(rating)
    }

    override fun sortItemToRemove(): Sort {
        return Sort.PACKAGE_DISCOUNT
    }

    override fun showHotelFavorite(): Boolean {
        return true
    }

    override fun isClientSideFiltering() : Boolean {
        return !FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_hotel_server_side_filters)
    }
}
