package com.expedia.bookings.data.packages

import com.expedia.bookings.tracking.hotel.PageUsableData

enum class PackagesPageUsableData(val pageUsableData: PageUsableData) {
    SEARCH(PageUsableData()),
    HOTEL_RESULTS(PageUsableData()),
    HOTEL_INFOSITE(PageUsableData()),
    FLIGHT_OUTBOUND(PageUsableData()),
    FLIGHT_INBOUND(PageUsableData()),
    RATE_DETAILS(PageUsableData())
}
