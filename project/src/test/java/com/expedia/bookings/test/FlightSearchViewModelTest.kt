package com.expedia.bookings.test

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.FlightSearchViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
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
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightSearchViewModelTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    var service: FlightServices by Delegates.notNull()

    var vm: FlightSearchViewModel by Delegates.notNull()

    @Before
    fun before(){
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        val interceptor = MockInterceptor()
        service = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        val context = RuntimeEnvironment.application
        vm = FlightSearchViewModel(context, service)
    }

    @Test
    fun testOutboundFlightMap(){
        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        vm.outboundResultsObservable.subscribe(testSubscriber)

        doFlightSearch()
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        val legs = testSubscriber.onNextEvents[0]
        assertEquals(2, legs.size)
        assertEquals("leg1", legs[0].legId)
        assertEquals("34fa89938312d0fd8322ee27cb89f8a1", legs[1].legId)
        assertEquals(1, legs[0].packageOfferModel.urgencyMessage.ticketsLeft)
        assertEquals("$696.00", legs[0].packageOfferModel.price.differentialPriceFormatted)
        assertEquals(7, legs[1].packageOfferModel.urgencyMessage.ticketsLeft)
        assertEquals("$800.00", legs[1].packageOfferModel.price.differentialPriceFormatted)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testInboundFlightMap() {
        val searchSubscriber = TestSubscriber<List<FlightLeg>>()
        vm.outboundResultsObservable.subscribe(searchSubscriber)

        doFlightSearch()
        searchSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)

        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        vm.inboundResultsObservable.take(2).subscribe(testSubscriber)

        val outboundFlight1 = FlightLeg()
        outboundFlight1.legId = "leg1"

        val outboundFlight2 = FlightLeg()
        outboundFlight2.legId = "34fa89938312d0fd8322ee27cb89f8a1"

        vm.confirmedOutboundFlightSelection.onNext(outboundFlight1)
        vm.confirmedOutboundFlightSelection.onNext(outboundFlight2)

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        val inboundFlights1 = testSubscriber.onNextEvents[0]
        val inboundFlights2 = testSubscriber.onNextEvents[1]
        assertEquals(1, inboundFlights1.size)
        assertEquals(1, inboundFlights2.size)
        assertEquals("leg0", inboundFlights1[0].legId)
        assertEquals("0558a569d2c6b1af709befca2e617390", inboundFlights2[0].legId)
        testSubscriber.assertValueCount(2)
    }

    private fun doFlightSearch() {
        val params = FlightSearchParams.Builder(26, 500)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams
        vm.searchParamsObservable.onNext(params)
    }

    private fun getDummySuggestion(): SuggestionV4 {
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
