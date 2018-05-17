package com.expedia.bookings.itin.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.bitmaps.IMedia
import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.notification.INotificationManager
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.services.ITNSServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.ItinContentGenerator
import com.expedia.bookings.widget.itin.SummaryButton
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.FlightCode
import com.mobiata.flightlib.data.Waypoint
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class NotificationSchedulerTest {

    lateinit var context: Context
    lateinit var sut: NotificationScheduler
    private lateinit var tnsServicesMock: TestTNSService
    private val itinDatas = listOf<ItinCardData>(ItinCardDataFlightBuilder().build(), ItinCardDataFlightBuilder().build(multiSegment = true, isShared = true))
    private lateinit var notificationManagerMock: TestNotificationManager
    private lateinit var userStateManager: UserStateManager
    private lateinit var pos: PointOfSale

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        userStateManager = UserLoginTestUtil.getUserStateManager()
        pos = PointOfSale.getPointOfSale()
        tnsServicesMock = Mockito.spy(TestTNSService())
        val dbMock = Mockito.mock(Db::class.java)
        Mockito.`when`(dbMock.abacusGuid).thenReturn("333333")
        notificationManagerMock = Mockito.spy(TestNotificationManager())
        val gcmKeeperMock = Mockito.mock(GCMRegistrationKeeper::class.java)
        Mockito.`when`(gcmKeeperMock.getRegistrationId(context)).thenReturn("1234")
        sut = Mockito.spy(NotificationScheduler(context = context,
                db = dbMock,
                notificationManager = notificationManagerMock,
                userStateManager = userStateManager,
                tnsServices = tnsServicesMock,
                gcmRegistrationKeeper = gcmKeeperMock,
                pos = pos))
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

        Mockito.`when`(sut.getGenerator(itinDatas[0])).thenReturn(MockItinGenerator(context, itinDatas[0]))
        sut.registerForPushNotifications(itinDatas)
        Mockito.verify(sut, Mockito.times(1)).getTNSUser(pos.siteId)
    }

    @Test
    fun getTNSUserTest() {
        assertEquals(TNSUser("15", null, null, "333333"), sut.getTNSUser(15))
    }

    @Test
    fun registerForPushNotificationsToggleOnTest() {
        Mockito.`when`(sut.getGenerator(itinDatas[0])).thenReturn(MockItinGenerator(context, itinDatas[0]))
        sut.registerForPushNotifications(itinDatas)
        Mockito.verify(sut, Mockito.times(1)).getTNSUser(pos.siteId)
        assertNotNull(tnsServicesMock.tnsCourier)
        assertNotNull(tnsServicesMock.tnsUser)
        assertNotEquals(0, tnsServicesMock.tnsFlights?.size)
        Mockito.verify(tnsServicesMock, Mockito.times(1)).registerForFlights(tnsServicesMock.tnsUser!!, tnsServicesMock.tnsCourier!!, tnsServicesMock.tnsFlights!!)
    }

    @After
    fun tearDown() = AbacusTestUtils.resetABTests()

    @Test
    fun testGetItinFlightswithMultiSegmentFlight() =
            assertEquals(2, sut.getItinFlights(itinCardDataMultiSegmentFlight()).size)

    @Test
    fun testGetItinFlightswithHotel() = assertEquals(0, sut.getItinFlights(itinCardDataHotel()).size)

    @Test
    fun testGetItinFlightswithSharedFlight() = assertEquals(1, sut.getItinFlights(itinCardDataSharedFlight()).size)

    @Test
    fun testGetFlightsForNewSystem() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val testItinCardDataShared = ItinCardDataFlightBuilder().build()
        testItinCardDataShared.tripComponent.parentTrip.setIsShared(true)
        val itinCardDatas = listOf(testItinCardData, testItinCardDataShared)
        val dateTimeTimeZonePattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSZ"

        val expectedFlightList = listOf(TNSFlight("UA", "2017-09-05T21:33:00.000-0700", "2017-09-05T20:00:00.000-0700", "LAS", "681", "SFO"),
                TNSFlight("UA", "2017-09-05T21:33:00.000-0700", "2017-09-05T20:00:00.000-0700", "LAS", "681", "SFO"))
        val expectedDateFormatWithTZ = JodaUtils.format(testItinCardData.flightLeg.getSegment(0).segmentDepartureTime, dateTimeTimeZonePattern)
        val actualFlightList = sut.getFlightsForNewSystem(itinCardDatas)

        assertEquals(expectedFlightList, actualFlightList)
        assertEquals(expectedDateFormatWithTZ, actualFlightList[0].departure_date)
    }

    @Test
    fun testIsFlightDataAvailable() {
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

    class MockItinGenerator(context: Context, data: ItinCardData) : ItinContentGenerator<ItinCardData>(context, data) {
        override fun getTypeIconResId(): Int = 0

        override fun getType(): TripComponent.Type = TripComponent.Type.FLIGHT

        override fun getShareSubject(): String = ""

        override fun getShareTextShort(): String = ""

        override fun getShareTextLong(): String = ""

        override fun getHeaderImagePlaceholderResId(): Int = 0

        override fun getHeaderBitmapDrawable(): MutableList<out IMedia> = listOf<IMedia>().toMutableList()

        override fun getHeaderText(): String = ""

        override fun getReloadText(): String = ""

        override fun getTitleView(convertView: View?, container: ViewGroup?): View = View(context)

        override fun getSummaryView(convertView: View?, container: ViewGroup?): View = View(context)

        override fun getDetailsView(convertView: View?, container: ViewGroup?): View = View(context)

        override fun getSummaryLeftButton(): SummaryButton = SummaryButton(1, "", {})

        override fun getSummaryRightButton(): SummaryButton = SummaryButton(1, "", {})

        override fun getAddToCalendarIntents(): MutableList<Intent> = listOf<Intent>().toMutableList()

        override fun generateNotifications(): MutableList<Notification> = listOf(Notification()).toMutableList()
    }

    private fun itinCardDataMultiSegmentFlight(): List<ItinCardDataFlight> = listOf(ItinCardDataFlightBuilder().build(multiSegment = true))

    private fun itinCardDataHotel(): List<ItinCardDataHotel> = listOf(ItinCardDataHotelBuilder().build())

    private fun itinCardDataSharedFlight(): List<ItinCardDataFlight> = listOf(ItinCardDataFlightBuilder().build(isShared = true))

    private class TestFlight : Flight() {
        override fun getSegmentArrivalTime(): DateTime? = null
        override fun getSegmentDepartureTime(): DateTime? = null
        override fun getPrimaryFlightCode(): FlightCode? = null
        override fun getOriginWaypoint(): Waypoint? = null
        override fun getDestinationWaypoint(): Waypoint? = null
    }

    open class TestTNSService : ITNSServices {
        var tnsUser: TNSUser? = null
        var tnsCourier: Courier? = null
        var tnsFlights: List<TNSFlight>? = listOf()
        override fun registerForFlights(user: TNSUser, courier: Courier, flights: List<TNSFlight>): Disposable {
            tnsUser = user
            tnsCourier = courier
            tnsFlights = flights
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
}
