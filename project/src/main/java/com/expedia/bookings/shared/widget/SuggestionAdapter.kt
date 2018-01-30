package com.expedia.bookings.shared.widget

import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.shared.vm.SuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.SuggestionAdapterViewModel

class SuggestionAdapter(viewModel: SuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return SuggestionViewModel()
    }
}
