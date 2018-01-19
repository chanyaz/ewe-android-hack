package com.expedia.bookings.shared

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.shared.vm.GenericSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.BaseSuggestionViewModel

class GenericSuggestionAdapter(viewModel: SuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return GenericSuggestionViewModel()
    }

    override fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData? {
        return null
    }
}
