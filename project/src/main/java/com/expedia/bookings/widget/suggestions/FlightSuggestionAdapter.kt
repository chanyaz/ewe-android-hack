package com.expedia.bookings.widget.suggestions

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.SuggestionViewModel

class FlightSuggestionAdapter(viewmodel: SuggestionAdapterViewModel) : SuggestionAdapter(viewmodel) {
    override fun makeViewHolder(root: ViewGroup, vm: SuggestionViewModel): RecyclerView.ViewHolder {
        return FlightSuggestionViewHolder(root, vm)
    }
}

class FlightSuggestionViewHolder(root: ViewGroup, vm: SuggestionViewModel) : AbstractSuggestionViewHolder(root, vm) {
    override fun trackRecentSearchClick() {
        val suggestion = vm.suggestionObserver.value
        val isRecentSearch = (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON)
        if (isRecentSearch) {
            FlightsV2Tracking.trackFlightRecentSearchClick()
        }
    }
}
