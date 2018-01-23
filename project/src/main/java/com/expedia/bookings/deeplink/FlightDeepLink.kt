package com.expedia.bookings.deeplink

import org.joda.time.LocalDate

class FlightDeepLink : DeepLink() {
    var origin: String? = null
    var destination: String? = null
    var departureDate: LocalDate? = null
    var returnDate: LocalDate? = null
    var numAdults: Int = 0
}
