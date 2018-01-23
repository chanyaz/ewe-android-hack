package com.expedia.bookings.hotel.tracking

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat

class SuggestionTrackingData {
    var suggestionsFocused = false
    var suggestionSelected = false

    var charactersTypedCount = 0
    var selectedSuggestionPosition = 0

    var isChild = false
    var isParent = false
    var isHistory = false

    var suggestionsShownCount = 0
    var previousSuggestionsShownCount = 0

    var suggestionGaiaId: String? = null
    var suggestionType: String? = null
    var displayName: String? = null

    fun updateData(suggestion: SuggestionV4) {
        this.isHistory = suggestion.isHistoryItem
        this.suggestionGaiaId = suggestion.gaiaId
        this.suggestionType = suggestion.type
        this.displayName = HtmlCompat.stripHtml(suggestion.regionNames.displayName)
    }

    fun reset() {
        suggestionsFocused = false
        suggestionSelected = false

        charactersTypedCount = 0
        selectedSuggestionPosition = 0

        isChild = false
        isParent = false
        suggestionsShownCount = 0
        previousSuggestionsShownCount = 0

        suggestionGaiaId = null
        suggestionType = null
        displayName = null
    }
}
