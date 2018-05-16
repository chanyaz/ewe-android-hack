package com.expedia.bookings.data.hotels.shortlist

data class ShortlistItemMetadata(
        var hotelId: String? = null,
        var chkIn: String? = null,
        var chkOut: String? = null,
        var roomConfiguration: String? = null)
