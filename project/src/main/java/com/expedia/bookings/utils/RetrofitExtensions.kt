package com.expedia.bookings.utils

fun RetrofitError.trackingString(): String {
    when (this) {
        RetrofitError.NO_INTERNET ->
            return "NetworkError"
        RetrofitError.TIMEOUT ->
            return "NetworkTimeOut"
        else ->
            return "UnknownRetrofitError"
    }
}
