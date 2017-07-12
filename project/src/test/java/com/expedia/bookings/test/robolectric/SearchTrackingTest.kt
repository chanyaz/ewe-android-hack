package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class SearchTrackingTest {
    private lateinit var sut: FlightSearchTrackingDataBuilder

    @Before
    fun setup() {
        sut = FlightSearchTrackingDataBuilder()
    }

    @Test
    fun testPopulatingTrackingData() {
        val dummySearchParams = setupFlightSearchParams(true)
        //TrackingData has no Information of SearchParams
        assertFalse(sut.isWorkComplete())
        //TrackingData populated with SearchParams
        sut.searchParams(dummySearchParams)
        assertFalse(sut.isWorkComplete())
        //Response is received
        sut.searchResponse(emptyList())
        assertFalse(sut.isWorkComplete())
        //Response is Usable
        sut.markResultsUsable()
        assertTrue(sut.isWorkComplete())
        compareSearchParamsAndTrackingData(dummySearchParams, true)
        assertFalse(sut.isWorkComplete())
    }

    @Test
    fun testSearchParamsInTrackingData() {
        var dummySearchParams = setupFlightSearchParams(false)
        sut.searchParams(dummySearchParams)
        sut.searchResponse(emptyList())
        sut.markResultsUsable()
        //For One Way Trip when return date is null
        compareSearchParamsAndTrackingData(dummySearchParams, false)
        dummySearchParams = setupFlightSearchParams(true)
        sut.searchParams(dummySearchParams)
        sut.searchResponse(emptyList())
        sut.markResultsUsable()
        //For Round Trip when return date is non-null
        compareSearchParamsAndTrackingData(dummySearchParams, true)
    }

    @Test
    fun testTimeToLoadUsable() {
        sut.markSearchClicked()
        sut.searchParams(setupFlightSearchParams(false))
        sut.searchResponse(emptyList())
        sut.markResultsUsable()
        assertNotNull(sut.build().performanceData.getPageLoadTime())
    }

    private fun compareSearchParamsAndTrackingData(dummySearchParams: FlightSearchParams, isRoundTrip: Boolean) {
        val searchTrackingData = sut.build()
        assertEquals(dummySearchParams.departureAirport, searchTrackingData.departureAirport)
        assertEquals(dummySearchParams.arrivalAirport, searchTrackingData.arrivalAirport)
        assertEquals(dummySearchParams.departureDate, searchTrackingData.departureDate)
        if (isRoundTrip) {
            assertEquals(dummySearchParams.returnDate, searchTrackingData.returnDate)
        } else {
            assertNull(searchTrackingData.returnDate)
        }
        assertEquals(2, searchTrackingData.adults)
        assertEquals(2, searchTrackingData.children.size)
        assertEquals(4, searchTrackingData.guests)
        assertFalse(sut.isWorkComplete())
    }

    private fun setupFlightSearchParams(isRoundTrip: Boolean): FlightSearchParams {
        val departureSuggestion = SuggestionV4()
        departureSuggestion.gaiaId = "1234"
        val departureRegionNames = SuggestionV4.RegionNames()
        departureRegionNames.displayName = "San Francisco"
        departureRegionNames.shortName = "SFO"
        departureSuggestion.regionNames = departureRegionNames

        val testDepartureCoordinates = SuggestionV4.LatLng()
        testDepartureCoordinates.lat = 600.5
        testDepartureCoordinates.lng = 300.3
        departureSuggestion.coordinates = testDepartureCoordinates

        val arrivalSuggestion = SuggestionV4()
        arrivalSuggestion.gaiaId = "5678"
        val arrivalRegionNames = SuggestionV4.RegionNames()
        arrivalRegionNames.displayName = "Los Angeles"
        arrivalRegionNames.shortName = "LAX"
        arrivalSuggestion.regionNames = arrivalRegionNames
        arrivalSuggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        childList.add(2)
        childList.add(4)
        val startDate = LocalDate().plusDays(2)
        var endDate: LocalDate? = null
        if (isRoundTrip) {
            endDate = LocalDate().plusDays(3)
        }

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, startDate, endDate, 2, childList, false, null, null, null, null, null,null)
    }
}
