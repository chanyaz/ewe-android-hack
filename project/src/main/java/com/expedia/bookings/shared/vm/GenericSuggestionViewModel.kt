package com.expedia.bookings.shared.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.vm.packages.BaseSuggestionViewModel

class GenericSuggestionViewModel : BaseSuggestionViewModel() {
    override fun getTitle(suggestion: SuggestionV4): String {
        if (isChild(suggestion) && suggestion.isHistoryItem) {
            return SuggestionStrUtils.formatDashWithoutSpace(getShortName(suggestion))
        }
        return getDisplayName(suggestion)
    }

    override fun getSubTitle(suggestion: SuggestionV4): String {
        return ""
    }

    override fun getIcon(suggestion: SuggestionV4): Int {
        if (suggestion.isHistoryItem) {
            return R.drawable.recents
        } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
            return R.drawable.ic_suggest_current_location
        } else {
            return R.drawable.search_type_icon
        }
    }
}
