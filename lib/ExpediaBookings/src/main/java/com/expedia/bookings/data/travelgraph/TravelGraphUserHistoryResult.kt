package com.expedia.bookings.data.travelgraph

import com.expedia.bookings.data.SuggestionV4
import java.util.ArrayList

class TravelGraphUserHistoryResult {
    var product: TravelGraphUserHistoryResponse.TravelGraphItemLOB? = null
    var action: TravelGraphUserHistoryResponse.TravelGraphItemType? = null
    var items: List<TravelGraphItem> = emptyList()

    private val maxSuggestion = 3

    fun convertToSuggestionV4List(): List<SuggestionV4> {
        val suggestionList = ArrayList<SuggestionV4>()
        for (tgItem in items) {
            if (suggestionList.count() >= maxSuggestion) {
                break
            }
            if (tgItem.isValid()) {
                suggestionList.add(tgItem.searchInfo!!.searchRegion!!.toSuggestionV4()!!)
            }
        }

        return suggestionList
    }
}
