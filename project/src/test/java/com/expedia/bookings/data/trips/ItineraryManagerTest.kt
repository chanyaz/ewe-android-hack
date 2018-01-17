package com.expedia.bookings.data.trips

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.test.CustomMatchers.Companion.hasEntries
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest.mapThat
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.TimeSource
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.FlightCode
import com.mobiata.flightlib.data.Waypoint
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItineraryManagerTest {

    val context = RuntimeEnvironment.application
    lateinit var itinManager: ItineraryManager

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
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.TripsNewFlightAlerts, R.string.preference_enable_trips_flight_alerts)
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
    fun testGetItinFlightswithMultiSegmentFlight() {
        val mockedItinManager = createMockItinManager(itinCardDataMultiSegmentFlight())
        Mockito.`when`(mockedItinManager.itinFlights).thenCallRealMethod()
        val usersFlightList = mockedItinManager.itinFlights
        assertEquals(2, usersFlightList.size)
    }

    @Test
    fun testGetItinFlightswithHotel() {
        val mockedItinManager = createMockItinManager(itinCardDataHotel())
        Mockito.`when`(mockedItinManager.itinFlights).thenCallRealMethod()
        val usersFlightList = mockedItinManager.itinFlights
        assertEquals(0, usersFlightList.size)
    }

    @Test
    fun testGetItinFlightswithSharedFlight() {
        val mockedItinManager = createMockItinManager(itinCardDataSharedFlight())
        Mockito.`when`(mockedItinManager.itinFlights).thenCallRealMethod()
        val usersFlightList = mockedItinManager.itinFlights
        assertEquals(1, usersFlightList.size)
    }

    @Test
    fun testGetFlightsForNewSystem() {
        val sut = ItineraryManager.getInstance()
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val testItinCardDataShared = ItinCardDataFlightBuilder().build()
        testItinCardDataShared.tripComponent.parentTrip.setIsShared(true)
        val itinCardData = sut.itinCardData
        val dateTimeTimeZonePattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"

        itinCardData.clear()
        itinCardData.add(testItinCardData)
        itinCardData.add(testItinCardDataShared)

        val expectedFlightList = listOf<TNSFlight>(TNSFlight("UA", "2017-09-05T21:33:00.000-0700", "2017-09-05T20:00:00.000-0700", "LAS", "681", "SFO"),
                TNSFlight("UA", "2017-09-05T21:33:00.000-0700", "2017-09-05T20:00:00.000-0700", "LAS", "681", "SFO"))
        val expectedDateFormatWithTZ = JodaUtils.format(testItinCardData.flightLeg.getSegment(0).segmentDepartureTime, dateTimeTimeZonePattern)
        val actualFlightList = sut.flightsForNewSystem

        assertEquals(expectedFlightList, actualFlightList)
        assertEquals(expectedDateFormatWithTZ, actualFlightList[0].departure_date)
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
    fun testIsFlightDataAvailable() {
        val sut = ItineraryManager.getInstance()
        val testItinCardData = ItinCardDataFlightBuilder().build()
        assertTrue(sut.isFlightDataAvailable(testItinCardData.flightLeg.getSegment(0)))

        val testFlightInvalid = TestFlight()
        assertFalse(sut.isFlightDataAvailable(testFlightInvalid))

        var testFlightValid = Flight()
        var testFlightCode = FlightCode()
        testFlightCode.mAirlineCode = null
        testFlightValid.addFlightCode(testFlightCode, Flight.F_PRIMARY_AIRLINE_CODE)
        assertFalse(sut.isFlightDataAvailable(testFlightValid))

        testFlightValid = Flight()
        testFlightCode = FlightCode()
        testFlightCode.mNumber = ""
        assertFalse(sut.isFlightDataAvailable(testFlightValid))

        testFlightValid = Flight()
        var testWaypoint = Waypoint(Waypoint.ACTION_ARRIVAL)
        testWaypoint.mAirportCode = ""
        testFlightInvalid.destinationWaypoint = testWaypoint
        assertFalse(sut.isFlightDataAvailable(testFlightValid))

        testFlightValid = Flight()
        testWaypoint = Waypoint(Waypoint.ACTION_DEPARTURE)
        testWaypoint.mAirportCode = ""
        testFlightInvalid.originWaypoint = testWaypoint
        assertFalse(sut.isFlightDataAvailable(testFlightValid))
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
        SettingUtils.save(context, R.string.preference_trips_use_retrofit_call_for_details, true)
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

    private fun createMockItinManager(itinCardData: ItinCardData): ItineraryManager {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        Mockito.`when`(mockItineraryManager.itinCardData).thenReturn(listOf(itinCardData))
        return mockItineraryManager
    }

    private fun itinCardDataMultiSegmentFlight(): ItinCardDataFlight = ItinCardDataFlightBuilder().build(multiSegment = true)

    private fun itinCardDataHotel(): ItinCardDataHotel = ItinCardDataHotelBuilder().build()

    private fun itinCardDataSharedFlight(): ItinCardDataFlight = ItinCardDataFlightBuilder().build(isShared = true)

    private class TestFlight : Flight() {
        override fun getSegmentArrivalTime(): DateTime? = null
        override fun getSegmentDepartureTime(): DateTime? = null
        override fun getPrimaryFlightCode(): FlightCode? = null
        override fun getOriginWaypoint(): Waypoint? = null
        override fun getDestinationWaypoint(): Waypoint? = null
    }

    private class TimeSourceOne : TimeSource {
        override fun now(): Long = TimeUnit.MINUTES.toMillis(1)
    }

    private class TimeSourceTwenty : TimeSource {
        override fun now(): Long = TimeUnit.MINUTES.toMillis(20)
    }
}
