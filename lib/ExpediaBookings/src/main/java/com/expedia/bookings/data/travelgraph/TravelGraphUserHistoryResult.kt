package com.expedia.bookings.data.travelgraph

import java.util.ArrayList

class TravelGraphUserHistoryResult {
    var product: TravelGraphUserHistoryResponse.TravelGraphItemLOB? = null
    var action: TravelGraphUserHistoryResponse.TravelGraphItemType? = null
    var items: List<TravelGraphItem> = emptyList()

    private val maxSuggestion = 3

    fun getRecentSearchInfos(): List<SearchInfo> {
        val recentSearches = ArrayList<SearchInfo>()
        for (item in items) {
            if (recentSearches.count() >= maxSuggestion) {
                break
            }

            val searchInfo = item.toRecentSearchInfo()
            if (searchInfo != null && searchInfo.isValid()) {
                recentSearches.add(searchInfo)
            }
        }
        return recentSearches
    }
}
