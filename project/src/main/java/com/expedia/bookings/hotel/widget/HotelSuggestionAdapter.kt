package com.expedia.bookings.hotel.widget

import com.expedia.bookings.data.SuggestionDataItem
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.hotel.vm.HotelSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.BaseSuggestionViewModel

class HotelSuggestionAdapter(viewModel: SuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return HotelSuggestionViewModel()
    }

    override fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int) : SuggestionTrackingData {
        val suggestions = suggestionItems.filter { it is SuggestionDataItem.V4 } as List<SuggestionDataItem.V4>

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
