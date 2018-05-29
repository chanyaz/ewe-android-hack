package com.expedia.bookings.test.data.flights

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightMultiDestinationSearchParam
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlightMultiDestinationSearchParamTest {

    private var arrivalAirport: SuggestionV4? = null
    private var departureAirport: SuggestionV4? = null
    private var departureDate = LocalDate.now().plusDays(1)

    val builder = FlightMultiDestinationSearchParam.Builder()

    @Before
    fun setup() {
        arrivalAirport = getDummySuggestion("San Francisco", "SFO")
        departureAirport = getDummySuggestion("Seattle", "SEA")
    }

    @Test
    fun testBuilderStatus() {
        builder.arrivalAirport(arrivalAirport)
        builder.departureAirport(departureAirport)
        builder.departureDate(departureDate)

        assertTrue(builder.hasArrivalAirport())
        assertTrue(builder.hasDepartureAirport())
        assertTrue(builder.hasDepartureDate())
    }

    @Test
    fun testBuilderBuildValid() {
        builder.arrivalAirport(arrivalAirport)
        builder.departureAirport(departureAirport)
        builder.departureDate(departureDate)

        val params = builder.build()
        assertEquals(arrivalAirport, params.arrivalAirport)
        assertEquals(departureAirport, params.departureAirport)
        assertEquals(departureDate, params.departureDate)
    }

    @Test
    fun testMultiDestinationSearchParamMap() {
        builder.arrivalAirport(arrivalAirport)
        builder.departureAirport(departureAirport)
        builder.departureDate(departureDate)

        val params = builder.build()
        var mapMultiDestinationSearchParams = params.multiDestinationSearchParamMap()
        assertEquals("SFO", mapMultiDestinationSearchParams["arrivalAirport"])
        assertEquals("SEA", mapMultiDestinationSearchParams["departureAirport"])
    }

    private fun getDummySuggestion(city: String, airport: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = city
        suggestion.regionNames.fullName = city
        suggestion.regionNames.shortName = city
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = airport
        return suggestion
    }
}
