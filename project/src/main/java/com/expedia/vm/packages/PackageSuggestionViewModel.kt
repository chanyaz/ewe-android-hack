package com.expedia.vm.packages

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils

class PackageSuggestionViewModel(private val isOrigin: Boolean) : BaseSuggestionViewModel() {
    override fun getTitle(suggestion: SuggestionV4): String {
        val isChild = suggestion.hierarchyInfo?.isChild ?: false
        val isHistory = suggestion.isHistoryItem
        val displayName = getDisplayName(suggestion)
        val shortName = HtmlCompat.stripHtml(suggestion.regionNames.shortName)

        if (isOrigin) {
            return SuggestionStrUtils.formatAirportName(if (isChild && isHistory) shortName else displayName)
        } else {
            return if (isChild && isHistory) SuggestionStrUtils.formatDashWithoutSpace(shortName) else displayName
        }
    }

    override fun getSubTitle(suggestion: SuggestionV4): String {
        if (isOrigin) {
            return StrUtils.formatCityStateName(getDisplayName(suggestion))
        } else {
            return ""
        }
    }

    override fun getIcon(suggestion: SuggestionV4): Int {
        if (suggestion.isHistoryItem) {
            return R.drawable.recents
        } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return R.drawable.ic_suggest_current_location
        } else if (isOrigin) {
            return R.drawable.airport_suggest
        } else {
            return R.drawable.search_type_icon
        }
    }
}
