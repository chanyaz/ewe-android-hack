package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.data.Flight
import com.expedia.bookings.itin.tripstore.data.FlightLocation
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinTime
import com.expedia.bookings.itin.tripstore.extensions.getDateTime
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.notification.INotificationManager
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.services.ITNSServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class NotificationSchedulerTest {

    lateinit var context: Context
    lateinit var sut: NotificationScheduler
    private lateinit var tnsServicesMock: MockTNSService
    private val itinDatas = listOf<ItinCardData>(ItinCardDataFlightBuilder().build(), ItinCardDataFlightBuilder().build(multiSegment = true, isShared = true))
    private lateinit var notificationManagerMock: TestNotificationManager
    private lateinit var userStateManager: UserStateManager
    private lateinit var pos: PointOfSale
    private lateinit var mockJsonUtil: MockJsonUtil

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        userStateManager = UserLoginTestUtil.getUserStateManager()
        pos = PointOfSale.getPointOfSale()
        tnsServicesMock = MockTNSService()
        val dbMock = Mockito.mock(Db::class.java)
        Mockito.`when`(dbMock.abacusGuid).thenReturn("333333")
        notificationManagerMock = Mockito.spy(TestNotificationManager())
        val gcmKeeperMock = Mockito.mock(GCMRegistrationKeeper::class.java)
        Mockito.`when`(gcmKeeperMock.getRegistrationId(context)).thenReturn("1234")
        mockJsonUtil = MockJsonUtil()
        sut = NotificationScheduler(context = context,
                db = dbMock,
                notificationManager = notificationManagerMock,
                userStateManager = userStateManager,
                tnsServices = tnsServicesMock,
                gcmRegistrationKeeper = gcmKeeperMock,
                pos = pos,
                jsonUtil = mockJsonUtil)
    }

    @Test
    fun scheduleLocalNotifications() {
        sut.scheduleLocalNotifications(itinDatas)
        Mockito.verify(notificationManagerMock, Mockito.times(1)).cancelAllExpired()
        Mockito.verify(notificationManagerMock, Mockito.times(1)).scheduleAll()
        assertNotNull(notificationManagerMock.mNotification)
        Mockito.verify(notificationManagerMock, Mockito.times(1)).searchForExistingAndUpdate(notificationManagerMock.mNotification!!)
    }

    @Test
    fun registerForPushNotificationsTest() {
        mockJsonUtil.list.add(ItinMocker.flightDetailsHappy)
        assertFalse(tnsServicesMock.registerForFlightsCalled)
        sut.registerForPushNotifications()
        assertTrue(tnsServicesMock.registerForFlightsCalled)
    }

    @Test
    fun getTNSUserTest() {
        assertEquals(TNSUser("15", null, null, "333333"), sut.getTNSUser(15))
    }

    @Test
    fun registerForPushNotificationsToggleOnTest() {
        mockJsonUtil.list.add(ItinMocker.flightDetailsHappyMultiSegment)
        assertFalse(tnsServicesMock.registerForFlightsCalled)
        sut.registerForPushNotifications()
        assertNotNull(tnsServicesMock.tnsCourier)
        assertNotNull(tnsServicesMock.tnsUser)
        assertEquals(5, tnsServicesMock.tnsFlights?.size)
        assertTrue(tnsServicesMock.registerForFlightsCalled)
    }

    @Test
    fun testGetItinFlightswithMultiSegmentFlight() =
            assertEquals(6, sut.getTNSFlights(listOf(ItinMocker.flightDetailsHappy, ItinMocker.flightDetailsHappyMultiSegment)).size)

    @Test
    fun testGetItinFlightswithHotel() = assertEquals(0, sut.getTNSFlights(listOf(ItinMocker.hotelDetailsHappy, ItinMocker.hotelDetailsExpediaCollect)).size)

    @Test
    fun testPackageGetTNSFlights() = assertEquals(2, sut.getTNSFlights(listOf(ItinMocker.hotelPackageHappy)).size)

    @Test
    fun testGetFlightsForNewSystem() {
        val dateTimeTimeZonePattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"

        val expectedFlightList = listOf(TNSFlight(airline = "UA", arrival_date = "2017-09-05T21:33:00.000-0700", departure_date = "2017-09-05T20:00:00.000-0700", destination = "LAS", flight_no = "681", origin = "SFO", is_last = true))

        val expectedDateFormatWithTZ = JodaUtils.format(ItinMocker.flightDetailsHappy.flights?.first()?.legs?.first()?.segments?.first()?.departureTime?.getDateTime()!!, dateTimeTimeZonePattern)
        val actualFlightList = sut.getTNSFlights(listOf(ItinMocker.flightDetailsHappy))

        assertEquals(expectedFlightList, actualFlightList)
        assertEquals(expectedDateFormatWithTZ, actualFlightList[0].departure_date)
    }

    @Test
    fun testIsFlightDataAvailable() {
        val happyFlight = Flight(FlightLocation("OTO"),
                FlightLocation("SFO"), "AA123", "A445", ItinTime("May 5",
                "May 5", "May 5", "2017-04-05T21:33:00.000-0700"), ItinTime("May 6",
                "May 6", "May 6", "2017-04-06T21:33:00.000-0700"))
        assertTrue(sut.isFlightDataAvailable(happyFlight))

        val testNullAirlineCode = Flight(FlightLocation("OTO"),
                FlightLocation("SFO"), null, "A445", ItinTime("May 5",
                "May 5", "May 5", "2017-04-05T21:33:00.000-0700"), ItinTime("May 6",
                "May 6", "May 6", "2017-04-06T21:33:00.000-0700"))
        assertFalse(sut.isFlightDataAvailable(testNullAirlineCode))

        val testNullFlightNumber = Flight(FlightLocation("OTO"),
                FlightLocation("SFO"), "AA123", null, ItinTime("May 5",
                "May 5", "May 5", "2017-04-05T21:33:00.000-0700"), ItinTime("May 6",
                "May 6", "May 6", "2017-04-06T21:33:00.000-0700"))
        assertFalse(sut.isFlightDataAvailable(testNullFlightNumber))

        val testArrivalTimeRawNull = Flight(FlightLocation("OTO"),
                FlightLocation("SFO"), "AA123", "A445", ItinTime("May 5",
                "May 5", "May 5", "2017-04-05T21:33:00.000-0700"), ItinTime("May 6",
                "May 6", "May 6", null))
        assertFalse(sut.isFlightDataAvailable(testArrivalTimeRawNull))

        val testDepartureTimeRawNull = Flight(FlightLocation("OTO"),
                FlightLocation("SFO"), "AA123", "A445", ItinTime("May 5",
                "May 5", "May 5", null), ItinTime("May 6",
                "May 6", "May 6", "2017-04-06T21:33:00.000-0700"))
        assertFalse(sut.isFlightDataAvailable(testDepartureTimeRawNull))

        val testArrivalLocationAirportCodeNull = Flight(FlightLocation("OTO"),
                FlightLocation(null), "AA123", "A445", ItinTime("May 5",
                "May 5", "May 5", "2017-04-05T21:33:00.000-0700"), ItinTime("May 6",
                "May 6", "May 6", "2017-04-06T21:33:00.000-0700"))
        assertFalse(sut.isFlightDataAvailable(testArrivalLocationAirportCodeNull))

        val testDepartureLocationAirportCodeNull = Flight(FlightLocation(null),
                FlightLocation("SFO"), "AA123", "A445", ItinTime("May 5",
                "May 5", "May 5", "2017-04-05T21:33:00.000-0700"), ItinTime("May 6",
                "May 6", "May 6", "2017-04-06T21:33:00.000-0700"))
        assertFalse(sut.isFlightDataAvailable(testDepartureLocationAirportCodeNull))
    }

    private class MockTNSService : ITNSServices {
        var tnsUser: TNSUser? = null
        var tnsCourier: Courier? = null
        var tnsFlights: List<TNSFlight>? = listOf()
        var registerForFlightsCalled = false
        override fun registerForFlights(user: TNSUser, courier: Courier, flights: List<TNSFlight>): Disposable {
            tnsUser = user
            tnsCourier = courier
            tnsFlights = flights
            registerForFlightsCalled = true
            return Observable.just("1").subscribe()
        }

        override fun deregisterForFlights(user: TNSUser, courier: Courier) {
            tnsFlights = null
        }
    }

    open class TestNotificationManager : INotificationManager {
        override fun scheduleNotification(notification: Notification) {
        }

        override fun cancelNotificationIntent(notification: Notification) {
        }

        override fun cancelAndDeleteNotification(notification: Notification) {
        }

        override fun dismissNotification(notification: Notification) {
        }

        override fun findExisting(notification: Notification): Notification? {
            return notification
        }

        override fun hasExisting(notification: Notification): Boolean {
            return true
        }

        override fun setNotificationStatusToDismissed(notification: Notification) {
        }

        override fun deleteAll() {
        }

        override fun deleteAll(itinId: String) {
        }

        override fun wasFired(uniqueId: String): Boolean {
            return true
        }

        var mNotification: Notification? = null
        override fun scheduleAll() = Unit

        override fun cancelAllExpired() = Unit

        override fun searchForExistingAndUpdate(notification: Notification) {
            mNotification = notification
        }
    }

    private class MockJsonUtil(val list: MutableList<Itin> = mutableListOf()) : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            return null
        }

        override fun getItinList(): List<Itin> {
            return list
        }
    }
}
