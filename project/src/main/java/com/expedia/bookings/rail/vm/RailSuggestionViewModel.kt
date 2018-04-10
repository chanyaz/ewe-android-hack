package com.expedia.bookings.rail.vm

import android.content.Context
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.utils.SuggestionStrUtils

class RailSuggestionViewModel(context: Context) : BaseSuggestionViewModel(context) {
    override fun getTitle(): CharSequence {
        if (isChild(suggestion) && suggestion.isHistoryItem) {
            return SuggestionStrUtils.formatDashWithoutSpace(getShortName(suggestion))
        }
        return getDisplayName(suggestion)
    }

    override fun getSubTitle(): String {
        return ""
    }
}
