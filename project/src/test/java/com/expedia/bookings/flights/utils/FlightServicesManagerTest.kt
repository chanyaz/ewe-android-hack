package com.expedia.bookings.flights.utils

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightSearchResponse.FlightSearchType
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockFlightServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightServicesManagerTest {
    val context = RuntimeEnvironment.application
    var serviceRule = MockFlightServiceTestRule()
        @Rule get
    lateinit var sut: FlightServicesManager

    @Before
    fun setup() {
        sut = FlightServicesManager(serviceRule.services!!)
    }

    @Test
    fun testFlightsSuccessfulSearch() {
        val testSuccessHandler = TestObserver<Pair<FlightSearchType, FlightSearchResponse>>()
        val testErrorHandler = TestObserver<Pair<FlightSearchType, ApiError>>()

        val successHandler = PublishSubject.create<Pair<FlightSearchType, FlightSearchResponse>>()
        val errorHandler = PublishSubject.create<Pair<FlightSearchType, ApiError>>()

        successHandler.subscribe(testSuccessHandler)
        errorHandler.subscribe(testErrorHandler)

        sut.doFlightSearch(serviceRule.flightSearchParams(true), FlightSearchType.NORMAL, successHandler, errorHandler)

        testSuccessHandler.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, testSuccessHandler.valueCount())
        assertEquals(FlightSearchType.NORMAL, testSuccessHandler.values()[0].first)
        val expectedResponse = testSuccessHandler.values()[0].second
        assertEquals("leg0", expectedResponse.legs.get(0).legId)
    }

    @Test
    fun testFlightSearchErrorWithoutHTTPException() {
        testSearchError(serviceRule.flightSearchParams(false, "search_error"), ApiError.Code.FLIGHT_SEARCH_NO_RESULTS)
    }

    @Test
    fun testFlightSearchErrorWithNetworkErrorException() {
        testSearchError(serviceRule.flightSearchParams(false, "malformed"), ApiError.Code.NO_INTERNET)
    }

    private fun testSearchError(params: FlightSearchParams, expectedErrorCode: ApiError.Code) {
        val testSuccessHandler = TestObserver<Pair<FlightSearchType, FlightSearchResponse>>()
        val testErrorHandler = TestObserver<Pair<FlightSearchType, ApiError>>()

        val successHandler = PublishSubject.create<Pair<FlightSearchType, FlightSearchResponse>>()
        val errorHandler = PublishSubject.create<Pair<FlightSearchType, ApiError>>()

        successHandler.subscribe(testSuccessHandler)
        errorHandler.subscribe(testErrorHandler)

        sut.doFlightSearch(params, FlightSearchType.NORMAL, successHandler, errorHandler)

        testErrorHandler.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(FlightSearchType.NORMAL, testErrorHandler.values()[0].first)
        assertEquals(expectedErrorCode, testErrorHandler.values()[0].second.errorCode)
    }
}
