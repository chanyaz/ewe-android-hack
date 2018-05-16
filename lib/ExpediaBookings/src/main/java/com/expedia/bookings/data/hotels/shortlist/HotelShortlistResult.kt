package com.expedia.bookings.data.hotels.shortlist

data class HotelShortlistResult<T>(
        var product: String? = null,
        var type: String? = null,
        var items: List<T> = emptyList())
