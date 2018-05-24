package com.expedia.bookings.unit

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.KongFlightServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.Constants
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class KongFlightServicesTest {
    var server: MockWebServer = MockWebServer()
        @Rule get

    var service: KongFlightServices? = null
    val logger = HttpLoggingInterceptor()
    val root = File("../mocked/templates").canonicalPath
    val opener = FileSystemOpener(root)

    @Before
    fun before() {
        logger.level = HttpLoggingInterceptor.Level.BODY
        service = KongFlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(), listOf(MockInterceptor()),
                Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun testMockSearchBlowsUpForRoundTrip() {
        server.enqueue(MockResponse()
                .setBody("{garbage}"))
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)
        val params = FlightSearchParams.Builder(26, 500)
                .origin(dummySuggestion)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams

        service!!.flightSearch(params, observer, resultsResponseReceived)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoValues()
        observer.assertError(IOException::class.java)
        resultsResponseReceivedTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testMockSearchWorks() {
        setServiceDispatcher()
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)
        val params = FlightSearchParams.Builder(26, 500)
                .flightCabinClass("COACH")
                .origin(dummySuggestion)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams

        service!!.flightSearch(params, observer, resultsResponseReceived)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
        resultsResponseReceivedTestSubscriber.assertValueCount(1)
        val response = observer.values()[0]
        Assert.assertEquals(7, response.legs.size.toLong())
        Assert.assertEquals(5, response.offers.size.toLong())
        Assert.assertEquals("coach", response.offers[0].offersSeatClassAndBookingCode[0][0].seatClass)
        Assert.assertEquals("-3.00", response.offers[0].discountAmount.amount.toString())
        Assert.assertEquals(Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", "AA"), response.legs[0].segments[0].airlineLogoURL)
    }

    @Test
    @Throws(Throwable::class)
    fun testMockGreedySearch() {
        setServiceDispatcher()
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)
        val params = FlightSearchParams.Builder(26, 500)
                .flightCabinClass("COACH")
                .origin(dummySuggestion)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams

        service!!.greedyFlightSearch(params, observer, resultsResponseReceived)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
        resultsResponseReceivedTestSubscriber.assertValueCount(1)
        val response = observer.values()[0]
        Assert.assertEquals(7, response.legs.size.toLong())
        Assert.assertEquals(5, response.offers.size.toLong())
        Assert.assertEquals("coach", response.offers[0].offersSeatClassAndBookingCode[0][0].seatClass)
        Assert.assertEquals("-3.00", response.offers[0].discountAmount.amount.toString())
        Assert.assertEquals(FlightSearchResponse.FlightSearchType.GREEDY, response.searchType)
        Assert.assertEquals(Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", "AA"), response.legs[0].segments[0].airlineLogoURL)
    }

    @Test
    fun testNewCreateTripWorks() {
        setServiceDispatcher()
        service = KongFlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(), listOf(MockInterceptor()),
                Schedulers.trampoline(), Schedulers.trampoline())

        val observer = TestObserver<FlightCreateTripResponse>()
        val params = FlightCreateTripParams.Builder()
                .productKey("happy_round_trip")
                .setNumberOfAdultTravelers(2)
                .setChildTravelerAge(listOf(1, 10))
                .setInfantSeatingInLap(true)
                .build()

        service!!.createTrip(params, observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
    }

    private fun setServiceDispatcher() {
        server.setDispatcher(ExpediaDispatcher(opener))
    }

    private val dummySuggestion: SuggestionV4
        get() {
            val suggestion = SuggestionV4()
            suggestion.gaiaId = ""
            suggestion.regionNames = SuggestionV4.RegionNames()
            suggestion.regionNames.displayName = ""
            suggestion.regionNames.fullName = ""
            suggestion.regionNames.shortName = ""
            suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
            suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
            suggestion.hierarchyInfo!!.airport!!.airportCode = ""
            return suggestion
        }
}
