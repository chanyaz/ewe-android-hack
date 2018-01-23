package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.Location
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.text.HtmlCompat
import com.mobiata.flightlib.data.Airport
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightsV2DataUtilTest {

    private val context = RuntimeEnvironment.application

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

    @Test
    fun getSuggestionV4FromAirportTest() {

        val airport = getDummyAirport()
        val airportName = HtmlCompat.stripHtml(context.getString(R.string.dropdown_airport_selection, airport.mAirportCode, airport.mName))
        val suggestionV4 = FlightsV2DataUtil.getSuggestionV4FromAirport(context, airport)

        assertEquals(airportName, suggestionV4.regionNames.displayName)
        assertEquals(airportName, suggestionV4.regionNames.fullName)
        assertEquals(airportName, suggestionV4.regionNames.shortName)

        assertEquals(airport.mRegionId, suggestionV4.hierarchyInfo?.airport?.regionId)
        assertEquals(airport.mAirportCode, suggestionV4.hierarchyInfo?.airport?.airportCode)

        assertEquals(airport.mCountryCode, suggestionV4.hierarchyInfo?.country?.countryCode)
        assertEquals(airport.mCountry, suggestionV4.hierarchyInfo?.country?.name)

        assertEquals(false, suggestionV4.hierarchyInfo?.isChild)

        assertEquals(0.0, suggestionV4.coordinates.lat)
        assertEquals(0.0, suggestionV4.coordinates.lng)
    }

    private fun getDummyAirport(): Airport {
        val airport = Airport()
        airport.mAirportCode = "SYD"
        airport.mRegionId = "6139200"
        airport.mName = "Sydney"
        airport.mCountryCode = "AUS"
        airport.mCountry = "Australia"
        return airport
    }
}
