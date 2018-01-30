package com.expedia.bookings.hotel.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.text.HtmlCompat

open class HotelSuggestionViewModel : BaseSuggestionViewModel() {
    override fun getTitle(suggestion: SuggestionV4): String {
        return HtmlCompat.stripHtml(suggestion.regionNames.displayName)
    }

    override fun getSubTitle(suggestion: SuggestionV4): String {
        return ""
    }

    override fun getIcon(suggestion: SuggestionV4): Int {
        if (suggestion.isHistoryItem) {
            return R.drawable.search_type_icon
        } else if (suggestion.isRecentSearchItem) {
            return R.drawable.recents
        } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return R.drawable.ic_suggest_current_location
        } else if (suggestion.iconType == SuggestionV4.IconType.MAGNIFYING_GLASS_ICON) {
            return R.drawable.google_search
        } else if (suggestion.type == "HOTEL") {
            return R.drawable.hotel_suggest
        } else if (suggestion.type == "AIRPORT") {
            return R.drawable.airport_suggest
        } else {
            return R.drawable.search_type_icon
        }
    }
}
