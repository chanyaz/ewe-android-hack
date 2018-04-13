package com.expedia.bookings.widget.packages

import android.content.Context
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.BaseSuggestionAdapterViewModel
import com.expedia.bookings.packages.vm.PackageSuggestionViewModel

class PackageSuggestionAdapter(viewModel: BaseSuggestionAdapterViewModel,
                               private val isOrigin: Boolean) : BaseSuggestionAdapter(viewModel) {

    override fun getSuggestionViewModel(context: Context): BaseSuggestionViewModel {
        return PackageSuggestionViewModel(isOrigin, context)
    }
}
