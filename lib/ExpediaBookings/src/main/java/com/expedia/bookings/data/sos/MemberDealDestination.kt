package com.expedia.bookings.data.sos

import java.math.BigDecimal

class MemberDealDestination {
    internal var destinationInfo: DestinationInfo? = null
    internal var hotelPricingSummary: HotelPricingSummary? = null
    var hotels: List<Hotel>? = null

    val displayName: String
        get() = destinationInfo!!.longName!!

    inner class DestinationInfo {
        internal var regionID: String? = null
        internal var longName: String? = null
    }

    inner class HotelPricingSummary {
        internal var averagePrice: BigDecimal? = null
    }

    inner class Hotel {
        var offerDateRange: OfferDateRange? = null
        var destination: Destination? = null
        var offerMarkers: List<String>? = null
        var hotelInfo: HotelInfo? = null
        var hotelPricingInfo: HotelPricingInfo? = null
        var hotelUrls: HotelUrls? = null

        inner class OfferDateRange

        inner class Destination

        inner class HotelInfo {
            var travelStartDate: String? = null
            var travelEndDate: String? = null
        }

        inner class HotelPricingInfo {
            var totalPriceValue: Double? = null
        }

        inner class HotelUrls {
            var hotelInfositeUrl: String? = null
            var hotelSearchResultUrl: String? = null
        }
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
