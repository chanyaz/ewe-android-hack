package com.expedia.vm

import android.support.annotation.IdRes
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat

open class HotelSuggestionViewModel {
    private lateinit var suggestion: SuggestionV4
    private var iconDrawableRes: Int = R.drawable.search_type_icon

    fun bind(suggestion: SuggestionV4) {
        this.suggestion = suggestion
        if (suggestion.isHistoryItem) {
            iconDrawableRes = R.drawable.recents
        } else if (suggestion.isRecentSearchItem) {
            iconDrawableRes = R.drawable.recents
        } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            iconDrawableRes = R.drawable.ic_suggest_current_location
        } else if (suggestion.iconType == SuggestionV4.IconType.MAGNIFYING_GLASS_ICON) {
            iconDrawableRes = R.drawable.google_search
        } else if (suggestion.type == "HOTEL") {
            iconDrawableRes = R.drawable.hotel_suggest
        } else if (suggestion.type == "AIRPORT") {
            iconDrawableRes = R.drawable.airport_suggest
        } else {
            iconDrawableRes = R.drawable.search_type_icon
        }
    }

    @IdRes
    fun getIcon() : Int {
        return iconDrawableRes
    }

    open fun getTitle() : String {
        return HtmlCompat.stripHtml(suggestion.regionNames.displayName)
    }

    open fun isChild() : Boolean {
        return suggestion.hierarchyInfo?.isChild ?: false
    }

    open fun isHistoryItem() : Boolean {
        return suggestion.isHistoryItem
    }
}
