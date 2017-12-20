package com.expedia.bookings.test

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightOffersViewModelByot
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.mobiata.mocke3.FlightApiMockResponseGenerator
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class FlightOffersViewModelByotTest {

    var server: MockWebServer = MockWebServer()
        @Rule get
    lateinit private var flightServices: FlightServices
    lateinit private var sut: FlightOffersViewModelByot
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        setupFlightServices()
        Ui.getApplication(context).defaultTravelerComponent()
        sut = FlightOffersViewModelByot(context, flightServices)
    }

    @Test
    fun testGoodOutboundSearchResponse() {
        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.isOutboundSearch = true
        sut.outboundResultsObservable.subscribe(testSubscriber)
        performFlightSearch(false)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        val response = testSubscriber.onNextEvents[0]
        assertNotNull(response)
        assertEquals(response.size, 3)
    }

    @Test
    fun testGoodInboundSearchResponse() {
        val flightLeg = FlightLeg()
        flightLeg.legId = "leg-Id"
        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.isOutboundSearch = false
        sut.inboundResultsObservable.subscribe(testSubscriber)
        sut.outboundSelected.onNext(flightLeg)
        performFlightSearch(true)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        val response = testSubscriber.onNextEvents[0]
        assertNotNull(response)
        assertEquals(response.size, 4)
    }

    @Test
    fun testRoundTripFlightOfferSelection() {
        val offerSelectedSubscriber = TestSubscriber<FlightTripDetails.FlightOffer>()
        val flightProductIdSubscriber = TestSubscriber<String>()
        val outboundResultTestSubscriber = TestSubscriber<List<FlightLeg>>()
        val inboundResultTestSubscriber = TestSubscriber<List<FlightLeg>>()

        sut.outboundResultsObservable.subscribe(outboundResultTestSubscriber)
        sut.inboundResultsObservable.subscribe(inboundResultTestSubscriber)
        sut.flightOfferSelected.subscribe(offerSelectedSubscriber)
        sut.flightProductId.subscribe(flightProductIdSubscriber)

        //Outbound search will be fired
        performFlightSearch(false)
        outboundResultTestSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        outboundResultTestSubscriber.assertValueCount(1)

        val outboundFlightId = FlightLeg()
        outboundFlightId.legId = "leg0"
        sut.outboundSelected.onNext(outboundFlightId)

        //After outbound selection Inbound search will be fired
        sut.confirmedOutboundFlightSelection.onNext(outboundFlightId)
        inboundResultTestSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        inboundResultTestSubscriber.assertValueCount(1)

        //selection of inbound leg
        val inboundFlightId = FlightLeg()
        inboundFlightId.legId = "leg1"
        sut.confirmedInboundFlightSelection.onNext(inboundFlightId)
        offerSelectedSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        offerSelectedSubscriber.assertValueCount(1)

        flightProductIdSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        flightProductIdSubscriber.assertValueCount(1)
        flightProductIdSubscriber.assertValue(FlightApiMockResponseGenerator.SearchResultsResponseType.HAPPY_ROUND_TRIP.responseName)
    }

    private fun performFlightSearch(isInbound: Boolean) {
        val paramsBuilder = giveSearchParams(isInbound)
        sut.searchParamsObservable.onNext(paramsBuilder.build())
        Db.setFlightSearchParams(paramsBuilder.build())
    }

    private fun giveSearchParams(isInbound: Boolean): FlightSearchParams.Builder {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500).legNo(0)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .endDate(endDate)
                .adults(1) as FlightSearchParams.Builder

        if (isInbound) {
            paramsBuilder.legNo(1).selectedLegID("leg-Id")
        }
        return paramsBuilder
    }

    private fun setupFlightServices() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        flightServices = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "byot_search"
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = "byot_search"
        return suggestion
    }
}
