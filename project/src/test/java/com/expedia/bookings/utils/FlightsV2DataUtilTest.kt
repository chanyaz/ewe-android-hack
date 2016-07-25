package com.expedia.bookings.utils

import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.Location
import org.joda.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals

class FlightsV2DataUtilTest {

    @Test
    fun flightSearchParamsTest() {
        val gson = FlightsV2DataUtil.generateGson()
        val flightSearchParams = getDummyFlightSearchParams()
        val toJson = gson.toJson(flightSearchParams)
        val newParams = FlightsV2DataUtil.getFlightSearchParamsFromJSON(toJson)
        assertEquals(flightSearchParams, newParams)
    }

    @Test
    fun getSuggestionFromLocationTest() {
        val deeplinkLocation = "SFO"
        val suggestionFromDeeplinkLocation = FlightsV2DataUtil.getSuggestionFromDeeplinkLocation(deeplinkLocation)
        assertEquals(deeplinkLocation, suggestionFromDeeplinkLocation?.regionNames?.displayName)
        assertEquals(deeplinkLocation, suggestionFromDeeplinkLocation?.regionNames?.fullName)
        assertEquals(deeplinkLocation, suggestionFromDeeplinkLocation?.regionNames?.shortName)
        assertEquals(deeplinkLocation, suggestionFromDeeplinkLocation?.hierarchyInfo?.airport?.airportCode)
    }
    
    private fun getDummyFlightSearchParams(): FlightSearchParams {
        val flightSearchParams = FlightSearchParams()
        val location = Location()
        location.destinationId = "SFO"
        flightSearchParams.arrivalLocation = location
        val location1 = Location()
        location1.destinationId = "LAS"
        flightSearchParams.departureLocation = location1
        flightSearchParams.departureDate = LocalDate.now()
        flightSearchParams.returnDate = LocalDate.now().plusDays(1)
        flightSearchParams.numAdults = 3
        return flightSearchParams
    }

}
