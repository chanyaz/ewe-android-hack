package com.expedia.bookings.data.flights

import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.payment.Traveler
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

class FlightCheckoutResponse() : FlightTripResponse() {
    override fun getTripTotalExcludingFee(): Money {
        throw UnsupportedOperationException("TripTotalExcludingFee is not implemented for flights. totalPrice field is untouched/fee-less")
    }

    override fun tripTotalPayableIncludingFeeIfZeroPayableByPoints(): Money {
        val totalPrice = details.offer.totalPrice.copy()
        return totalPrice
    }

    override fun isCardDetailsRequiredForBooking(): Boolean {
        return true
    }

    @SerializedName("flightDetailResponse")
    override lateinit var details: FlightTripDetails

    class FlightAggregatedResponse {
        lateinit var flightsDetailResponse: List<FlightTripDetails>
    }

    var flightAggregatedResponse: FlightAggregatedResponse? = null

    val currencyCode: String? = null
    val orderId: String? = null
    var totalChargesPrice: Money? = null

    @SerializedName("mobileAirAttachQualifier")
    val airAttachInfo: AirAttachInfo? = null

    class AirAttachInfo {
        @SerializedName("airAttachQualified")
        val hasAirAttach: Boolean = false
        @SerializedName("offerExpiresTime")
        val offerExpirationTimes: AirAttachExpirationInfo? = null

        class AirAttachExpirationInfo {
            @SerializedName("raw")
            private val fullExpirationDate: String? = null
            fun airAttachExpirationTime(): DateTime {
                return DateTime.parse(fullExpirationDate)
            }
        }
    }

    lateinit var passengerDetails: List<Traveler>

    /**
     * This method's been created because the aggregated response tag is missing in case of price change.
     * Every other time, we should be relying on aggregated response to give us the flight trip details
     */
    fun getFirstFlightTripDetails(): FlightTripDetails {
        val flightsDetailResponse = flightAggregatedResponse?.flightsDetailResponse
        return if (flightsDetailResponse != null) flightsDetailResponse[0] else details
    }

    fun getFirstFlightLeg(): FlightLeg {
        val legs = getFirstFlightTripDetails().getLegs()
        return legs.first()
    }

    fun getFirstFlightFirstSegment(): FlightLeg.FlightSegment {
        return getFirstFlightLeg().segments[0]
    }

    fun getFirstFlightLastSegment(): FlightLeg.FlightSegment {
        val segments = getFirstFlightLeg().segments
        return segments[segments.size - 1]
    }

    /**
     * This method's been created because the aggregated response tag is missing in case of price change.
     * Every other time, we should be relying on aggregated response to give us the flight trip details
     */
    fun getLastFlightTripDetails(): FlightTripDetails {
        val flightsDetailResponse = flightAggregatedResponse?.flightsDetailResponse
        return if (flightsDetailResponse != null) flightsDetailResponse[flightsDetailResponse.size - 1] else details
    }

    fun getLastFlightLeg(): FlightLeg {
        val legs = getLastFlightTripDetails().getLegs()
        return legs[legs.size - 1]
    }

    fun getLastFlightLastSegment(): FlightLeg.FlightSegment {
        val segments = getLastFlightLeg().segments
        return segments[segments.size - 1]
    }

    fun getFirstSegmentOfLastFlightLeg(): FlightLeg.FlightSegment {
        val segments = getLastFlightLeg().segments
        return segments[0]
    }

    override fun getOffer(): FlightTripDetails.FlightOffer {
        val flightsDetailResponse = flightAggregatedResponse?.flightsDetailResponse
        return if (flightsDetailResponse != null) flightsDetailResponse[0].offer else details.offer
    }
}
