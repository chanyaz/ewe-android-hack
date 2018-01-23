package com.expedia.bookings.data

import com.expedia.bookings.hotel.tracking.SuggestionTrackingData

class SearchSuggestion(val suggestionV4: SuggestionV4) {
    var trackingData: SuggestionTrackingData? = null
}
