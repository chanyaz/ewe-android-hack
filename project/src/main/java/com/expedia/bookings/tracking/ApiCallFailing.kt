package com.expedia.bookings.tracking

sealed class ApiCallFailing(val apiCall: String, val errorCode: String) {
    class FlightSearch(val code: String) : ApiCallFailing("FLIGHT_SEARCH", code)
    class FlightCreateTrip(val code: String) : ApiCallFailing("FLIGHT_CREATE_TRIP", code)
    class FlightCheckout(val code: String) : ApiCallFailing("FLIGHT_CHECKOUT", code)
}
