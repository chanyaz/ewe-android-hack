package com.expedia.bookings.data.os

import com.expedia.bookings.data.sos.DealsDestination

open class LastMinuteDealsResponse {
    var offerInfo: OfferInfo? = null
    var offerErrorInfo: ErrorInfo? = null
    var debugInformation: DebugInformation? = null
    var offers: Offers? = null
        protected set

    fun hasError(): Boolean {
        return offerErrorInfo != null
    }

    val errorCode: Int
        get() {
            if (offerErrorInfo == null) {
                return -1
            }
            return offerErrorInfo!!.errorCode
        }

    inner class OfferInfo {
        var currency: String? = null
        internal var language: String? = null
    }

    inner class DebugInformation {
        internal var activityId: String? = null
    }

    inner class ErrorInfo {
        internal var errorCode: Int = 0
        internal var errorMessage: String? = null
    }

    inner class Offers {
        var Hotel: List<DealsDestination.Hotel>? = null
    }

    fun getLeadingHotels(): List<DealsDestination.Hotel> {
        if (offers?.Hotel != null) {
            return offers?.Hotel!!.filter { it.hasLeadingPrice() }
        }
        return emptyList()
    }
}
