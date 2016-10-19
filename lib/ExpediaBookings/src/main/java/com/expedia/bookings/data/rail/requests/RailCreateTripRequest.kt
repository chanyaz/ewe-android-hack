package com.expedia.bookings.data.rail.requests

// variable names in this model have to match 1:1 to the format the api expects
class RailCreateTripRequest(val offerTokens: List<String>) {
    val pos = PointOfSaleKey()
    val locale = "en_GB"
    val clientCode = "12345"
}