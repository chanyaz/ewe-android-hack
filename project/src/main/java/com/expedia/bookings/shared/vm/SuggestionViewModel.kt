package com.expedia.bookings.shared.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.SuggestionStrUtils

class SuggestionViewModel(context: Context) : BaseSuggestionViewModel(context) {
    override fun getTitle(): String {
        if (isChild(suggestion) && suggestion.isHistoryItem) {
            return SuggestionStrUtils.formatDashWithoutSpace(getShortName(suggestion))
        }
        return getDisplayName(suggestion)
    }

    override fun getSubTitle(): String {
        return ""
    }

    override fun getIcon(): Int {
        if (suggestion.isHistoryItem) {
            return R.drawable.recents
        } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return R.drawable.ic_suggest_current_location
        } else {
            return R.drawable.search_type_icon
        }
    }
}
