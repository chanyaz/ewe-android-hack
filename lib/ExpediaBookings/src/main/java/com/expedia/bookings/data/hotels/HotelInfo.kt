package com.expedia.bookings.data.hotels

import com.expedia.bookings.utils.NumberUtils
import com.google.gson.annotations.SerializedName

class HotelInfo {
    var id: String = ""
    var localizedHotelName: String = ""
    var isPinned: Boolean = false
    var isSponsored: Boolean = false
    var hasFreeCancel: Boolean = false
    var hasPayLater: Boolean = false
    var roomsLeftAtThisPrice: Int = 0
    var starRating: Float = 0f
    var guestRating: Float = 0f
    var price: HotelPriceInfo? = null
    var vip: Boolean = false
    var imageUrl: String = ""
    var lowResImageUrl: String = ""
    var latLong: List<Double> = emptyList()
    var regionName: String = ""
    var regionId: String = ""
    var directDistance: ProximityDistance? = null
    var neighborhoodId: String = ""
    var neighborhoodName: String = ""
    var impressionTrackingUrl: String = ""
    var clickTrackingUrl: String = ""
    var isAvailable: Boolean = false

    fun convertToLegacyHotel(): Hotel {
        val legacyHotel = Hotel()
        legacyHotel.hotelId = id
        legacyHotel.localizedName = localizedHotelName
        legacyHotel.isSponsoredListing = isSponsored
        legacyHotel.hasFreeCancellation = hasFreeCancel
        legacyHotel.isPaymentChoiceAvailable = hasPayLater
        legacyHotel.isShowEtpChoice = hasPayLater
        legacyHotel.roomsLeftAtThisRate = roomsLeftAtThisPrice
        legacyHotel.hotelStarRating = starRating
        legacyHotel.hotelGuestRating = NumberUtils.round(guestRating, 2)
        populateLegacyHotelRate(legacyHotel)
        legacyHotel.isVipAccess = vip
        legacyHotel.largeThumbnailUrl = imageUrl
        legacyHotel.thumbnailUrl = lowResImageUrl
        populateLegacyLatLong(legacyHotel)
        legacyHotel.locationId = if (neighborhoodId.isNotBlank()) neighborhoodId else regionId
        populateLegacyDistance(legacyHotel)
        legacyHotel.neighborhoodName = if (neighborhoodName.isNotBlank()) neighborhoodName else regionName
        legacyHotel.isHotelAvailable = isAvailable
        legacyHotel.impressionTrackingUrl = impressionTrackingUrl
        legacyHotel.clickTrackingUrl = clickTrackingUrl
        legacyHotel.isSoldOut = !isAvailable

        return legacyHotel
    }

    private fun populateLegacyHotelRate(legacyHotel: Hotel) {
        price?.let { price ->
            legacyHotel.lowRateInfo = price.convertToHotelRate()
            legacyHotel.rateCurrencyCode = price.currencyCode
        }
    }

    private fun populateLegacyLatLong(legacyHotel: Hotel) {
        if (latLong.size == 2) {
            legacyHotel.latitude = latLong.first()
            legacyHotel.longitude = latLong.last()
        }
    }

    private fun populateLegacyDistance(legacyHotel: Hotel) {
        directDistance?.let { directDistance ->
            when (directDistance.unit) {
                DistanceUnit.KM -> {
                    legacyHotel.proximityDistanceInKiloMeters = directDistance.value
                    legacyHotel.proximityDistanceInMiles = kilometersToMiles(directDistance.value)
                    legacyHotel.distanceUnit = "Kilometers"
                }
                DistanceUnit.MILES -> {
                    legacyHotel.proximityDistanceInKiloMeters = milesToKilometers(directDistance.value)
                    legacyHotel.proximityDistanceInMiles = directDistance.value
                    legacyHotel.distanceUnit = "Miles"
                }
            }
        }
    }

    private fun milesToKilometers(miles: Double): Double {
        return miles * 1.609344
    }

    private fun kilometersToMiles(kilometers: Double): Double {
        return kilometers * 0.621371192
    }

    data class ProximityDistance(val value: Double, val unit: DistanceUnit)

    enum class DistanceUnit {
        @SerializedName("km")
        KM,
        @SerializedName("miles")
        MILES
    }
}
