package com.expedia.vm.lx

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.utils.SuggestionStrUtils

class LxSuggestionViewModel : BaseSuggestionViewModel() {
    override fun getTitle(suggestion: SuggestionV4): String {
        return SuggestionStrUtils.formatCityName(getDisplayName(suggestion))
    }

    override fun getSubTitle(suggestion: SuggestionV4): String {
        if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return ""
        }
        return SuggestionStrUtils.formatAirportName(getShortName(suggestion))
    }

    override fun getIcon(suggestion: SuggestionV4): Int {
        if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
            return R.drawable.recents
        } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return R.drawable.ic_suggest_current_location
        }
        return R.drawable.search_type_icon
    }
}
