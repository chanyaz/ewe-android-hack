package com.expedia.bookings.extensions

import com.expedia.bookings.utils.RetrofitError

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
