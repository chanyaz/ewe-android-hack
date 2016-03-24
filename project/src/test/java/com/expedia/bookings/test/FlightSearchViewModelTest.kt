package com.expedia.bookings.test

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.vm.FlightSearchViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit.RestAdapter
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

class FlightSearchViewModelTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    var service: FlightServices by Delegates.notNull()

    var vm: FlightSearchViewModel by Delegates.notNull()

    @Before
    fun before(){
        service = FlightServices("http://localhost:" + server.port,
                OkHttpClient(), MockInterceptor(),
                Schedulers.immediate(), Schedulers.immediate(),
                RestAdapter.LogLevel.FULL)
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
        vm = FlightSearchViewModel(service)
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

        vm.outboundFlightSelected.onNext(outboundFlight1)
        vm.outboundFlightSelected.onNext(outboundFlight2)

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
        val params = FlightSearchParams.Builder(26)
                .departure(getDummySuggestion())
                .arrival(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams
        vm.flightParamsObservable.onNext(params)
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
