package com.expedia.bookings.unit

import com.mobiata.mocke3.FlightApiMockResponseGenerator
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlightServicesTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    var service: FlightServices? = null

    @Before
    fun before() {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        service = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(), listOf(MockInterceptor()),
                Schedulers.trampoline(), Schedulers.trampoline(), false)
    }

    @Test
    fun testMockSearchBlowsUp() {
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
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
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
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
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
    @Throws(Throwable::class)
    fun testMockGreedyCachedSearch() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)

        val origin = dummySuggestion
        origin.hierarchyInfo!!.airport!!.airportCode = "cached_bookable"
        val params = FlightSearchParams.Builder(26, 500)
                .setFeatureOverride(Constants.FEATURE_FLIGHT_CACHE)
                .flightCabinClass("COACH")
                .origin(origin)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams

        service!!.greedyCachedFlightSearch(params, observer, resultsResponseReceived)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
        resultsResponseReceivedTestSubscriber.assertValueCount(1)
        val response = observer.values()[0]
        Assert.assertEquals(4, response.legs.size.toLong())
        Assert.assertEquals(2, response.offers.size.toLong())
        Assert.assertTrue(response.isResponseCached())
        Assert.assertTrue(response.areCachedResultsBookable())
        Assert.assertFalse(response.areCachedResultsNonBookable())
        Assert.assertEquals(FlightSearchResponse.FlightSearchType.CACHED_GREEDY, response.searchType)
    }
    @Test
    fun testMockBookableCachedSearch() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)

        val origin = dummySuggestion
        origin.hierarchyInfo!!.airport!!.airportCode = "cached_bookable"
        val params = FlightSearchParams.Builder(26, 500)
                .setFeatureOverride(Constants.FEATURE_FLIGHT_CACHE)
                .flightCabinClass("COACH")
                .origin(origin)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams

        service!!.cachedFlightSearch(params, observer, resultsResponseReceived)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
        resultsResponseReceivedTestSubscriber.assertValueCount(1)
        val response = observer.values()[0]
        Assert.assertEquals(4, response.legs.size.toLong())
        Assert.assertEquals(2, response.offers.size.toLong())
        Assert.assertTrue(response.isResponseCached())
        Assert.assertTrue(response.areCachedResultsBookable())
        Assert.assertFalse(response.areCachedResultsNonBookable())
    }

    @Test
    fun testMockCachedResultsNotFound() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)

        val origin = dummySuggestion
        origin.hierarchyInfo!!.airport!!.airportCode = "cached_not_found"
        val params = FlightSearchParams.Builder(26, 500)
                .setFeatureOverride(Constants.FEATURE_FLIGHT_CACHE)
                .flightCabinClass("COACH")
                .origin(origin)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams

        service!!.cachedFlightSearch(params, observer, resultsResponseReceived)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
        resultsResponseReceivedTestSubscriber.assertValueCount(1)
        val response = observer.values()[0]
        Assert.assertEquals(0, response.legs.size.toLong())
        Assert.assertEquals(0, response.offers.size.toLong())
        Assert.assertTrue(response.isResponseCached())
        Assert.assertFalse(response.areCachedResultsBookable())
        Assert.assertTrue(response.areCachedResultsNonBookable())
    }

    @Test
    fun testMockOutboundSearchWorksForByot() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)

        val params = FlightSearchParams.Builder(26, 500)
                .legNo(0)
                .origin(dummySuggestionForByot)
                .destination(dummySuggestionForByot)
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
    }

    @Test
    fun testMockInboundSearchWorksForByot() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val resultsResponseReceived = PublishSubject.create<Unit>()

        val observer = TestObserver<FlightSearchResponse>()
        val resultsResponseReceivedTestSubscriber = TestObserver<Unit>()
        resultsResponseReceived.subscribe(resultsResponseReceivedTestSubscriber)

        val params = FlightSearchParams.Builder(26, 500)
                .legNo(1)
                .selectedLegID("leg-id")
                .origin(dummySuggestionForByot)
                .destination(dummySuggestionForByot)
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
    }

    @Test
    fun testSearchErrorWorks() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))

        val observer = TestObserver<FlightSearchResponse>()
        val departureSuggestion = dummySuggestion
        val suggestion = FlightApiMockResponseGenerator.SuggestionResponseType.SEARCH_ERROR.suggestionString
        departureSuggestion.hierarchyInfo!!.airport!!.airportCode = suggestion
        departureSuggestion.gaiaId = suggestion
        val params = FlightSearchParams.Builder(26, 500)
                .origin(departureSuggestion)
                .destination(dummySuggestion)
                .startDate(LocalDate.now())
                .adults(1)
                .build() as FlightSearchParams

        service!!.flightSearch(params, observer, null)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)

        observer.assertNoErrors()
        observer.assertComplete()
        observer.assertValueCount(1)
        val response = observer.values()[0]
        Assert.assertEquals(0, response.legs.size.toLong())
        Assert.assertEquals(0, response.offers.size.toLong())
    }

    @Test
    fun testCheckoutWorks() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = PublishSubject.create<FlightCheckoutResponse>()

        val testObserver = TestObserver<FlightCheckoutResponse>()
        observer.subscribe(testObserver)

        val params = HashMap<String, Any>()
        params.set("tripId", "happy_round_trip")
        params.set("tealeafTransactionId", "tealeafFlight:happy_round_trip")
        service!!.checkout(params, null, observer)
        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val response = testObserver.values()[0]
        assertEquals("725b4f99-bfea-4bef-bbec-a1fa194350e5", response.newTrip?.tripId)
    }

    @Test
    fun testCheckoutErrorWorks() {
        val root = File("../mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val observer = PublishSubject.create<FlightCheckoutResponse>()

        val testObserver = TestObserver<FlightCheckoutResponse>()
        observer.subscribe(testObserver)

        val params = HashMap<String, Any>()
        params.set("tripId", "checkout_custom_error")
        params.set("tealeafTransactionId", "tealeafFlight:checkout_custom_error")
        service!!.checkout(params, null, observer)
        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val response = testObserver.values()[0]
        assertTrue(response.errors.isNotEmpty())
        assertEquals("Custom error response for checkout. tripId is populated from corresponding createTrip",
                response.errors.get(0).errorInfo.summary)
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

    private val dummySuggestionForByot: SuggestionV4
        get() {
            val dummySuggestion = dummySuggestion
            dummySuggestion.hierarchyInfo!!.airport!!.airportCode = "byot_search"
            return dummySuggestion
        }
}
