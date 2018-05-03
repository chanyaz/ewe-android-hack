package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BaseHotelFilterOptions

class PackageHotelFilterOptions : BaseHotelFilterOptions() {

    override fun getFiltersQueryMap(): Map<String, String> {
        val params = HashMap<String, String>()

        if (!filterHotelName.isNullOrEmpty()) {
            params["hotelName"] = filterHotelName!!
        }

        if (filterStarRatings.isNotEmpty()) {
            params["stars"] = filterStarRatings.joinToString(",")
        }

        if (userSort != null) {
            params["hotelSortOrder"] = userSort!!.packageSortName
        }

        params["vipOnly"] = filterVipOnly.toString()

        return params
    }

    override fun isEmpty(): Boolean {
        return filterHotelName.isNullOrEmpty()
                && filterStarRatings.isEmpty()
                && !filterVipOnly
                && userSort == null
    }
}
