package com.expedia.bookings.data.travelgraph

import com.expedia.bookings.data.multiitem.Price
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate

class TravelGraphItem {
    var id: String? = null
    var startDateUTCTimestamp: Long? = null
    var endDateUTCTimestamp: Long? = null
    var lastViewedDateUTCTimestamp: Long? = null
    var price: Price? = null
    var searchInfo: TravelGraphHotelSearchInfo? = null

    fun getStartDate(): LocalDate? {
        return if (startDateUTCTimestamp != null) LocalDate(startDateUTCTimestamp!!, DateTimeZone.UTC) else null
    }

    fun getEndDate(): LocalDate? {
        return if (endDateUTCTimestamp != null) LocalDate(endDateUTCTimestamp!!, DateTimeZone.UTC) else null
    }

    fun toRecentSearchInfo(): SearchInfo? {
        val builder = SearchInfo.Builder()
        builder.startDate(getStartDate())
        builder.endDate(getEndDate())
        builder.destination(searchInfo?.searchRegion?.toSuggestionV4())
        builder.travelers(searchInfo?.getTravelerInfo())
        return builder.build()
    }
}
