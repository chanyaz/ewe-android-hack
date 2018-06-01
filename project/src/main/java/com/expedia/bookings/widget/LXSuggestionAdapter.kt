package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.bookings.lx.vm.LXSuggestionAdapterViewModel
import com.expedia.bookings.lx.vm.LxSuggestionViewModel

class LXSuggestionAdapter(viewmodel: LXSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewmodel) {
    override fun getSuggestionViewModel(context: Context): BaseSuggestionViewModel {
        return LxSuggestionViewModel(context)
    }
}
