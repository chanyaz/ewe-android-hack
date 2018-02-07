package com.expedia.bookings.shared.widget

import android.content.Context
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.shared.vm.SuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.BaseSuggestionAdapterViewModel

class SuggestionAdapter(viewModel: BaseSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(context: Context): BaseSuggestionViewModel {
        return SuggestionViewModel(context)
    }
}
