package com.expedia.bookings.test

import android.content.DialogInterface
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightSearchViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
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
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightSearchViewModelTest {

    private val LOTS_MORE: Long = 100
    private val context = RuntimeEnvironment.application

    var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit private var service: FlightServices
    lateinit private var sut: FlightSearchViewModel
    lateinit private var flightSearchParams: FlightSearchParams

    @Test
    fun testNetworkErrorDialogCancel() {
        val testSubscriber = TestSubscriber<Unit>()
        val expectedDialogMsg = "Your device is not connected to the internet.  Please check your connection and try again."

        givenDefaultTravelerComponent()
        givenGoodSearchParams()
        givenFlightSearchThrowsIOException()
        createSystemUnderTest()
        sut.noNetworkObservable.subscribe(testSubscriber)

        doFlightSearch()
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfNoInternetDialog = Shadows.shadowOf(noInternetDialog)
        val cancelBtn = noInternetDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.performClick()

        assertEquals("", shadowOfNoInternetDialog.title)
        assertEquals(expectedDialogMsg, shadowOfNoInternetDialog.message)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testNetworkErrorDialogRetry() {
        val expectedDialogMsg = "Your device is not connected to the internet.  Please check your connection and try again."

        givenDefaultTravelerComponent()
        givenGoodSearchParams()
        givenFlightSearchThrowsIOException()
        createSystemUnderTest()

        doFlightSearch()
        val noInternetDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfNoInternetDialog = Shadows.shadowOf(noInternetDialog)
        val retryBtn = noInternetDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        retryBtn.performClick()
        retryBtn.performClick()

        assertEquals("", shadowOfNoInternetDialog.title)
        assertEquals(expectedDialogMsg, shadowOfNoInternetDialog.message)
        assertEquals(3, (service as TestFlightServiceSearchThrowsException).searchCount) // 1 original, 2 retries
    }

    @Test
    fun testFlightSearchDatesOnTabChanges() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToMMMd(endDate)

        sut.datesObserver.onNext(Pair(startDate, endDate))
        assertEquals(null, sut.cachedEndDateObservable.value)
        assertEquals("$expectedStartDate - $expectedEndDate", sut.dateTextObservable.value)

        sut.isRoundTripSearchObservable.onNext(false)
        assertEquals(endDate, sut.cachedEndDateObservable.value)
        assertEquals("$expectedStartDate (One Way)", sut.dateTextObservable.value)

        val newStartDate = LocalDate.now().plusDays(20)
        val expectedNewStartDate = DateUtils.localDateToMMMd(newStartDate)

        sut.datesObserver.onNext(Pair(newStartDate, null))
        sut.isRoundTripSearchObservable.onNext(true)
        assertEquals(null, sut.cachedEndDateObservable.value)
        assertEquals("$expectedNewStartDate – Select return date", sut.dateTextObservable.value)

        sut.datesObserver.onNext(Pair(null, null))
        assertEquals("Select Dates", sut.dateTextObservable.value)

        sut.isRoundTripSearchObservable.onNext(false)
        assertEquals("Select departure date", sut.dateTextObservable.value)
    }

    @Test
    fun testParamsMissingDestination() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testPerformSearchSubscriber = TestSubscriber<Unit>()
        val testSearchButtonSubscriber = TestSubscriber<Boolean>()

        sut.searchButtonObservable.subscribe(testSearchButtonSubscriber)

        sut.errorNoDestinationObservable.subscribe(testPerformSearchSubscriber)
        givenParamsHaveOrigin()
        givenParamsHaveDates(LocalDate.now(), LocalDate.now().plusDays(1))

        sut.performSearchObserver.onNext(Unit)
        testPerformSearchSubscriber.assertValueCount(1)

        testSearchButtonSubscriber.assertValue(false)
    }

    @Test
    fun testParamsMissingOrigin() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testSubscriber = TestSubscriber<Unit>()
        sut.errorNoOriginObservable.subscribe(testSubscriber)
        sut.performSearchObserver.onNext(Unit)

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testParamsMissingValidDates() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testSubscriber = TestSubscriber<Unit>()
        sut.errorNoDatesObservable.subscribe(testSubscriber)
        givenParamsHaveOrigin()
        givenParamsHaveDestination()

        sut.performSearchObserver.onNext(Unit)

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testParamsOriginMatchingDestination() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testSubscriber = TestSubscriber<String>()
        sut.errorOriginSameAsDestinationObservable.subscribe(testSubscriber)

        val origin = SuggestionV4()
        sut.getParamsBuilder().origin(origin)
        sut.getParamsBuilder().destination(origin)
        givenValidStartAndEndDates()

        sut.performSearchObserver.onNext(Unit)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Departure and arrival airports must be different.")
    }

    @Test
    fun testParamsDatesExceedMaxStay() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testSubscriber = TestSubscriber<String>()
        sut.errorMaxDurationObservable.subscribe(testSubscriber)

        val startDate = LocalDate()
        val endDate = startDate.plusDays(sut.getMaxSearchDurationDays() + 1)

        givenParamsHaveOrigin()
        givenParamsHaveDestination()
        givenParamsHaveDates(startDate, endDate)

        sut.performSearchObserver.onNext(Unit)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("We're sorry, but we are unable to search for hotel stays longer than 330 days.")
    }

    @Test
    fun testGoodSearchResponse() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testSubscriber = TestSubscriber<FlightSearchResponse>()

        sut.flightSearchResponseSubject.subscribe(testSubscriber)
        sut.searchParamsObservable.onNext(makeSearchParams())

        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertNotNull(testSubscriber.onNextEvents[0])
    }

    @Test
    fun testResponseHasError() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

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
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val observer = getMakeResultsObserver()
        val testSubscriber = TestSubscriber<ApiError>()
        sut.errorObservable.subscribe(testSubscriber)

        val flightSearchResponse = FlightSearchResponse()
        observer.onNext(flightSearchResponse)

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    fun testFlightSearchEnabled() {
        givenDefaultTravelerComponent()
        givenMockServer()
        createSystemUnderTest()

        makeSearchParams()
        sut.isRoundTripSearchObservable.onNext(false)
        sut.getParamsBuilder()
                .startDate(LocalDate.now())
                .adults(1)
                .build() as FlightSearchParams

        assertEquals(true, sut.getParamsBuilder().areRequiredParamsFilled())

        sut.isRoundTripSearchObservable.onNext(true)
        assertEquals(false, sut.getParamsBuilder().areRequiredParamsFilled())

        sut.getParamsBuilder().endDate(LocalDate.now().plusDays(4))
        assertEquals(true, sut.getParamsBuilder().areRequiredParamsFilled())
    }

    @Test
    fun testRoundTripMissingReturnDate() {
        givenDefaultTravelerComponent()
        givenMockServer()
        createSystemUnderTest()

        val origin = getDummySuggestion()
        origin.hierarchyInfo?.airport?.airportCode = "SFO"
        val destination = getDummySuggestion()
        destination.hierarchyInfo?.airport?.airportCode = "LAS"

        val testSubscriber = TestSubscriber<Unit>()
        sut.errorNoDatesObservable.subscribe(testSubscriber)
        sut.isRoundTripSearchObservable.onNext(true)
        sut.searchParamsObservable.onNext(makeSearchParams())
        sut.performSearchObserver.onNext(Unit)
        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.assertValueCount(1)
        assertNotNull(testSubscriber.onNextEvents[0])
    }

    @Test
    fun testSameStartAndEndDateAllowed() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        assertTrue(sut.sameStartAndEndDateAllowed(), "Same day return flights are allowed")
    }

    @Test
    fun testClearDestinationLocation() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val testSubscriber = TestSubscriber<String>()
        val destination = SuggestionV4()
        destination.regionNames = SuggestionV4.RegionNames()
        destination.regionNames.displayName = ""
        sut.formattedDestinationObservable.subscribe(testSubscriber)
        sut.destinationLocationObserver.onNext(destination)

        sut.clearDestinationLocation()

        testSubscriber.assertValueCount(2)
        testSubscriber.assertValues("", "")
    }

    @Test
    fun testInfantInLapObserver() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        val startDate = LocalDate()
        val endDate = startDate.plusDays(3)
        givenParamsHaveDates(startDate, endDate)

        sut.isInfantInLapObserver.onNext(true)
        assertTrue(sut.getParamsBuilder().build().infantSeatingInLap)

        sut.isInfantInLapObserver.onNext(false)
        assertFalse(sut.getParamsBuilder().build().infantSeatingInLap)
    }

    private fun getMakeResultsObserver(): Observer<FlightSearchResponse> {
        val makeResultsObserverMethod = sut.javaClass.getDeclaredMethod("makeResultsObserver")
        makeResultsObserverMethod.isAccessible = true
        return makeResultsObserverMethod.invoke(sut) as Observer<FlightSearchResponse>
    }

    private fun givenValidStartAndEndDates() {
        val startDate = LocalDate()
        val endDate = LocalDate()
        sut.getParamsBuilder()
                .startDate(startDate)
                .endDate(endDate)
    }

    private fun givenParamsHaveDates(startDate: LocalDate, endDate: LocalDate) {
        sut.getParamsBuilder()
                .startDate(startDate)
                .endDate(endDate)
    }

    private fun givenParamsHaveOrigin() {
        val origin = SuggestionV4()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "SFO"
        origin.hierarchyInfo = SuggestionV4.HierarchyInfo()
        origin.hierarchyInfo?.airport = airport
        origin.regionNames = SuggestionV4.RegionNames()
        origin.regionNames.displayName = "SFO"
        sut.originLocationObserver.onNext(origin)
    }

    private fun givenParamsHaveDestination() {
        val destination = SuggestionV4()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "LHR"
        destination.hierarchyInfo = SuggestionV4.HierarchyInfo()
        destination.hierarchyInfo?.airport = airport
        destination.regionNames = SuggestionV4.RegionNames()
        destination.regionNames.displayName = "LHR"
        sut.destinationLocationObserver.onNext(destination)
    }

    private fun makeSearchParams(): FlightSearchParams {
        val origin = getDummySuggestion()
        origin.hierarchyInfo?.airport?.airportCode = "SFO"
        val destination = getDummySuggestion()
        destination.hierarchyInfo?.airport?.airportCode = "LAS"

        return sut.getParamsBuilder()
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now())
                .adults(1)
                .build() as FlightSearchParams
    }

    private fun givenGoodSearchParams() {
        flightSearchParams = FlightSearchParams.Builder(26, 500)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .adults(1)
                .build() as FlightSearchParams
    }

    private fun doFlightSearch() {
        sut.searchParamsObservable.onNext(flightSearchParams)
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

    private fun givenDefaultTravelerComponent() {
        Ui.getApplication(context).defaultTravelerComponent()
    }

    private fun createSystemUnderTest() {
        sut = FlightSearchViewModel(context, service)
    }

    class TestFlightServiceSearchThrowsException(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, observeOn: Scheduler, subscribeOn: Scheduler) : FlightServices(endpoint, okHttpClient, interceptor, observeOn, subscribeOn) {
        var searchCount = 0

        override fun flightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>): Subscription? {
            searchCount++
            observer.onError(IOException())
            return null
        }
    }

    private fun givenFlightSearchThrowsIOException() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = TestFlightServiceSearchThrowsException("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }

    private fun givenMockServer() {
        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                interceptor, Schedulers.immediate(), Schedulers.immediate())
    }
}
