package com.expedia.bookings.hotel.widget

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.hotel.vm.HotelSuggestionViewModel
import com.expedia.bookings.shared.data.SuggestionDataItem
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.SuggestionAdapterViewModel

class HotelSuggestionAdapter(viewModel: SuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return HotelSuggestionViewModel()
    }

    override fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData {
        val suggestions = suggestionItems.filter { it is SuggestionDataItem.SuggestionDropDown } as List<SuggestionDataItem.SuggestionDropDown>

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
