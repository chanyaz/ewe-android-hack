package com.expedia.bookings.hotel.widget

import android.content.Context
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.hotel.vm.HotelSuggestionViewModel
import com.expedia.bookings.shared.data.SuggestionDataItem
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.BaseSuggestionAdapterViewModel

class HotelSuggestionAdapter(viewModel: BaseSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(context: Context): BaseSuggestionViewModel {
        return HotelSuggestionViewModel(context)
    }

    override fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData {
        val suggestions = suggestionItems.filterIsInstance<SuggestionDataItem.SuggestionDropDown>()

        val trackingData = SuggestionTrackingData()
        trackingData.selectedSuggestionPosition = position + 1
        trackingData.suggestionsShownCount = suggestions.count()
        trackingData.previousSuggestionsShownCount = pastSuggestionsShownCount

        // api doesn't give us parent information so we need to manually check
        if (!suggestion.isChild && position + 1 < suggestions.count()) {
            trackingData.isParent = suggestions[position + 1].suggestion.isChild
        }
        trackingData.isChild = suggestion.isChild
        trackingData.updateData(suggestion)

        return trackingData
    }
}
