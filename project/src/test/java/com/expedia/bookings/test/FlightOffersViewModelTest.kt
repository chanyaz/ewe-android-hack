package com.expedia.bookings.test

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightOffersViewModel
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
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class FlightOffersViewModelTest {

    private var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit private var flightServices: FlightServices
    lateinit private var sut: FlightOffersViewModel

    private val context = RuntimeEnvironment.application
    private val flightSearchResponseSubject = PublishSubject.create<FlightSearchResponse>()
    private val isRoundTripSearchSubject = BehaviorSubject.create<Boolean>()

    @Before
    fun setup() {
        setupFlightServices()
        Ui.getApplication(context).defaultTravelerComponent()
        sut = FlightOffersViewModel(context, flightSearchResponseSubject, isRoundTripSearchSubject)
    }

    @Test
    fun testShowFlightChargesObFees() {
        val showObChargesTestSubscriber = TestSubscriber<Boolean>()
        val urlTestSubscriber = TestSubscriber<String>()
        sut.showChargesObFeesSubject.subscribe(showObChargesTestSubscriber)
        sut.obFeeDetailsUrlObservable.subscribe(urlTestSubscriber)

        performFlightSearch(roundTrip = true)

        sut.outboundSelected.onNext(makeFlightLegWithOBFees("leg0"))
        sut.inboundSelected.onNext(makeFlightLegWithOBFees("leg1"))

        showObChargesTestSubscriber.assertValues(true, true)
        urlTestSubscriber.assertValue("http://www.expedia.com/api/flight/obFeeCostSummary?langid=1033")
    }

    @Test
    fun testFlightChargesObFeesText() {
        configurePointOfSaleAirlinesChargeAdditionalFees()

        val testSubscriber = TestSubscriber<String>()
        sut.offerSelectedChargesObFeesSubject.subscribe(testSubscriber)

        sut.showChargesObFeesSubject.onNext(true)

        testSubscriber.assertValue("Airline fee applies based on payment method")
    }

    @Test
    fun testFlightChargesObFeesPosNotAirlineSpecific() {
        val testSubscriber = TestSubscriber<String>()
        sut.offerSelectedChargesObFeesSubject.subscribe(testSubscriber)

        sut.showChargesObFeesSubject.onNext(true)

        testSubscriber.assertValue("Payment fees may apply")
    }

    @Test
    fun testRoundTripFlightOfferSelection() {
        val offerSelectedSubscriber = TestSubscriber<FlightTripDetails.FlightOffer>()
        val flightProductIdSubscriber = TestSubscriber<String>()

        isRoundTripSearchSubject.onNext(true)
        performFlightSearch(roundTrip = true)

        sut.flightOfferSelected.subscribe(offerSelectedSubscriber)
        sut.flightProductId.subscribe(flightProductIdSubscriber)

        val outboundFlightId = FlightLeg()
        outboundFlightId.legId = "leg1"
        sut.confirmedOutboundFlightSelection.onNext(outboundFlightId)

        offerSelectedSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        offerSelectedSubscriber.assertNoValues()

        val inboundFlightId = FlightLeg()
        inboundFlightId.legId = "leg0"
        sut.confirmedInboundFlightSelection.onNext(inboundFlightId)

        offerSelectedSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        offerSelectedSubscriber.assertValueCount(1)

        flightProductIdSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        flightProductIdSubscriber.assertValueCount(1)
        flightProductIdSubscriber.assertValue("happy_roundtrip_0")
    }

    @Test
    fun testOnewWayFlightOfferSelection() {
        val offerSelectedSubscriber = TestSubscriber<FlightTripDetails.FlightOffer>()
        val flightProductIdSubscriber = TestSubscriber<String>()

        isRoundTripSearchSubject.onNext(false)
        performFlightSearch(roundTrip = false)

        sut.flightOfferSelected.subscribe(offerSelectedSubscriber)
        sut.flightProductId.subscribe(flightProductIdSubscriber)

        val outboundFlightId = FlightLeg()
        outboundFlightId.legId = "leg0"
        sut.confirmedOutboundFlightSelection.onNext(outboundFlightId)

        offerSelectedSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        offerSelectedSubscriber.assertValueCount(1)

        flightProductIdSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        flightProductIdSubscriber.assertValueCount(1)
        flightProductIdSubscriber.assertValue("happy_oneway_0")
    }

    @Test
    fun testFlightOfferFiredBeforeConfirmedSelection() {
        val offerSelectedSubscriber = TestSubscriber<FlightTripDetails.FlightOffer>()
        sut.flightOfferSelected.subscribe(offerSelectedSubscriber)

        performFlightSearch(roundTrip = true)

        sut.outboundSelected.onNext(makeFlightLegWithOBFees("leg1"))
        sut.inboundSelected.onNext(makeFlightLegWithOBFees("leg0"))

        offerSelectedSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        offerSelectedSubscriber.assertValueCount(1)
        assertNotNull(offerSelectedSubscriber.onNextEvents[0])
    }

    @Test
    fun testOutboundFlightMap(){
        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.outboundResultsObservable.subscribe(testSubscriber)

        performFlightSearch(true)
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
        val firstOutboundFlightId = "leg1"
        val secondOutboundFlightId = "34fa89938312d0fd8322ee27cb89f8a1"
        val searchSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.outboundResultsObservable.subscribe(searchSubscriber)

        performFlightSearch(true)
        searchSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)

        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.inboundResultsObservable.take(2).subscribe(testSubscriber)

        sut.confirmedOutboundFlightSelection.onNext(makeFlightLegWithOBFees(firstOutboundFlightId))
        sut.confirmedOutboundFlightSelection.onNext(makeFlightLegWithOBFees(secondOutboundFlightId))

        val outboundFlight1 = FlightLeg()
        outboundFlight1.legId = firstOutboundFlightId

        val outboundFlight2 = FlightLeg()
        outboundFlight2.legId = secondOutboundFlightId

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        val inboundFlights1 = testSubscriber.onNextEvents[0]
        val inboundFlights2 = testSubscriber.onNextEvents[1]
        assertEquals(1, inboundFlights1.size)
        assertEquals(1, inboundFlights2.size)
        assertEquals("leg0", inboundFlights1[0].legId)
        assertEquals("0558a569d2c6b1af709befca2e617390", inboundFlights2[0].legId)
        testSubscriber.assertValueCount(2)
    }

    private fun configurePointOfSaleAirlinesChargeAdditionalFees() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airlines_charge_additional_fees.json")
    }

    private fun performFlightSearch(roundTrip: Boolean) {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(20, 20)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
        }
        flightServices.flightSearch(paramsBuilder.build(), flightSearchResponseSubject)
    }

    private fun makeFlightLegWithOBFees(legId: String): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.legId = legId
        flightLeg.mayChargeObFees = true
        return flightLeg
    }

    private fun setupFlightServices() {
        val logger = HttpLoggingInterceptor()
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        flightServices = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        server.setDispatcher(ExpediaDispatcher(opener))
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
