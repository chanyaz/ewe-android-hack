package com.expedia.bookings.widget

import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.LXSuggestionAdapterViewModel
import com.expedia.vm.lx.LxSuggestionViewModel

class LXSuggestionAdapter(viewmodel: LXSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewmodel) {
    override fun getSuggestionViewModel(): BaseSuggestionViewModel {
        return LxSuggestionViewModel()
    }
}
