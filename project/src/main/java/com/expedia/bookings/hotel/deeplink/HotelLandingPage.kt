package com.expedia.bookings.hotel.deeplink

enum class HotelLandingPage(val id: String) {
    SEARCH("page_search"),
    RESULTS("page_results"),
    INFO_SITE("page_info_site");

    companion object {
        fun fromId(id: String?): HotelLandingPage? {
            when (id) {
                SEARCH.id -> return SEARCH
                RESULTS.id -> return RESULTS
                INFO_SITE.id -> return INFO_SITE
                else -> return null
            }
        }
    }
}