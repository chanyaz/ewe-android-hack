package com.expedia.bookings.data.packages

class MultiItemApiCreateTripResponse {
    var errors: List<Error>? = null
    lateinit var tripId: String

    class Error {
        lateinit var key: String
    }
}
