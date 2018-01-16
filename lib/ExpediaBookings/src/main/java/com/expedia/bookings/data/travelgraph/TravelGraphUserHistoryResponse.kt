package com.expedia.bookings.data.travelgraph

class TravelGraphUserHistoryResponse {
    var metadata: TravelGraphMetadata? = null
    var message: String? = null
    var results: List<TravelGraphUserHistoryResult> = emptyList()

    fun getSearchHistoryResultFor(travelGraphLOB: TravelGraphItemLOB): TravelGraphUserHistoryResult? {
        return results.find { result ->
            result.product == travelGraphLOB && result.action == TravelGraphUserHistoryResponse.TravelGraphItemType.SEARCH
        }
    }

    class TravelGraphMetadata {
        var transactionGUID: String? = null
        var userContext: TravelGraphUserContext? = null
    }

    class TravelGraphUserContext {
        var expUserId: String? = null
        var guid: String? = null
        var siteId: String? = null
    }

    enum class TravelGraphItemLOB {
        HOTEL
    }

    enum class TravelGraphItemType {
        SEARCH,
        DETAIL
    }
}
