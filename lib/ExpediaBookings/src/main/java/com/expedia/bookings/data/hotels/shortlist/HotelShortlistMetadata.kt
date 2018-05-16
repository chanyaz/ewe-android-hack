package com.expedia.bookings.data.hotels.shortlist

data class HotelShortlistMetadata(var userContext: HotelShortlistUserContext? = null) {
    data class HotelShortlistUserContext(
            var siteId: String? = null,
            var expUserId: String? = null,
            var guid: String? = null)
}
