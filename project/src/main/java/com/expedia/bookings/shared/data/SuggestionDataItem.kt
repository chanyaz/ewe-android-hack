package com.expedia.bookings.shared.data

import com.expedia.bookings.data.SuggestionV4

sealed class SuggestionDataItem() {
    class SuggestionDropDown(val suggestion: SuggestionV4) : SuggestionDataItem()
    class CurrentLocation(val suggestion: SuggestionV4) : SuggestionDataItem()
    class Label(val suggestionLabel: String) : SuggestionDataItem()
}
