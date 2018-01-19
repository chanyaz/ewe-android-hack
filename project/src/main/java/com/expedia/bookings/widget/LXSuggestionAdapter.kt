package com.expedia.bookings.widget

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.LXSuggestionAdapterViewModel

import com.expedia.vm.lx.LxSuggestionViewModel
import com.expedia.vm.packages.BaseSuggestionViewModel

class LXSuggestionAdapter(viewmodel: LXSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewmodel) {
    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return LxSuggestionViewModel()
    }

    override fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData? {
        return null
    }
}
