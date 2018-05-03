package com.expedia.bookings.data

abstract class BaseHotelFilterOptions {

    var filterHotelName: String? = null
    var filterStarRatings: List<Int> = emptyList()
    var filterVipOnly: Boolean = false
    var userSort: SortType? = null

    abstract fun getFiltersQueryMap(): Map<String, String>

    abstract fun isEmpty(): Boolean

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    enum class SortType(val sortName: String, val packageSortName: String) {
        EXPERT_PICKS("ExpertPicks", "FEATURED"),
        STARS("StarRatingDesc", ""),
        PRICE("PriceAsc", "PRICE_ASCENDING"),
        REVIEWS("Reviews", "REVIEWS"),
        DISTANCE("Distance", ""),
        MOBILE_DEALS("Deals", "PACKAGE_SAVINGS")
    }
}
