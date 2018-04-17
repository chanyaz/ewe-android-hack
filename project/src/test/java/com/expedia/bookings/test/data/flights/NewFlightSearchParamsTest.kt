package com.expedia.bookings.test.data.flights

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Constants
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class NewFlightSearchParamsTest {
    private val maxStay = 26
    private val maxRange = 329
    val builder = FlightSearchParams.Builder(maxStay, maxRange)

    private val expectedOrigin = getDummySuggestion("San Francisco", "SFO")
    private val expectedDestination = getDummySuggestion("Seattle", "SEA")

    private val expectedNumAdults = 2
    private val tomorrow = LocalDate.now().plusDays(1)
    private val expectedReturnDate = tomorrow.plusDays(2)
    private lateinit var expectedChildrenString: String
    private val expectedNumChildren = 2

    var children = ArrayList<Int>()

    @Before
    fun setup() {
        children.add(3)
        children.add(5)
        expectedChildrenString = "3,5"
    }

    @Test
    fun testBuilderStatusChildrenOnly() {
        builder.children(children)
        assertFalse(builder.hasStart())
        assertFalse(builder.hasEnd())
        assertFalse(builder.hasOriginLocation())
        assertFalse(builder.hasDestinationLocation())
        assertFalse(builder.areRequiredParamsFilled())
        assertFalse(builder.hasValidDateDuration())
    }

    @Test
    fun testBuilderStatusAdultsOnly() {
        builder.adults(expectedNumAdults)
        assertFalse(builder.hasStart())
        assertFalse(builder.hasEnd())
        assertFalse(builder.hasOriginLocation())
        assertFalse(builder.hasDestinationLocation())
        assertFalse(builder.areRequiredParamsFilled())
        assertFalse(builder.hasValidDateDuration())
    }

    @Test
    fun testBuilderStatusDepartOnly() {
        builder.startDate(tomorrow)
        assertTrue(builder.hasStart())
        assertTrue(builder.hasValidDateDuration())

        assertFalse(builder.hasEnd())
        assertFalse(builder.hasOriginLocation())
        assertFalse(builder.hasDestinationLocation())
        assertFalse(builder.areRequiredParamsFilled())
    }

    @Test
    fun testInvalidMaxStay() {
        val dateOutOfRange = tomorrow.plusDays(maxStay + 2)
        builder.startDate(tomorrow)
        builder.endDate(dateOutOfRange)
        assertFalse(builder.hasValidDateDuration())
    }

    @Test
    fun testBuilderStatusValidDates() {
        builder.startDate(tomorrow)
        builder.endDate(expectedReturnDate)
        assertTrue(builder.hasStart())
        assertTrue(builder.hasEnd())
        assertTrue(builder.hasValidDateDuration())

        assertFalse(builder.hasOriginLocation())
        assertFalse(builder.hasDestinationLocation())
        assertFalse(builder.areRequiredParamsFilled())
    }

    @Test
    fun testBuilderStatusMissingDeparture() {
        builder.startDate(tomorrow)
        builder.endDate(expectedReturnDate)
        builder.origin(expectedOrigin)

        assertTrue(builder.hasStart())
        assertTrue(builder.hasEnd())
        assertTrue(builder.hasValidDateDuration())

        assertTrue(builder.hasOriginLocation())
        assertFalse(builder.hasDestinationLocation())
        assertFalse(builder.areRequiredParamsFilled())
    }

    @Test
    fun testBuilderStatusIdenticalLocations() {
        builder.origin(expectedOrigin)
        builder.destination(expectedOrigin)

        assertTrue(builder.hasOriginLocation())
        assertTrue(builder.hasDestinationLocation())
        assertTrue(builder.isOriginSameAsDestination())
    }

    @Test
    fun testBuilderStatusValidLocations() {
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)

        assertTrue(builder.hasOriginLocation())
        assertTrue(builder.hasDestinationLocation())

        assertFalse(builder.isOriginSameAsDestination())
        assertFalse(builder.hasStart())
        assertFalse(builder.hasEnd())
        assertFalse(builder.areRequiredParamsFilled())
        assertFalse(builder.hasValidDateDuration())
    }

    @Test
    fun testBuilderStatusAllValid() {
        builder.adults(expectedNumAdults)
        builder.startDate(tomorrow)
        builder.endDate(tomorrow.plusDays(1))
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)

        assertTrue(builder.hasStart())
        assertTrue(builder.hasEnd())
        assertTrue(builder.hasOriginLocation())
        assertTrue(builder.hasDestinationLocation())
        assertTrue(builder.areRequiredParamsFilled())
        assertTrue(builder.hasValidDateDuration())
    }

    @Test
    fun testBuilderBuildValid() {
        builder.adults(expectedNumAdults)
        builder.children(children)
        builder.startDate(tomorrow)
        builder.endDate(expectedReturnDate)
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)

        val params = builder.build()
        assertEquals(expectedNumAdults.toLong(), params.adults.toLong())
        assertEquals(expectedNumChildren.toLong(), params.children.size.toLong())
        assertEquals(tomorrow, params.departureDate)
        assertEquals(expectedReturnDate, params.returnDate)
        assertEquals(expectedChildrenString, params.childrenString)
        assertEquals(expectedOrigin.hierarchyInfo?.airport, params.departureAirport.hierarchyInfo?.airport)
        assertEquals(expectedDestination.hierarchyInfo?.airport, params.arrivalAirport.hierarchyInfo?.airport)
    }

    @Test
    fun testEndOfTripOneWay() {
        builder.adults(expectedNumAdults)
        builder.startDate(tomorrow)
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)
        val params = builder.build()

        assertEquals(tomorrow, params.getEndOfTripDate())
    }

    @Test
    fun testEndOfTripRoundTrip() {
        builder.adults(expectedNumAdults)
        builder.startDate(tomorrow)
        builder.endDate(expectedReturnDate)
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)
        val params = builder.build()

        assertEquals(expectedReturnDate, params.getEndOfTripDate())
        assertNotEquals(params.startDate, params.getEndOfTripDate())
    }

    @Test
    fun testBuildParamsForAdvanceSearchFilter() {
        builder.adults(expectedNumAdults)
        builder.startDate(tomorrow)
        builder.endDate(expectedReturnDate)
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)
        var params = builder.build()

        assertNull(params.nonStopFlight)
        assertNull(params.showRefundableFlight)

        params = builder.nonStopFlight(true).build()

        assertTrue(params.nonStopFlight!!)
        assertNull(params.showRefundableFlight)

        params = builder.showRefundableFlight(true).build()
        assertTrue(params.nonStopFlight!!)
        assertTrue(params.showRefundableFlight!!)
    }

    @Test
    fun testBuildParamsForInboundSearchWithoutSeatClassPreference() {
        val params = giveSearchParams(Constants.FEATURE_SUBPUB)
        val inboundSearchParams = giveSearchParams(Constants.FEATURE_SUBPUB).buildParamsForInboundSearch(maxStay, maxRange, "outboundleg")
        assertEquals(params.adults, inboundSearchParams.adults)
        assertEquals(params.children.size, inboundSearchParams.children.size)
        assertEquals(params.departureDate, inboundSearchParams.departureDate)
        assertEquals(params.returnDate, inboundSearchParams.returnDate)
        assertEquals(params.departureAirport, inboundSearchParams.departureAirport)
        assertEquals(params.arrivalAirport, inboundSearchParams.arrivalAirport)
        assertEquals(1, inboundSearchParams.legNo)
        assertNull(inboundSearchParams.flightCabinClass)
        assertEquals("outboundleg", inboundSearchParams.selectedOutboundLegId)
        assertEquals(6400, inboundSearchParams.maxOfferCount)
    }

    @Test
    fun testBuildParamsForInboundSearchWithSeatClassPreference() {
        builder.adults(expectedNumAdults)
        builder.startDate(tomorrow)
        builder.endDate(expectedReturnDate)
        builder.origin(expectedOrigin)
        builder.destination(expectedDestination)
        builder.flightCabinClass("BUSINESS")
        val params = builder.build()
        val inboundSearchParams = params.buildParamsForInboundSearch(maxStay, maxRange, "outboundleg")

        assertEquals("BUSINESS", inboundSearchParams.flightCabinClass)
    }

    @Test
    fun testCachedSearchParams() {
        val cachedParams = giveSearchParams(Constants.FEATURE_SUBPUB).buildParamsForCachedSearch(maxStay, maxRange)
        assertEquals("SubPub,FlightSearchCacheGet", cachedParams.featureOverride)
    }

    @Test
    fun testEvolableSearchParams() {
        val flightSearchParams = giveSearchParams(Constants.FEATURE_EVOLABLE)
        assertEquals("GetEvolable", flightSearchParams.featureOverride)
    }

    @Test
    fun testSubpubEvolableSearchParams() {
        val flightSearchParams = giveSearchParams(Constants.FEATURE_SUBPUB, Constants.FEATURE_EVOLABLE)
        assertEquals("SubPub,GetEvolable", flightSearchParams.featureOverride)
        assertEquals(1600, flightSearchParams.maxOfferCount)
    }

    private fun giveSearchParams(vararg overrides: String): FlightSearchParams {
        for (flag in overrides) {
            builder.setFeatureOverride(flag)
        }

        return builder
                .origin(expectedOrigin)
                .destination(expectedOrigin)
                .startDate(tomorrow)
                .endDate(expectedReturnDate)
                .adults(expectedNumAdults)
                .build() as FlightSearchParams
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
