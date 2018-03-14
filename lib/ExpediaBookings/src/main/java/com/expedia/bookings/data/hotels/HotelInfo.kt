package com.expedia.bookings.data.hotels

class HotelInfo {
    var id: String = ""
    var regionId: String = ""
    var localizedHotelName: String = ""
    var isPinned: Boolean = false
    var isSponsored: Boolean = false
    var hasFreeCancel: Boolean = false
    var hasPayLater: Boolean = false
    var vip: Boolean = false
    var isAvailable: Boolean = false
    var starRating: Float = 0.toFloat()
    var guestRating: Float = 0.toFloat()
    var price: HotelPriceInfo? = null
    var imageUrl: String = ""
    var lowResImageUrl: String = ""
    var latLong: List<Double> = emptyList()
    var directDistance: ProximityDistance? = null

    data class ProximityDistance(val value: Double, val unit: DistanceUnit)

    enum class DistanceUnit {
        km,
        miles
    }
}
