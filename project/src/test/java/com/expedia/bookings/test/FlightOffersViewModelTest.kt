package com.expedia.bookings.test

import android.content.DialogInterface
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.FlightOffersViewModel
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.mobiata.mocke3.FlightApiMockResponseGenerator
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
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class FlightOffersViewModelTest {

    private var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit private var flightServices: FlightServices
    lateinit private var sut: FlightOffersViewModel
    lateinit private var flightSearchParams: FlightSearchParams

    private val context = RuntimeEnvironment.application
    private val isRoundTripSearchSubject = BehaviorSubject.create<Boolean>()

    @Before
    fun setup() {
        setupFlightServices()
        Ui.getApplication(context).defaultTravelerComponent()
        flightSearchParams = giveSearchParams(false).build()
        sut = FlightOffersViewModel(context, flightServices)
    }

    @Test
    fun testNetworkErrorDialogCancel() {
        val testSubscriber = TestSubscriber<Unit>()
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
        val testSubscriber = TestSubscriber<String>()
        sut.flightCabinClassSubject.subscribe(testSubscriber)
        performFlightSearch(false)
        testSubscriber.assertNoValues()
    }

    @Test
    fun testWhenNonStopFilterIsNull() {
        val testSubscriber = TestSubscriber<Boolean>()
        sut.nonStopSearchFilterAppliedSubject.subscribe(testSubscriber)
        performFlightSearch(false)
        assertFalse(testSubscriber.onNextEvents[0])
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
        val testSubscriber = TestSubscriber<List<FlightLeg>>()

        sut.outboundResultsObservable.subscribe(testSubscriber)
        performFlightSearch(false)

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertNotNull(testSubscriber.onNextEvents[0])
    }

    @Test
    fun testBookableCachedSearchResponse() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSearchResultCaching)
        val resultsTestSubscriber = TestSubscriber<List<FlightLeg>>()
        val cachedCallCompleteTestSubscriber = TestSubscriber<Boolean>()
        val cachedSearchTrackingTestSubscriber = TestSubscriber<String>()
        val cancelSearchTestSubscriber = TestSubscriber<Unit>()

        sut.outboundResultsObservable.subscribe(resultsTestSubscriber)
        sut.isCachedCallCompleted.subscribe(cachedCallCompleteTestSubscriber)
        sut.cachedSearchTrackingString.subscribe(cachedSearchTrackingTestSubscriber)
        sut.cancelOutboundSearchObservable.subscribe(cancelSearchTestSubscriber)

        performCachedFlightSearch(false, "cached_bookable")

        resultsTestSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        resultsTestSubscriber.assertValueCount(1)
        assertNotNull(resultsTestSubscriber.onNextEvents[0])
        cachedCallCompleteTestSubscriber.assertValueCount(2)
        assertEquals("B", cachedSearchTrackingTestSubscriber.onNextEvents[0])
        cancelSearchTestSubscriber.assertValueCount(1)
    }

    @Test
    fun testNonBookableCachedSearchResponse() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSearchResultCaching)
        val resultsTestSubscriber = TestSubscriber<List<FlightLeg>>()
        val cachedCallCompleteTestSubscriber = TestSubscriber<Boolean>()
        val cachedSearchTrackingTestSubscriber = TestSubscriber<String>()
        val cancelSearchTestSubscriber = TestSubscriber<Unit>()

        sut.outboundResultsObservable.subscribe(resultsTestSubscriber)
        sut.isCachedCallCompleted.subscribe(cachedCallCompleteTestSubscriber)
        sut.cachedSearchTrackingString.subscribe(cachedSearchTrackingTestSubscriber)
        sut.cancelOutboundSearchObservable.subscribe(cancelSearchTestSubscriber)

        performCachedFlightSearch(false, "cached_non_bookable")

        // Non bookable cached search response should not be rendered for now.
        resultsTestSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        resultsTestSubscriber.assertValueCount(0)
        cachedCallCompleteTestSubscriber.assertValueCount(2)
        assertEquals("NB", cachedSearchTrackingTestSubscriber.onNextEvents[0])
        cancelSearchTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testCachedSearchNoResultsResponse() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsSearchResultCaching)
        val resultsTestSubscriber = TestSubscriber<List<FlightLeg>>()
        val cachedCallCompleteTestSubscriber = TestSubscriber<Boolean>()
        val cachedSearchTrackingTestSubscriber = TestSubscriber<String>()
        val cancelSearchTestSubscriber = TestSubscriber<Unit>()

        sut.outboundResultsObservable.subscribe(resultsTestSubscriber)
        sut.isCachedCallCompleted.subscribe(cachedCallCompleteTestSubscriber)
        sut.cachedSearchTrackingString.subscribe(cachedSearchTrackingTestSubscriber)
        sut.cancelOutboundSearchObservable.subscribe(cancelSearchTestSubscriber)

        performCachedFlightSearch(false, "cached_not_found")

        // Cached results were not found.
        resultsTestSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        resultsTestSubscriber.assertValueCount(0)
        cachedCallCompleteTestSubscriber.assertValueCount(2)
        assertEquals("CN", cachedSearchTrackingTestSubscriber.onNextEvents[0])
        cancelSearchTestSubscriber.assertValueCount(0)
    }

    @Test
    fun testResponseHasError() {
        val observer = getMakeResultsObserver()
        val testSubscriber = TestSubscriber<ApiError>()
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
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        val flightSearchResponse = FlightSearchResponse()
        observer.onNext(flightSearchResponse)

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
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

        testSubscriber.assertValue("Payment Fees May Apply")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
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
        flightProductIdSubscriber.assertValue(FlightApiMockResponseGenerator.SearchResultsResponseType.HAPPY_ROUND_TRIP.responseName)
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
        flightProductIdSubscriber.assertValue(FlightApiMockResponseGenerator.SearchResultsResponseType.HAPPY_ONE_WAY.responseName)
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
        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val legs = testSubscriber.onNextEvents[0]
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
        sut.searchParamsObservable.onNext(flightSearchParams)
        assertEquals(sut.obFeeDetailsUrlObservable.value, "http://www.expedia.com/api/flight/obFeeCostSummary?langid=1033")
    }

    @Test
    fun testInboundFlightMap() {
        val firstOutboundFlightId = "leg1"
        val secondOutboundFlightId = "34fa89938312d0fd8322ee27cb89f8a1"
        val searchSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.outboundResultsObservable.subscribe(searchSubscriber)

        performFlightSearch(true)
        searchSubscriber.awaitValueCount(1, 2, TimeUnit.SECONDS)

        val testSubscriber = TestSubscriber<List<FlightLeg>>()
        sut.inboundResultsObservable.take(2).subscribe(testSubscriber)

        sut.confirmedOutboundFlightSelection.onNext(makeFlightLegWithOBFees(firstOutboundFlightId))
        sut.confirmedOutboundFlightSelection.onNext(makeFlightLegWithOBFees(secondOutboundFlightId))

        val outboundFlight1 = FlightLeg()
        outboundFlight1.legId = firstOutboundFlightId

        val outboundFlight2 = FlightLeg()
        outboundFlight2.legId = secondOutboundFlightId

        testSubscriber.awaitValueCount(2, 2, TimeUnit.SECONDS)
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
        val paramsBuilder = giveSearchParams(roundTrip)
        sut.searchParamsObservable.onNext(paramsBuilder.build())
    }

    private fun performCachedFlightSearch(roundTrip: Boolean, airportCode: String) {
        val paramsBuilder = giveCachedSearchParams(roundTrip, airportCode)
        sut.cachedFlightSearchObservable.onNext(paramsBuilder.build())
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

    private fun giveCachedSearchParams(roundTrip: Boolean, airportCode: String): FlightSearchParams.Builder {
        val origin = getDummySuggestion()
        origin.hierarchyInfo!!.airport!!.airportCode = airportCode
        return giveSearchParams(roundTrip)
                .setFeatureOverride(Constants.FEATURE_FLIGHT_CACHE)
                .origin(origin) as FlightSearchParams.Builder
    }

    private fun makeFlightLegWithOBFees(legId: String): FlightLeg {
        val flightLeg = FlightLeg()
        flightLeg.legId = legId
        flightLeg.mayChargeObFees = true
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
                interceptor, Schedulers.immediate(), Schedulers.immediate())
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
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    private fun getMakeResultsObserver(): Observer<FlightSearchResponse> {
        val makeResultsObserverMethod = sut.javaClass.superclass.getDeclaredMethod("makeResultsObserver")
        makeResultsObserverMethod.isAccessible = true
        return makeResultsObserverMethod.invoke(sut) as Observer<FlightSearchResponse>
    }

    class TestFlightServiceSearchThrowsException(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, observeOn: Scheduler, subscribeOn: Scheduler) : FlightServices(endpoint, okHttpClient, interceptor, observeOn, subscribeOn) {
        var searchCount = 0

        override fun flightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>, resultsResponseReceivedObservable: PublishSubject<Unit>?): Subscription {
            searchCount++
            observer.onError(IOException())
            return Mockito.mock(Subscription::class.java)
        }
    }

}
