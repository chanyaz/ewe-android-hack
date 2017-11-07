package com.expedia.bookings.data.travelgraph

class TravelGraphUserHistoryResponse {
    var metadata: TravelGraphMetadata? = null
    var message: String? = null
    var results: List<TravelGraphUserHistoryResult> = emptyList()

    inner class TravelGraphUserHistoryResult {
        var product: TravelGraphItemLOB? = null
        var action: TravelGraphItemType? = null
        var items: List<TravelGraphItem> = emptyList()
    }

    inner class TravelGraphMetadata {
        var transactionGUID: String? = null
        var userContext: TravelGraphUserContext? = null
    }

    inner class TravelGraphUserContext {
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
