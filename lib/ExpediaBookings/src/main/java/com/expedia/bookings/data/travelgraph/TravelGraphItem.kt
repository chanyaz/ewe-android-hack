package com.expedia.bookings.data.travelgraph

import com.expedia.bookings.data.multiitem.Price

class TravelGraphItem {
    var id: String? = null
    var startDateUTCTimestamp: Long? = null
    var endDateUTCTimestamp: Long? = null
    var lastViewedDateUTCTimestamp: Long? = null
    var price: Price? = null
    var searchInfo: TravelGraphHotelSearchInfo? = null
}