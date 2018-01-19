package com.expedia.bookings.data

sealed class SuggestionDataItem {
    class CurrentLocation(val suggestion : SuggestionV4) : SuggestionDataItem()
    class V4(val suggestion : SuggestionV4) : SuggestionDataItem()
    class Label(val suggestionlabel: String) : SuggestionDataItem()
}
