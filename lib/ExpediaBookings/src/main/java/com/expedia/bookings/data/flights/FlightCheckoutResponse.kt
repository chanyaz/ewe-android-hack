package com.expedia.bookings.data.flights

import com.expedia.bookings.data.Money
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

class FlightCheckoutResponse() : FlightCreateTripResponse() {
    val currencyCode: String? = null
    val orderId: String? = null
    val totalChargesPrice: Money? = null

    @SerializedName("mobileAirAttachQualifier")
    val airAttachInfo: AirAttachInfo? = null
    private val flightDetailResponse: FlightTripDetails? = null

    override fun getDetails(): FlightTripDetails? {
        return flightDetailResponse
    }

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
}