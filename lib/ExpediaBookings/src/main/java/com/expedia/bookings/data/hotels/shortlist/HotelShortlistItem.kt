package com.expedia.bookings.data.hotels.shortlist

data class HotelShortlistItem(
        var templateName: String? = null,
        var shortlistItem: ShortlistItem? = null,
        var link: String? = null,
        var image: String? = null,
        var title: String? = null,
        var blurb: String? = null,
        var id: String? = null,
        var name: String? = null,
        var description: String? = null,
        var media: String? = null,
        var rating: String? = null,
        var guestRating: String? = null,
        var numberOfReviews: String? = null,
        var numberOfRooms: String? = null,
        var price: String? = null,
        var regionId: String? = null,
        var currency: String? = null,
        var tripLocations: String? = null,
        var tripDates: String? = null,
        var routeType: String? = null) {

    fun getHotelId(): String? {
        val metadata = shortlistItem?.metaData
        return if (metadata?.hotelId.isNullOrBlank()) shortlistItem?.itemId else metadata?.hotelId
    }
}
