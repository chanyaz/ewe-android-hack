package com.expedia.bookings.data.hotels.shortlist

data class HotelShortlistResponse<T>(
        var metadata: HotelShortlistMetadata? = null,
        var results: List<HotelShortlistResult<T>> = emptyList())
