package com.expedia.bookings.data.sos

open class DealsResponse {
    var offerInfo: OfferInfo? = null
    var offerErrorInfo: ErrorInfo? = null
    var debugInformation: DebugInformation? = null
    var destinations: List<DealsDestination>? = null
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
}
