package com.expedia.bookings.data.sos

import java.math.BigDecimal

class DealsDestination {
    internal var destinationInfo: DestinationInfo? = null
    internal var hotelPricingSummary: HotelPricingSummary? = null
    var hotels: List<Hotel>? = null

    val displayName: String?
        get() = destinationInfo!!.longName

    inner class DestinationInfo {
        internal var longName: String? = null
    }

    inner class HotelPricingSummary {
        internal var averagePrice: BigDecimal? = null
    }

    inner class Hotel {
        var offerDateRange: OfferDateRange? = null
        var destination: Destination? = null
        var offerMarkers: List<String>? = null
        var hotelPricingInfo: HotelPricingInfo? = null
        var hotelUrls: HotelUrls? = null

        inner class OfferDateRange {
            var travelStartDate: List<Int>? = null
            var travelEndDate: List<Int>? = null
        }

        inner class Destination {
            var shortName: String? = null
            var regionID: String? = null
            var city: String? = null
        }

        inner class HotelPricingInfo {
            var averagePriceValue: Double? = null
            var totalPriceValue: Double? = null
            var crossOutPriceValue: Double? = null
            var percentSavings: Double? = null
        }

        inner class HotelUrls {
            var hotelInfositeUrl: String? = null
            var hotelSearchResultUrl: String? = null
        }

        fun hasLeadingPrice(): Boolean {
            if (offerMarkers != null) {
                for (marker in offerMarkers!!) {
                    if (marker.equals(OfferMarker.LEADIN_PRICE.toString())) {
                        return true
                    }
                }
            }
            return false
        }
    }

    fun getLeadingHotel(): Hotel? {
        if (hotels != null) {
            for (hotel in hotels!!) {
                if (hotel.hasLeadingPrice()) {
                    return hotel
                }
            }
        }
        return null
    }

    enum class OfferMarker private constructor(private val stringValue: String) {
        LEADIN_PRICE("LEADIN_PRICE"),
        HIGHEST_STAR_RATING("HIGHEST_STAR_RATING"),
        HIGHEST_GUEST_RATING("HIGHEST_GUEST_RATING"),
        HIGHEST_DISCOUNT("HIGHEST_DISCOUNT");

        override fun toString(): String {
            return stringValue
        }
    }
}
