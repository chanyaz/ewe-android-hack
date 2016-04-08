package com.expedia.bookings.data.packages

class PackageApiError {

    enum class Code {

        //Package search error
        search_response_null,
        pkg_unknown_error,
        pkg_destination_resolution_failed,
        pkg_flight_no_longer_available,
        pkg_invalid_checkin_checkout_dates
    }

    val errorCode: Code? = null

}