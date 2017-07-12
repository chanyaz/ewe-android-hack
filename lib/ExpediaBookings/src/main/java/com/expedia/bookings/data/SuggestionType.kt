package com.expedia.bookings.data

sealed class SuggestionType {
    class SUGGESTIONV4(val suggestion : SuggestionV4) : SuggestionType()
    class SUGGESTIONLABEL(val suggestionlabel: String) : SuggestionType()
}
