package com.expedia.bookings.flights.widget.adapter

import android.content.Context
import com.expedia.bookings.flights.vm.FlightsSuggestionViewModel
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.vm.BaseSuggestionAdapterViewModel

class FlightsSuggestionAdapter(viewModel: BaseSuggestionAdapterViewModel) : BaseSuggestionAdapter(viewModel) {
    override fun getSuggestionViewModel(context: Context): BaseSuggestionViewModel {
        return FlightsSuggestionViewModel(context)
    }
}
