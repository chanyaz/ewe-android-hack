package com.expedia.bookings.test

import android.content.DialogInterface
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.FlightTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightOffersViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.mobiata.mocke3.FlightDispatcherUtils
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightOffersViewModelTest {

    var server: MockWebServer = MockWebServer()
        @Rule get

    private lateinit var flightServices: FlightServices
    private lateinit var sut: FlightOffersViewModel
    private lateinit var flightSearchParams: FlightSearchParams

    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        setupFlightServices()
        Ui.getApplication(context).defaultTravelerComponent()
        flightSearchParams = giveSearchParams(false).build()
        sut = FlightOffersViewModel(context, flightServices)
    }

    @Test
    fun testNetworkErrorDialogCancel() {
        val testSubscriber = TestObserver<Unit>()
        val expectedDialogMsg = "Your device is not connected to the internet.  Please check your connection and try again."
        givenFlightSearchThrowsIOException()
        sut = FlightOffersViewModel(context, flightServices)
        sut.noNetworkObservable.subscribe(testSubscriber)

        sut.searchParamsObservable.onNext(flightSearchParams)
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfNoInternetDialog = Shadows.shadowOf(noInternetDialog)
        val cancelBtn = noInternetDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.performClick()

        assertEquals("", shadowOfNoInternetDialog.title)
        assertEquals(expectedDialogMsg, shadowOfNoInternetDialog.message)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testForNoCabinClass() {
        val testSubscriber = TestObserver<String>()
        sut.flightCabinClassSubject.subscribe(testSubscriber)
        performFlightSearch(false)
        testSubscriber.assertNoValues()
    }

    @Test
    fun testWhenNonStopFilterIsNull() {
        val testSubscriber = TestObserver<Boolean>()
        sut.nonStopSearchFilterAppliedSubject.subscribe(testSubscriber)
        performFlightSearch(false)
        assertFalse(testSubscriber.values()[0])
    }

    @Test
    fun testNetworkErrorDialogRetry() {
        val expectedDialogMsg = "Your device is not connected to the internet.  Please check your connection and try again."
        givenFlightSearchThrowsIOException()
        sut = FlightOffersViewModel(context, flightServices)
        sut.searchParamsObservable.onNext(flightSearchParams)
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfNoInternetDialog = Shadows.shadowOf(noInternetDialog)
        val retryBtn = noInternetDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        retryBtn.performClick()
        retryBtn.performClick()

        assertEquals("", shadowOfNoInternetDialog.title)
        assertEquals(expectedDialogMsg, shadowOfNoInternetDialog.message)
        assertEquals(3, (flightServices as TestFlightServiceSearchThrowsException).searchCount) // 1 original, 2 retries
    }

    @Test
    fun testGoodSearchResponse() {
        val testSubscriber = TestObserver<List<FlightLeg>>()

        sut.outboundResultsObservable.subscribe(testSubscriber)
        performFlightSearch(false)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertNotNull(testSubscriber.values()[0])
    }

    @Test
    fun testNetworkErrorForGreedySearch() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsGreedySearchCall)
        val expectedDialogMsg = "Your device is not connected to the internet.  Please check your connection and try again."
        givenFlightSearchThrowsIOException()
        sut = FlightOffersViewModel(context, flightServices)
        sut.greedyFlightSearchObservable.onNext(flightSearchParams)
        //Error Dialog Box should not appear till search button is clicked
        assertTrue(sut.isGreedyCallAborted)
        assertEquals(1, (flightServices as TestFlightServiceSearchThrowsException).searchCount)

        //Error Dialog Box should appear till search button is clicked
        sut.searchParamsObservable.onNext(flightSearchParams)
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfNoInternetDialog = Shadows.shadowOf(noInternetDialog)
        val retryBtn = noInternetDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        retryBtn.performClick()
        retryBtn.performClick()

        assertEquals("", shadowOfNoInternetDialog.title)
        assertEquals(expectedDialogMsg, shadowOfNoInternetDialog.message)
        assertEquals(4, (flightServices as TestFlightServiceSearchThrowsException).searchCount) // 2 original, 2 retries
    }

    @Test
    fun testNoSearchCallIfGreedyIsNotAborted () {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsGreedySearchCall)
        val testSubscriber = TestObserver<List<FlightLeg>>()
        sut = FlightOffersViewModel(context, flightServices)
        sut.outboundResultsObservable.subscribe(testSubscriber)
        performFlightSearch(false)

        testSubscriber.assertValueCount(0)

        sut.cancelGreedySearchObservable.onNext(Unit)
        sut.isGreedyCallAborted = true
        performFlightSearch(false)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testResponseHasError() {
        val observer = getMakeResultsObserver()
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        val flightSearchResponse = FlightSearchResponse()
        val apiError = ApiError(ApiError.Code.FLIGHT_SOLD_OUT)
        flightSearchResponse.errors = listOf(apiError)
        observer.onNext(flightSearchResponse)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(apiError)
    }

    @Test
    fun testResponseHasNoResults() {

        val observer = getMakeResultsObserver()
        val testSubscriber = TestObserver<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        val flightSearchResponse = FlightSearchResponse()
        observer.onNext(flightSearchResponse)

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS, testSubscriber.values()[0].getErrorCode())
    }

    @Test
    fun testShowFlightChargesObFees() {
        val urlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(urlTestSubscriber)

        performFlightSearch(roundTrip = true)

        sut.outboundSelected.onNext(makeFlightLeg("leg1"))
        sut.inboundSelected.onNext(makeFlightLeg("leg0"))

        urlTestSubscriber.assertValue("http://www.expedia.com/api/flight/obFeeCostSummary?langid=1033")
    }

    @Test
    fun testHideFlightChargesObFeesForSelectedOnewayOffer() {
        val urlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(urlTestSubscriber)

        val testSubscriber = TestObserver<String>()
        sut.offerSelectedChargesObFeesSubject.subscribe(testSubscriber)
        performFlightSearch(roundTrip = false)

        val outboundFlightId = makeFlightLeg("leg0")
        sut.confirmedOutboundFlightSelection.onNext(outboundFlightId)

        urlTestSubscriber.assertValue("http://www.expedia.com/api/flight/obFeeCostSummary?langid=1033")
    }

    @Test
    fun testRoundTripFlightOfferSelection() {
        val offerSelectedSubscriber = TestObserver<FlightTripDetails.FlightOffer>()
        val flightProductIdSubscriber = TestObserver<String>()

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
        flightProductIdSubscriber.assertValue(FlightDispatcherUtils.SearchResultsResponseType.HAPPY_ROUND_TRIP.responseName)
    }

    @Test
    fun testOnewWayFlightOfferSelection() {
        val offerSelectedSubscriber = TestObserver<FlightTripDetails.FlightOffer>()
        val flightProductIdSubscriber = TestObserver<String>()

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
        flightProductIdSubscriber.assertValue(FlightDispatcherUtils.SearchResultsResponseType.HAPPY_ONE_WAY.responseName)
    }

    @Test
    fun testFlightOfferFiredBeforeConfirmedSelection() {
        val offerSelectedSubscriber = TestObserver<FlightTripDetails.FlightOffer>()
        sut.flightOfferSelected.subscribe(offerSelectedSubscriber)

        performFlightSearch(roundTrip = true)

        sut.outboundSelected.onNext(makeFlightLeg("leg1"))
        sut.inboundSelected.onNext(makeFlightLeg("leg0"))

        offerSelectedSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        offerSelectedSubscriber.assertValueCount(1)
        assertNotNull(offerSelectedSubscriber.values()[0])
    }

    @Test
    fun testOutboundFlightMap() {
        val testSubscriber = TestObserver<List<FlightLeg>>()
        sut.outboundResultsObservable.subscribe(testSubscriber)

        performFlightSearch(true)
        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val legs = testSubscriber.values()[0]
        assertEquals(5, legs.size)
        assertEquals("leg1", legs[0].legId)
        assertEquals("34fa89938312d0fd8322ee27cb89f8a1", legs[1].legId)
        assertEquals(1, legs[0].packageOfferModel.urgencyMessage.ticketsLeft)
        assertEquals("$696.00", legs[0].packageOfferModel.price.differentialPriceFormatted)
        assertEquals(7, legs[1].packageOfferModel.urgencyMessage.ticketsLeft)
        assertEquals("$800.00", legs[1].packageOfferModel.price.differentialPriceFormatted)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testObFeeDetailsUrl() {
        val urlTestSubscriber = TestObserver<String>()
        sut.obFeeDetailsUrlObservable.subscribe(urlTestSubscriber)
        sut.searchParamsObservable.onNext(flightSearchParams)
        urlTestSubscriber.assertValue("http://www.expedia.com/api/flight/obFeeCostSummary?langid=1033")
    }

    @Test
    fun testInboundFlightMap() {
        val firstOutboundFlightId = "leg1"
        val secondOutboundFlightId = "34fa89938312d0fd8322ee27cb89f8a1"
        val searchSubscriber = TestObserver<List<FlightLeg>>()
        sut.outboundResultsObservable.subscribe(searchSubscriber)

        performFlightSearch(true)
        searchSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)

        val testSubscriber = TestObserver<List<FlightLeg>>()
        sut.inboundResultsObservable.take(2).subscribe(testSubscriber)

        sut.confirmedOutboundFlightSelection.onNext(makeFlightLeg(firstOutboundFlightId))
        sut.confirmedOutboundFlightSelection.onNext(makeFlightLeg(secondOutboundFlightId))

        val outboundFlight1 = FlightLeg()
        outboundFlight1.legId = firstOutboundFlightId

        val outboundFlight2 = FlightLeg()
        outboundFlight2.legId = secondOutboundFlightId

        testSubscriber.awaitValueCount(2, 2, TimeUnit.SECONDS)
        val inboundFlights1 = testSubscriber.values()[0]
        val inboundFlights2 = testSubscriber.values()[1]
        assertEquals(1, inboundFlights1.size)
        assertEquals(1, inboundFlights2.size)
        assertEquals("leg0", inboundFlights1[0].legId)
        assertEquals("0558a569d2c6b1af709befca2e617390", inboundFlights2[0].legId)
        testSubscriber.assertValueCount(2)
    }

    @Test
    fun testOneWayNumberOfTicketsLeft() {
        val ticketsLeftTestObserver = TestObserver.create<Int>()
        sut.ticketsLeftObservable.subscribe(ticketsLeftTestObserver)
        performFlightSearch(false)

        val outboundFlight = FlightTestUtil.getFlightLeg(numberOfTickets = 4)

        sut.confirmedOutboundFlightSelection.onNext(outboundFlight)
        ticketsLeftTestObserver.assertValue(4)
    }

    @Test
    fun testRoundTripNoTicketsValueAfterOnlySelectingOutbound() {
        val ticketsLeftTestObserver = TestObserver.create<Int>()
        sut.ticketsLeftObservable.subscribe(ticketsLeftTestObserver)
        performFlightSearch(true)

        val outboundFlight = FlightTestUtil.getFlightLeg(numberOfTickets = 4)
        sut.confirmedOutboundFlightSelection.onNext(outboundFlight)

        ticketsLeftTestObserver.assertNoValues()
    }

    @Test
    fun testRoundTripMinNumberOfTicketsLeftIsOutbound() {
        val ticketsLeftTestObserver = TestObserver.create<Int>()
        sut.ticketsLeftObservable.subscribe(ticketsLeftTestObserver)
        performFlightSearch(true)

        val outboundFlight = FlightTestUtil.getFlightLeg(numberOfTickets = 4)
        val inboundFlight = FlightTestUtil.getFlightLeg(numberOfTickets = 5)
        sut.confirmedOutboundFlightSelection.onNext(outboundFlight)
        sut.confirmedInboundFlightSelection.onNext(inboundFlight)

        ticketsLeftTestObserver.assertValue(4)
    }

    @Test
    fun testRoundTripMinNumberOfTicketsLeftIsInbound() {
        val ticketsLeftTestObserver = TestObserver.create<Int>()
        sut.ticketsLeftObservable.subscribe(ticketsLeftTestObserver)
        performFlightSearch(true)

        val outboundFlight = FlightTestUtil.getFlightLeg(numberOfTickets = 6)
        val inboundFlight = FlightTestUtil.getFlightLeg(numberOfTickets = 5)
        sut.confirmedOutboundFlightSelection.onNext(outboundFlight)
        sut.confirmedInboundFlightSelection.onNext(inboundFlight)

        ticketsLeftTestObserver.assertValue(5)
    }

    @Test
    @RunForBrands (brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentInInboundLeg() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent, 3)
        val firstOutboundFlightId = "leg1"

        performFlightSearch(true)
        addRichContentToFlightLeg(sut.flightMap[firstOutboundFlightId]!!.toList()[0])

        val testSubscriber = TestObserver<List<FlightLeg>>()
        sut.inboundResultsObservable.subscribe(testSubscriber)
        sut.confirmedOutboundFlightSelection.onNext(makeFlightLeg(firstOutboundFlightId))

        testSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)
        val inboundFlights = testSubscriber.values()[0]
        assertEquals("leg0", inboundFlights[0].legId)
        assertEquals(null, inboundFlights[0].richContent)
    }

    private fun addRichContentToFlightLeg(flightLeg: FlightLeg) {
        flightLeg.richContent = getRichContent()
    }

    private fun getRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = ""
        richContent.score = 7.9F
        richContent.legAmenities = getRichContentAmenities()
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        richContent.segmentAmenitiesList = listOf(getRichContentAmenities())
        return richContent
    }

    private fun getRichContentAmenities(): RichContent.RichContentAmenity {
        val amenities = RichContent.RichContentAmenity()
        amenities.wifi = true
        amenities.entertainment = false
        amenities.power = true
        return amenities
    }

    private fun performFlightSearch(roundTrip: Boolean) {
        val paramsBuilder = giveSearchParams(roundTrip)
        sut.searchParamsObservable.onNext(paramsBuilder.build())
    }

    private fun giveSearchParams(roundTrip: Boolean): FlightSearchParams.Builder {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
        }
        return paramsBuilder
    }

    private fun makeFlightLeg(legId: String): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.legId = legId
        return flightLeg
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
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline())
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

    private fun givenFlightSearchThrowsIOException() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        flightServices = TestFlightServiceSearchThrowsException("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.trampoline(), Schedulers.trampoline())
    }

    private fun getMakeResultsObserver(): Observer<FlightSearchResponse> {
        val makeResultsObserverMethod = sut.javaClass.superclass.getDeclaredMethod("makeResultsObserver")
        makeResultsObserverMethod.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        return makeResultsObserverMethod.invoke(sut) as Observer<FlightSearchResponse>
    }

    class TestFlightServiceSearchThrowsException(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, observeOn: Scheduler, subscribeOn: Scheduler) : FlightServices(endpoint, okHttpClient, listOf(interceptor), observeOn, subscribeOn) {
        var searchCount = 0

        override fun flightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>, resultsResponseReceivedObservable: PublishSubject<Unit>?): Disposable {
            searchCount++
            observer.onError(IOException())
            return Mockito.mock(Disposable::class.java)
        }

        override fun greedyFlightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>, resultsResponseReceivedObservable: PublishSubject<Unit>?): Disposable {
            searchCount++
            observer.onError(IOException())
            return Mockito.mock(Disposable::class.java)
        }
    }
}
