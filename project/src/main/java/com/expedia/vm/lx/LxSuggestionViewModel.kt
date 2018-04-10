package com.expedia.vm.lx

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.utils.SuggestionStrUtils

class LxSuggestionViewModel(context: Context) : BaseSuggestionViewModel(context) {
    override fun getTitle(): String {
        return SuggestionStrUtils.formatCityName(getDisplayName(suggestion))
    }

    override fun getSubTitle(): String {
        if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return ""
        }
        return SuggestionStrUtils.formatAirportName(getShortName(suggestion))
    }
}
