package com.expedia.bookings.tracking.flight

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.tracking.AbstractSearchTrackingData
import org.joda.time.LocalDate

class FlightSearchTrackingData : AbstractSearchTrackingData() {
    var departureAirport: SuggestionV4? = null
    var arrivalAirport: SuggestionV4? = null
    var departureDate: LocalDate? = null
    var returnDate: LocalDate? = null
    var adults: Int = 1
    var children: List<Int> = emptyList()
    var guests: Int = children.size + adults
    var infantSeatingInLap: Boolean = false
    var flightCabinClass: String? = null
    var showRefundableFlight: Boolean? = null
    var nonStopFlight: Boolean? = null

    var resultsReturned = false
    var flightLegList: List<FlightLeg> = emptyList()
}
