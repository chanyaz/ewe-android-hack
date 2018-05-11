package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.BaseHotelFilterOptions
import java.util.HashMap

class HotelFilterOptions : BaseHotelFilterOptions() {

    var filterGuestRatings: List<Int> = emptyList()
    var filterPrice: HotelSearchParams.PriceRange? = null
    var filterByNeighborhood: Neighborhood? = null
    var amenities: HashSet<Int> = HashSet()

    override fun getFiltersQueryMap(): Map<String, String> {
        val params = HashMap<String, String>()
        if (!filterHotelName.isNullOrEmpty()) {
            params["filterHotelName"] = filterHotelName!!
        }

        if (filterStarRatings.isNotEmpty()) {
            params["filterStarRatings"] = filterStarRatings.joinToString(",")
        }

        if (filterGuestRatings.isNotEmpty()) {
            params["guestRatingFilterItems"] = filterGuestRatings.joinToString(",")
        }

        if (filterPrice?.isValid() ?: false) {
            params["filterPrice"] = filterPrice!!.getPriceBuckets()
        }

        if (filterVipOnly) {
            params["vipOnly"] = filterVipOnly.toString()
        }

        if (!amenities.isEmpty()) {
            params["filterAmenities"] = amenities.joinToString(",")
        }

        return params
    }

    override fun isEmpty(): Boolean {
        return filterHotelName.isNullOrEmpty()
                && filterStarRatings.isEmpty()
                && filterGuestRatings.isEmpty()
                && filterPrice?.isValid() != true
                && userSort == null
                && !filterVipOnly
                && amenities.isEmpty()
    }
}
