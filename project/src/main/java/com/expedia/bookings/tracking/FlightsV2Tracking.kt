package com.expedia.bookings.tracking

object FlightsV2Tracking {
    fun trackSearchPageLoad() {
        OmnitureTracking.trackPageLoadFlightSearchV2()
    }

    fun trackTravelerPickerClick(actionLabel: String) {
        OmnitureTracking.trackFlightTravelerPickerClick(actionLabel)
    }

    fun trackFlightRecentSearchClick() {
        OmnitureTracking.trackFlightRecentSearchClick()
    }
}
