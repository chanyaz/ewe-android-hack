package com.expedia.bookings.test

import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FlightSearchParamsHistoryUtil
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightSearchViewModel
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(RobolectricRunner::class)
class FlightSearchViewModelTest {

    private val LOTS_MORE: Long = 100
    private val context = RuntimeEnvironment.application

    var server: MockWebServer = MockWebServer()
        @Rule get

    lateinit private var service: FlightServices
    lateinit private var sut: FlightSearchViewModel

    @Test
    fun testFlightSearchDatesOnTabChanges() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        val expectedStartDate = DateUtils.localDateToMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToMMMd(endDate)

        sut.datesUpdated(startDate, endDate)
        assertEquals(null, sut.cachedEndDateObservable.value)
        assertEquals("$expectedStartDate - $expectedEndDate", sut.dateTextObservable.value)

        sut.isRoundTripSearchObservable.onNext(false)
        assertEquals(endDate, sut.cachedEndDateObservable.value)
        assertEquals("$expectedStartDate (One Way)", sut.dateTextObservable.value)

        val newStartDate = LocalDate.now().plusDays(20)
        val expectedNewStartDate = DateUtils.localDateToMMMd(newStartDate)

        sut.datesUpdated(newStartDate, null)
        sut.isRoundTripSearchObservable.onNext(true)
        assertEquals(null, sut.cachedEndDateObservable.value)
        assertEquals("$expectedNewStartDate – Select return date", sut.dateTextObservable.value)

        sut.datesUpdated(null, null)
        assertEquals("Select Dates", sut.dateTextObservable.value)

