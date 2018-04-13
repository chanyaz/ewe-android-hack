package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils

class PackageSuggestionViewModel(private val isOrigin: Boolean, context: Context) : BaseSuggestionViewModel(context) {
    override fun getTitle(): String {
        val isChild = suggestion.hierarchyInfo?.isChild ?: false
        val isHistory = suggestion.isHistoryItem
        val displayName = getDisplayName(suggestion)
        val shortName = HtmlCompat.stripHtml(suggestion.regionNames.shortName)

        if (isOrigin) {
            return SuggestionStrUtils.formatAirportName(if (isChild && isHistory) shortName else displayName)
        }
        return if (isChild && isHistory) SuggestionStrUtils.formatDashWithoutSpace(shortName) else displayName
    }

    override fun getSubTitle(): String {
        if (isOrigin) {
            return StrUtils.formatCityStateName(getDisplayName(suggestion))
        }
        return ""
    }

    override fun getIcon(): Int {
        when {
            suggestion.isHistoryItem -> return R.drawable.recents
            suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON -> return R.drawable.ic_suggest_current_location
            isOrigin -> return R.drawable.airport_suggest
            else -> return R.drawable.search_type_icon
        }
    }
}
