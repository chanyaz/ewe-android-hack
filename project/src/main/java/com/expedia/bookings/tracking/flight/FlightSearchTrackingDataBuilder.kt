package com.expedia.bookings.tracking.flight

import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.tracking.AbstractTrackingDataBuilder

class FlightSearchTrackingDataBuilder : AbstractTrackingDataBuilder<FlightSearchTrackingData>() {
    override var trackingData = FlightSearchTrackingData()

    fun searchParams(searchParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        populateSearchParamFields(searchParams)
        paramsPopulated = true
    }

    fun searchResponse(flightLegs: List<FlightLeg>) {
        populateSearchResponseFields(flightLegs)
        responsePopulated = true
    }

    override fun build(): FlightSearchTrackingData {
        if (isWorkComplete()) {
            paramsPopulated = false
            responsePopulated = false
            responseTimePopulated = false
            return trackingData
        } else {
            throw IllegalStateException("Search Params and Search Response should be populated before building search tracking data")
        }
    }

    private fun populateSearchParamFields(searchParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        trackingData.departureAirport = searchParams.departureAirport
        trackingData.arrivalAirport = searchParams.arrivalAirport
        trackingData.departureDate = searchParams.departureDate
        trackingData.returnDate = searchParams.returnDate
        trackingData.adults = searchParams.adults
        trackingData.children = searchParams.children
        trackingData.guests = searchParams.guests
        trackingData.infantSeatingInLap = searchParams.infantSeatingInLap
    }

    private fun populateSearchResponseFields(flightLegs: List<FlightLeg>) {
        trackingData.resultsReturned = true
        trackingData.flightLegList = flightLegs
    }
}