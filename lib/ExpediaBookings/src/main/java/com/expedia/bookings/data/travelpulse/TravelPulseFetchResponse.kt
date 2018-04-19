package com.expedia.bookings.data.travelpulse

class TravelPulseFetchResponse {
    var metadata: TravelPulseMetadata? = null
    var results: List<TravelPulseResult> = emptyList()

    data class TravelPulseMetadata(var userContext: TravelPulseUserContext? = null)

    data class TravelPulseUserContext(
            var siteId: String? = null,
            var expUserId: String? = null,
            var guid: String? = null)

    data class TravelPulseResult(
        var product: String? = null,
        var type: String? = null,
        var items: List<TravelPulseItem> = emptyList())

    data class TravelPulseItem(
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
            var routeType: String? = null)

    data class ShortlistItem(
            var id: String? = null,
            var itemId: String? = null,
            var configId: String? = null,
            var guid: String? = null,
            var uid: String? = null,
            var expUserId: String? = null,
            var siteId: String? = null,
            var metaData: ShortlistItemMetaData? = null,
            var lastModifiedDate: LastModifiedDate? = null
    )

    data class ShortlistItemMetaData(
            var hotelId: String? = null,
            var chkIn: String? = null,
            var chkOut: String? = null,
            var roomConfiguration: String? = null)

    data class LastModifiedDate(var nano: Long? = null, var epochSecond: Long? = null)
}
