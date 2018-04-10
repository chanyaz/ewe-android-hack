package com.expedia.bookings.rail.widget

import android.content.Context
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.rail.vm.RailSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.BaseSuggestionAdapterViewModel

class RailSuggestionAdapter(viewModel: BaseSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(context: Context): BaseSuggestionViewModel {
        return RailSuggestionViewModel(context)
    }
}
