package com.expedia.bookings.widget.packages

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.BaseSuggestionViewModel
import com.expedia.vm.packages.PackageSuggestionViewModel

class PackageSuggestionAdapter(viewModel: SuggestionAdapterViewModel,
                               private val isOrigin: Boolean) : BaseSuggestionAdapter(viewModel) {

    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return PackageSuggestionViewModel(isOrigin)
    }

    override fun getSuggestionTrackingData(suggestion: SuggestionV4, position: Int): SuggestionTrackingData? {
        return null
    }
}