        sut.isRoundTripSearchObservable.onNext(false)
        assertEquals("Select departure date", sut.dateTextObservable.value)
    }

    @Test
    fun testFlightSearchDayWithDateAbacusTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightDayPlusDateSearchForm)
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        val startDate = LocalDate.now().plusDays(3)
        val endDate = LocalDate.now().plusDays(8)
        val expectedStartDate = DateUtils.localDateToEEEMMMd(startDate)
        val expectedEndDate = DateUtils.localDateToEEEMMMd(endDate)

        sut.datesUpdated(startDate, endDate)
        assertEquals(null, sut.cachedEndDateObservable.value)
        assertEquals("$expectedStartDate  -  $expectedEndDate", sut.dateTextObservable.value)

        sut.isRoundTripSearchObservable.onNext(false)
        assertEquals(endDate, sut.cachedEndDateObservable.value)
        assertEquals("$expectedStartDate (One Way)", sut.dateTextObservable.value)

        val newStartDate = LocalDate.now().plusDays(20)
        val expectedNewStartDate = DateUtils.localDateToEEEMMMd(newStartDate)

        sut.datesUpdated(newStartDate, null)
        sut.isRoundTripSearchObservable.onNext(true)
        assertEquals(null, sut.cachedEndDateObservable.value)
        assertEquals("$expectedNewStartDate – Select return date", sut.dateTextObservable.value)

        sut.datesUpdated(null, null)
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
    fun testStartDate() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        assertEquals(LocalDate.now() ,sut.getFirstAvailableDate(), "Start Date is Today")
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

    @Test
    fun testFlightCabinClassObserver() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        val startDate = LocalDate()
        val endDate = startDate.plusDays(3)
        givenParamsHaveDates(startDate, endDate)

        sut.flightCabinClassObserver.onNext(FlightServiceClassType.CabinCode.FIRST)
        assertEquals(FlightServiceClassType.CabinCode.FIRST.name, sut.getParamsBuilder().build().flightCabinClass)

        sut.flightCabinClassObserver.onNext(FlightServiceClassType.CabinCode.BUSINESS)
        assertEquals(FlightServiceClassType.CabinCode.BUSINESS.name, sut.getParamsBuilder().build().flightCabinClass)

        sut.flightCabinClassObserver.onNext(FlightServiceClassType.CabinCode.PREMIUM_COACH)
        assertEquals(FlightServiceClassType.CabinCode.PREMIUM_COACH.name, sut.getParamsBuilder().build().flightCabinClass)

        sut.flightCabinClassObserver.onNext(FlightServiceClassType.CabinCode.COACH)
        assertEquals(FlightServiceClassType.CabinCode.COACH.name, sut.getParamsBuilder().build().flightCabinClass)

    }

    @Test
    fun testFlightOutboundSearchForByot() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        givenValidStartAndEndDates()
        givenByotOutboundSearch()

        val searchParams = sut.getParamsBuilder().build()
        assertEquals(searchParams.legNo, 0)
        assertNull(searchParams.selectedOutboundLegId)

        try {
            givenValidStartAndEndDates()
            sut.getParamsBuilder().selectedLegID("leg-id")
            sut.getParamsBuilder().build()
            fail("This has to throw exception")
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun testFlightInboundSearchForByot() {
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()

        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        givenValidStartAndEndDates()
        givenByotInboundSearch()

        val searchParams = sut.getParamsBuilder().build()
        assertEquals(searchParams.legNo, 1)
        assertEquals(searchParams.selectedOutboundLegId, "leg-id")

        try {
            sut.getParamsBuilder().selectedLegID(null)
            sut.getParamsBuilder().build()
            fail("This has to throw exception")
        } catch (e: IllegalArgumentException) {
        }

    }

    @Test
    fun testByotAbacusTest() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightByotSearch)
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        givenValidStartAndEndDates()
        sut.isRoundTripSearchObservable.onNext(true)
        sut.performSearchObserver.onNext(Unit)
        assertNull(sut.searchParamsObservable.value.legNo)

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightByotSearch)
        sut.isRoundTripSearchObservable.onNext(true)
        sut.performSearchObserver.onNext(Unit)
        assertEquals(sut.searchParamsObservable.value.legNo, 0)
    }

    @Test
    fun testParamsNotSavedIfNotBucketedToRetainParamsABTest() {
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightRetainSearchParams)
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        givenValidStartAndEndDates()
        sut.isRoundTripSearchObservable.onNext(true)
        sut.performSearchObserver.onNext(Unit)
        FlightSearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedParams ->
            assertNull(loadedParams)
        })

    }

    @Test
    fun testParamsSavedIfBucketedToRetainParamsABTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightRetainSearchParams)
        givenMockServer()
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        givenValidStartAndEndDates()
        sut.isRoundTripSearchObservable.onNext(true)
        FlightSearchParamsHistoryUtil.loadPreviousFlightSearchParams(RuntimeEnvironment.application, { loadedParams ->
            assertNotNull(loadedParams)
        })
        sut.performSearchObserver.onNext(Unit)
    }

    @Test
    fun testReadyForInteractionTracking() {
        givenDefaultTravelerComponent()
        createSystemUnderTest()
        givenParamsHaveDestination()
        givenParamsHaveOrigin()
        givenValidStartAndEndDates()
        val params = sut.getParamsBuilder().build()

        val testSubscriber = TestSubscriber<Unit>()
        sut.isReadyForInteractionTracking.subscribe(testSubscriber)
        sut.previousSearchParamsObservable.onNext(params)
        testSubscriber.assertValueCount(1)
    }

    private fun givenByotOutboundSearch() {
        sut.getParamsBuilder()
                .legNo(0)
    }

    private fun givenByotInboundSearch() {
        sut.getParamsBuilder()
                .legNo(1).selectedLegID("leg-id")
    }

    private fun givenValidStartAndEndDates() {
        val startDate = LocalDate()
        val endDate = LocalDate()
        sut.getParamsBuilder()
                .startDate(startDate)
                .endDate(endDate)
    }

    private fun givenParamsHaveDates(startDate: LocalDate, endDate: LocalDate?) {
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
        sut = FlightSearchViewModel(context)
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
