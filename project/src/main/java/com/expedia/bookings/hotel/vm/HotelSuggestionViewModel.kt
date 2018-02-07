package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.squareup.phrase.Phrase

open class HotelSuggestionViewModel(context: Context) : BaseSuggestionViewModel(context) {
    override fun getTitle(): String {
        return HtmlCompat.stripHtml(suggestion.regionNames.displayName)
    }

    override fun getSubTitle(): String {
        if (searchInfo != null) {
            val nightsString = context.resources.getQuantityString(R.plurals.number_of_nights,
                    searchInfo!!.totalStay(), searchInfo!!.totalStay())
            val guestsString = context.resources.getQuantityString(R.plurals.number_of_guests_capitalized,
                    searchInfo!!.travelers.totalTravelers(), searchInfo!!.travelers.totalTravelers())

            return Phrase.from(context, R.string.suggestion_search_info_dropdown_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(searchInfo!!.startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(searchInfo!!.endDate))
                    .put("nights", nightsString)
                    .put("guests", guestsString)
                    .format().toString()
        }
        return ""
    }

    override fun getIcon(): Int {
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
