package com.expedia.bookings.data.trips

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.featureconfig.SatelliteFeatureConstants
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.NonFatalLoggerInterface
import com.expedia.bookings.services.TripsServices
import com.expedia.bookings.test.CustomMatchers.Companion.hasEntries
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest.mapThat
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.TimeSource
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.SatelliteFeatureConfigTestUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.mobiata.android.util.SettingUtils
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItineraryManagerTest {

    val context = RuntimeEnvironment.application
    lateinit var itinManager: ItineraryManager
    var server: MockWebServer = MockWebServer()
        @Rule get
    val mockDispatcherTripsServices by lazy {
        val logger = HttpLoggingInterceptor()
        val file = File("../lib/mocked/templates")
        val root = file.canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        val service = TripsServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(), interceptor, Schedulers.trampoline(), Schedulers.trampoline(), MockNonFatalLogger())
        service
    }

    @Before
    fun setup() {
        itinManager = ItineraryManager.getInstance()
    }

    @After
    fun tearDown() {
        itinManager.itinCardData.clear()
    }

    @Test
    fun testHasUpcomingOrInProgressTrip() {
        val now = LocalDateTime.now().toDateTime()
        DateTimeUtils.setCurrentMillisFixed(now.millis)
        val startDates = ArrayList<DateTime>()
        startDates.add(now.minusDays(3))
        val endDates = ArrayList<DateTime>()
        endDates.add(now.plusDays(12))
        assertEquals(true, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates))

        startDates.clear()
        endDates.clear()

        startDates.add(now.plusDays(12))
        endDates.add(now.plusDays(19))
        assertEquals(false, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates))

        startDates.clear()
        endDates.clear()

        startDates.add(now.plusDays(7))
        endDates.add(now.plusDays(10))
        assertEquals(false, ItineraryManager.hasUpcomingOrInProgressTrip(startDates, endDates))
    }

    @Test
    fun testOmnitureTrackingTripRefreshCallMade() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinTripRefreshCallMade()
        assertLinkTracked("Trips Call", "App.Itinerary.Call.Made", "event286", mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.LASTMINUTE, MultiBrand.TRAVELOCITY,
            MultiBrand.ORBITZ, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.CHEAPTICKETS,
            MultiBrand.EBOOKERS, MultiBrand.VOYAGES))
    fun testOmnitureTrackingTripRefreshCallSuccessWithFlight() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.TripsNewFlightAlerts)
        OmnitureTracking.trackItinTripRefreshCallSuccess(true)
        assertLinkTrackedWithExposure("Trips Call", "App.Itinerary.Call.Success", "event287", "16205.0.1", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureTrackingTripRefreshCallSuccessWithoutFlight() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinTripRefreshCallSuccess(false)
        assertLinkTracked("Trips Call", "App.Itinerary.Call.Success", "event287", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureTrackingTripRefreshCallFailure() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackItinTripRefreshCallFailure("Trip details response is null")
        assertLinkTrackedWithError("Trips Call", "App.Itinerary.Call.Failure", "event288", "Trip details response is null", mockAnalyticsProvider)
    }

    @Test
    fun getTripComponentFromFlightHistoryIdTest() {
        assertEquals(0, itinManager.itinCardData.size)
        val firstFlightData = ItinCardDataFlightBuilder().build(multiSegment = true)
        firstFlightData.mostRelevantFlightSegment.mFlightHistoryId = 123
        val secondFlightData = ItinCardDataFlightBuilder().build(multiSegment = false)
        secondFlightData.mostRelevantFlightSegment.mFlightHistoryId = 321
        val list = itinManager.itinCardData
        list.add(firstFlightData)
        list.add(secondFlightData)
        assertNotEquals(firstFlightData, secondFlightData)
        assertEquals(2, itinManager.itinCardData.size)
        val tripFlight = firstFlightData.tripComponent as TripFlight
        assertNull(itinManager.getTripComponentFromFlightHistoryId(2222))
        assertEquals(tripFlight, itinManager.getTripComponentFromFlightHistoryId(123))
    }

    @Test
    fun getTripComponentFromFlightHistoryIdTestWithFlag() {
        val firstFlightData = ItinCardDataFlightBuilder().build(multiSegment = true)
        firstFlightData.mostRelevantFlightSegment.mFlightHistoryId = 123
        val secondFlightData = ItinCardDataFlightBuilder().build(multiSegment = false)
        secondFlightData.mostRelevantFlightSegment.mFlightHistoryId = 321
        val list = itinManager.itinCardData
        list.add(firstFlightData)
        list.add(secondFlightData)
        assertNotEquals(firstFlightData, secondFlightData)
        assertEquals(2, itinManager.itinCardData.size)
        val tripFlight = secondFlightData.tripComponent as TripFlight
        assertNull(itinManager.getTripComponentFromFlightHistoryId(2222))
        SettingUtils.save(context, R.string.preference_push_notification_any_flight, true)
        assertEquals(tripFlight, itinManager.getTripComponentFromFlightHistoryId(111))
    }

    @Test
    fun testRefreshAllTripsShared() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val spyTripServices = Mockito.spy(MockTripServices(false))
        val syncTask = mockItineraryManager.SyncTask(spyTripServices, null)

        val trip = Trip("", "12345")
        trip.setIsShared(true)
        trip.markUpdated(false, TimeSourceOne())
        val trips = mapOf<String, Trip>(Pair(trip.tripNumber, trip))
        syncTask.refreshAllTrips(TimeSourceTwenty(), trips)
        Mockito.verify(spyTripServices, Mockito.times(1)).getSharedTripDetailsObservable(Mockito.anyString())
    }

    @Test
    fun testRefreshAllTripsGuest() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val spyTripServices = Mockito.spy(MockTripServices(false))
        val syncTask = mockItineraryManager.SyncTask(spyTripServices, null)

        val trip = Trip("asd@123.com", "12345")
        trip.setIsShared(false)
        trip.markUpdated(false, TimeSourceOne())
        val trips = mapOf<String, Trip>(Pair(trip.tripNumber, trip))
        syncTask.refreshAllTrips(TimeSourceTwenty(), trips)
        Mockito.verify(spyTripServices, Mockito.times(1)).getGuestTripObservable(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())
    }

    @Test
    fun testRefreshAllTripsUserBooked() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val spyTripServices = Mockito.spy(MockTripServices(false))
        val syncTask = mockItineraryManager.SyncTask(spyTripServices, null)

        val trip = Trip("", "12345")
        trip.tripId = "12345"
        trip.setIsShared(false)
        trip.markUpdated(false, TimeSourceOne())
        val trips = mapOf<String, Trip>(Pair(trip.tripNumber, trip))
        syncTask.refreshAllTrips(TimeSourceTwenty(), trips)
        Mockito.verify(spyTripServices, Mockito.times(1)).getTripDetailsObservable(Mockito.anyString(), Mockito.anyBoolean())
    }

    @Test
    fun testRefreshAllTripsMultipleTrips() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val spyTripServices = Mockito.spy(MockTripServices(false))
        val syncTask = mockItineraryManager.SyncTask(spyTripServices, null)

        val tripGuest = Trip("asd@123.com", "12345")
        tripGuest.setIsShared(false)
        tripGuest.markUpdated(false, TimeSourceOne())
        val tripUser = Trip("", "12346")
        tripUser.tripId = "12346"
        tripUser.setIsShared(false)
        tripUser.markUpdated(false, TimeSourceOne())
        val trips = mapOf<String, Trip>(Pair(tripGuest.tripNumber, tripGuest), Pair(tripUser.tripNumber, tripUser))
        syncTask.refreshAllTrips(TimeSourceTwenty(), trips)
        Mockito.verify(spyTripServices, Mockito.times(1)).getGuestTripObservable(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())
        Mockito.verify(spyTripServices, Mockito.times(1)).getTripDetailsObservable(Mockito.anyString(), Mockito.anyBoolean())
    }

    @Test
    fun testRefreshAllTripsNoTrips() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val spyTripServices = Mockito.spy(MockTripServices(false))
        val syncTask = mockItineraryManager.SyncTask(spyTripServices, null)

        val tripGuest = Trip("asd@123.com", "12345")
        tripGuest.setIsShared(false)
        tripGuest.markUpdated(false, TimeSourceTwenty())
        val tripUser = Trip("", "12346")
        tripUser.tripId = "12346"
        tripUser.setIsShared(false)
        tripUser.markUpdated(false, TimeSourceTwenty())
        val trips = mapOf<String, Trip>(Pair(tripGuest.tripNumber, tripGuest), Pair(tripUser.tripNumber, tripUser))
        syncTask.refreshAllTrips(TimeSourceTwenty(), trips)
        Mockito.verify(spyTripServices, Mockito.times(0)).getGuestTripObservable(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())
        Mockito.verify(spyTripServices, Mockito.times(0)).getTripDetailsObservable(Mockito.anyString(), Mockito.anyBoolean())
    }

    @Test
    fun testGetTripDetailsResponse() {
        SatelliteFeatureConfigTestUtils.enableFeatureForTest(context, SatelliteFeatureConstants.ITINERARY_MANAGER_USE_RETROFIT_TRIP_DETAILS)
        val itinManager = ItineraryManager.getInstance()

        //SUCCESSFUL RESPONSE
        val mockTripServices = MockTripServices(false)
        //normal
        var trip = Trip()
        trip.tripId = "53a6459c-822c-4425-9e14-3eea43f38a97"
        var response = itinManager.SyncTask(mockTripServices, null).getTripDetailsResponse(trip, false)
        assertTrue(response.isSuccess)
        assertEquals("7238007847306", response.trip.tripNumber)
        //shared
        trip.shareInfo.sharableDetailsUrl = "https://www.expedia.com/m/trips/shared/3onkuf_eBckddgmkNz3BNcCAqKW-p7rd4kTA4H5YkcUoaVhITa7YLZksqAi7kIDkO9f2Of33KaNUvN-pzL704LOL"
        trip.setIsShared(true)
        response = itinManager.SyncTask(mockTripServices, null).getTripDetailsResponse(trip, false)
        assertTrue(response.isSuccess)
        assertEquals("1103274148635", response.trip.tripNumber)
        //guest
        trip = Trip("test123@123.com", "7313989476663")
        response = itinManager.SyncTask(mockTripServices, null).getTripDetailsResponse(trip, false)
        assertTrue(response.isSuccess)
        assertEquals("7313989476663", response.trip.tripNumber)

        //ERROR RESPONSE
        response = itinManager.SyncTask(MockTripServices(true), null).getTripDetailsResponse(trip, false)
        assertFalse(response.isSuccess)
        assertTrue(response.hasErrors())
    }

    @Test
    fun testExceptionTripDetailsSynchronous() {
        SatelliteFeatureConfigTestUtils.enableFeatureForTest(context, SatelliteFeatureConstants.ITINERARY_MANAGER_USE_RETROFIT_TRIP_DETAILS)

        val itinManager = ItineraryManager.getInstance()
        val trip = Trip()
        trip.tripId = "ItineraryManagerTest_TestExceptionTripDetails"
        val response = itinManager.SyncTask(mockDispatcherTripsServices, null).getTripDetailsResponse(trip, false)
        assertNull(response)
    }

    @Test
    fun testExceptionSharedTripSynchronous() {
        SatelliteFeatureConfigTestUtils.enableFeatureForTest(context, SatelliteFeatureConstants.ITINERARY_MANAGER_USE_RETROFIT_TRIP_DETAILS)

        val itinManager = ItineraryManager.getInstance()
        val trip = Trip()
        trip.tripId = "53a6459c-822c-4425-9e14-3eea43f38a97"
        val mockEndpoint = "http://localhost:" + server.port
        val shareUrl = "https://www.expedia.com/m/trips/shared/ItineraryManagerTest_TestExceptionSharedTrip"
        trip.shareInfo.sharableDetailsUrl = shareUrl.replace("https://www.expedia.com", mockEndpoint)
        trip.setIsShared(true)
        if (ProductFlavorFeatureConfiguration.getInstance().shouldDisplayItinTrackAppLink()) {
            val response = itinManager.SyncTask(mockDispatcherTripsServices, null).getTripDetailsResponse(trip, false)
            assertNull(response)
        }
    }

    @Test
    fun testExceptionGuestTripSynchronous() {
        SatelliteFeatureConfigTestUtils.enableFeatureForTest(context, SatelliteFeatureConstants.ITINERARY_MANAGER_USE_RETROFIT_TRIP_DETAILS)

        val itinManager = ItineraryManager.getInstance()
        val trip = Trip("test123@123.com", "ItineraryManagerTest_TestExceptionGuestTrip")
        val response = itinManager.SyncTask(mockDispatcherTripsServices, null).getTripDetailsResponse(trip, false)
        assertNull(response)
    }

    @Test
    fun testTripDetailsObservableErrorResponse() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val syncTask = mockItineraryManager.SyncTask(null, null)
        val errorData = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/error_trip_response.json"))).readUtf8()
        val errorJsonObject = JSONObject(errorData)
        val testHandleTripResponse = TestHandleTripResponse()

        syncTask.waitAndParseDetailResponses(listOf(Observable.just(errorJsonObject)), null, testHandleTripResponse)
        assertTrue(testHandleTripResponse.refreshTripResponseNullCalled)
    }

    @Test
    fun testTripDetailsObservableSuccessInvalidKey() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val syncTask = mockItineraryManager.SyncTask(null, null)
        val legitData = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
        val legitDataJSONObject = JSONObject(legitData)
        val testHandleTripResponse = TestHandleTripResponse()

        syncTask.waitAndParseDetailResponses(listOf(Observable.just(legitDataJSONObject)), HashMap<String, Trip>(), testHandleTripResponse)
        assertFalse(testHandleTripResponse.refreshTripResponseHasErrorsCalled)
        assertFalse(testHandleTripResponse.refreshTripResponseNullCalled)
        assertFalse(testHandleTripResponse.refreshTripResponseSuccessCalled)
    }

    @Test
    fun testTripDetailsObservableSuccessHappyPath() {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val syncTask = mockItineraryManager.SyncTask(null, null)
        val legitData = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
        val legitDataJSONObject = JSONObject(legitData)
        val testHandleTripResponse = TestHandleTripResponse()

        val trips = HashMap<String, Trip>()
        trips.put("1103274148635", Trip())
        syncTask.waitAndParseDetailResponses(listOf(Observable.just(legitDataJSONObject)), trips, testHandleTripResponse)
        assertFalse(testHandleTripResponse.refreshTripResponseHasErrorsCalled)
        assertFalse(testHandleTripResponse.refreshTripResponseNullCalled)
        assertTrue(testHandleTripResponse.refreshTripResponseSuccessCalled)
    }

    private fun assertLinkTracked(linkName: String, rfrrId: String, event: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }

    private fun assertLinkTrackedWithExposure(linkName: String, rfrrId: String, event: String, evar34: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event,
                "&&v34" to evar34
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }

    private fun assertLinkTrackedWithError(linkName: String, rfrrId: String, event: String, error: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event,
                "&&c36" to error
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), mapThat(hasEntries(expectedData)))
    }

    private class TimeSourceOne : TimeSource {
        override fun now(): Long = TimeUnit.MINUTES.toMillis(1)
    }

    private class TimeSourceTwenty : TimeSource {
        override fun now(): Long = TimeUnit.MINUTES.toMillis(20)
    }

    private class MockNonFatalLogger : NonFatalLoggerInterface {
        override fun logException(e: Exception) {
            println("MockNonFatalLogger: ${e.printStackTrace()}")
        }
    }

    private class TestHandleTripResponse : IHandleTripResponse {
        var refreshTripResponseNullCalled = false
        var refreshTripResponseHasErrorsCalled = false
        var refreshTripResponseSuccessCalled = false
        override fun refreshTripResponseNull(trip: Trip) {
            refreshTripResponseNullCalled = true
            return
        }

        override fun refreshTripResponseHasErrors(trip: Trip, tripDetailsResponse: TripDetailsResponse) {
            refreshTripResponseHasErrorsCalled = true
            return
        }

        override fun refreshTripResponseSuccess(trip: Trip, deepRefresh: Boolean, tripDetailsResponse: TripDetailsResponse) {
            refreshTripResponseSuccessCalled = true
            return
        }
    }
}
