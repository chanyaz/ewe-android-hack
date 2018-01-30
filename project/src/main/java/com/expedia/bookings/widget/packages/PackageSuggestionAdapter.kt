package com.expedia.bookings.widget.packages

import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.PackageSuggestionViewModel

class PackageSuggestionAdapter(viewModel: SuggestionAdapterViewModel,
                               private val isOrigin: Boolean) : BaseSuggestionAdapter(viewModel) {

    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return PackageSuggestionViewModel(isOrigin)
    }
}
