package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.google.gson.annotations.SerializedName

class PackageCheckoutResponse() : BaseApiResponse() {
    val newTrip: TripDetails? = null
    val orderId: String? = null
    val totalChargesPrice: Money? = null
    val packageDetails: PackageDetails? = null

    class PackageDetails {
        var tealeafTransactionId: String? = null
        var tripId: String? = null
        var itineraryNumber: String? = null
        var hotel: HotelCreateTripResponse.HotelProductResponse? = null
        var flight: FlightProduct? = null
        var pricing: Pricing? = null
    }

    class FlightProduct {
        var details: FlightTripDetails? = null
        @SerializedName("rules")
        var flightRules: FlightCreateTripResponse.FlightRules? = null
    }

    class Pricing {
        var packageTotal: Money? = null
        var basePrice: Money? = null
        var totalTaxesAndFees: Money? = null
        var hotelPrice: Money? = null
        var flightPrice: Money? = null
        var savings: Money? = null
        var taxesAndFeesIncluded: Boolean = false
        var hotelPricing: HotelPricing? = null
        var bundleTotal: Money? = null

        fun hasResortFee(): Boolean {
            return hotelPricing != null && hotelPricing!!.mandatoryFees != null &&
                    !hotelPricing!!.mandatoryFees?.feeTotal?.isZero()!!
        }
    }

    class HotelPricing {
        var mandatoryFees: MandatoryFees? = null
    }

    class MandatoryFees {
        var feeTotal: Money? = null
    }
}
