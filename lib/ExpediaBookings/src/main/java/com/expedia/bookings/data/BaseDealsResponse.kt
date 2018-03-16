package com.expedia.bookings.data

abstract class BaseDealsResponse {
    var offerInfo: OfferInfo? = null
    var offerErrorInfo: ErrorInfo? = null
    var debugInformation: DebugInformation? = null

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

    class OfferInfo {
        var currency: String? = null
        internal var language: String? = null
    }

    class DebugInformation {
        internal var activityId: String? = null
    }

    class ErrorInfo {
        internal var errorCode: Int = 0
        internal var errorMessage: String? = null
    }
}
