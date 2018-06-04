package com.expedia.bookings.data.packages

import com.expedia.bookings.tracking.hotel.PageUsableData

enum class PackagesPageUsableData(val pageUsableData: PageUsableData) {
    SEARCH(PageUsableData()),
    HOTEL_RESULTS(PageUsableData()),
    HOTEL_FILTERED_RESULTS(PageUsableData()),
    HOTEL_INFOSITE(PageUsableData()),
    FLIGHT_OUTBOUND(PageUsableData()),
    FLIGHT_OUTBOUND_DETAILS(PageUsableData()),
    FLIGHT_INBOUND(PageUsableData()),
    FLIGHT_INBOUND_DETAILS(PageUsableData()),
    RATE_DETAILS(PageUsableData())
}
