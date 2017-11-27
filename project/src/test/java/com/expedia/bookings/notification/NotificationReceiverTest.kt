package com.expedia.bookings.notification

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.test.CustomMatchers
import com.expedia.bookings.test.NullSafeMockitoHamcrest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class NotificationReceiverTest {

    lateinit private var mockItineraryManager: ItineraryManager
    lateinit private var notificationReceiver: TestNotificationReceiver

    @Before
    fun setUp() {
        mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
    }

    @Test
    fun newNotificationAddsListenerCallsSync() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val uniqueId = "1234-legOne"

        val itinId = "1234"
        val triggerTime = System.currentTimeMillis() - 60000
        val ourNotification = Notification(uniqueId, itinId, triggerTime)
        ourNotification.notificationType = Notification.NotificationType.FLIGHT_CHECK_IN
        ourNotification.expirationTimeMillis = System.currentTimeMillis() + 60000

        val ourIntent = Intent(context, NotificationReceiver::class.java)
        val uriString = "expedia://notification/schedule/" + ourNotification.uniqueId
        ourIntent.data = Uri.parse(uriString)
        ourIntent.putExtra(NotificationReceiver.EXTRA_ACTION, NotificationReceiver.ACTION_SCHEDULE)
        ourIntent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION, ourNotification.toJson().toString())

        notificationReceiver = TestNotificationReceiver(mockItineraryManager, ourNotification)
        notificationReceiver.onReceive(context, ourIntent)

        Mockito.verify(mockItineraryManager, Mockito.times(1)).addSyncListener(Mockito.any(ItineraryManager.ItinerarySyncAdapter::class.java))
        Mockito.verify(mockItineraryManager, Mockito.times(1)).startSync(false)
    }

    @Test
    fun notificationPastTriggerTime() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val uniqueId = "1234-legOne"
        val itinId = "1234"
        val triggerTime = DateTime.now().minusMinutes(4).millis
        val ourNotification = Notification(uniqueId, itinId, triggerTime)
        ourNotification.notificationType = Notification.NotificationType.FLIGHT_CHECK_IN
        ourNotification.expirationTimeMillis = System.currentTimeMillis() + 60000

        val ourIntent = Intent(context, NotificationReceiver::class.java)
        val uriString = "expedia://notification/schedule/" + ourNotification.uniqueId
        ourIntent.data = Uri.parse(uriString)
        ourIntent.putExtra(NotificationReceiver.EXTRA_ACTION, NotificationReceiver.ACTION_SCHEDULE)
        ourIntent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION, ourNotification.toJson().toString())

        notificationReceiver = TestNotificationReceiver(mockItineraryManager, ourNotification)
        assertEquals(ourNotification.status, Notification.StatusType.NEW)
        notificationReceiver.onReceive(context, ourIntent)
        Mockito.verify(mockItineraryManager, Mockito.never()).addSyncListener(Mockito.any(ItineraryManager.ItinerarySyncAdapter::class.java))
        Mockito.verify(mockItineraryManager, Mockito.never()).startSync(false)
    }

    @Test
    fun testShouldLaunchAllNotifications() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)
        SettingUtils.save(context, context.getString(R.string.preference_launch_all_trip_notifications), true)
        assertTrue(notificationReceiver.launchAllNotifications(context))
    }

    @Test
    fun testShouldNotLaunchAllNotifications() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)
        SettingUtils.save(context, context.getString(R.string.preference_launch_all_trip_notifications), false)
        assertFalse(notificationReceiver.launchAllNotifications(context))
    }

    @Test
    fun notificationShowsOnSyncFailure() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val uniqueId = "1234-legOne"
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)
        val listener = notificationReceiver.makeValidTripSyncListener(context, makeNotification(uniqueId), mockItineraryManager)
        listener.onSyncFailure(ItineraryManager.SyncError.CANCELLED)

        Mockito.verify(mockItineraryManager, Mockito.times(1)).removeSyncListener(Mockito.any(ItineraryManager.ItinerarySyncAdapter::class.java))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)
        val allNotifications = shadowNotificationManager.allNotifications
        assertEquals(1, allNotifications.size)
    }

    @Test
    fun notificationShowsForValidTrip() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val notificationId = "1234_checkin"
        val tripId = "1234"
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)

        val listener = notificationReceiver.makeValidTripSyncListener(context, makeNotification(notificationId), mockItineraryManager)
        listener.onSyncFinished(makeTrip(tripId))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)
        val allNotifications = shadowNotificationManager.allNotifications
        assertEquals(1, allNotifications.size)
    }

    @Test
    fun notificationDoesNotShowForInvalidTrip() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val uniqueId = "1234-legOne"
        val invalidUniqueId = "4321 legTwo"
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)

        val listener = notificationReceiver.makeValidTripSyncListener(context, makeNotification(uniqueId), mockItineraryManager)
        val invalidTrip = makeTrip(invalidUniqueId)
        listener.onSyncFinished(invalidTrip)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)
        val allNotifications = shadowNotificationManager.allNotifications
        assertEquals(0, allNotifications.size)
    }

    @Test
    fun notificationShowsForBookingStatusNotificationTypeAndInvalidTrip() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val invalidUniqueId = "4321 legTwo"
        val notificationType = Notification.NotificationType.DESKTOP_BOOKING
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)

        notificationReceiver.checkTripValidAndShowNotification(context, makeNotification(invalidUniqueId, notificationType))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)
        val allNotifications = shadowNotificationManager.allNotifications

        Mockito.verify(mockItineraryManager, never()).addSyncListener(Mockito.any(ItineraryManager.ItinerarySyncAdapter::class.java))
        Mockito.verify(mockItineraryManager, never()).startSync(false)
        assertEquals(1, allNotifications.size)
    }

    @Test
    fun testNotificationShownOmnitureTracking() {
        val context = Robolectric.buildActivity(Activity::class.java).create().get()
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = shadowOf(notificationManager)

        val listOfNotificationTypes = listOf(
                Notification.NotificationType.ACTIVITY_START,
                Notification.NotificationType.CAR_DROP_OFF,
                Notification.NotificationType.CAR_PICK_UP,
                Notification.NotificationType.FLIGHT_CHECK_IN,
                Notification.NotificationType.FLIGHT_SHARE,
                Notification.NotificationType.FLIGHT_CANCELLED,
                Notification.NotificationType.FLIGHT_GATE_TIME_CHANGE,
                Notification.NotificationType.FLIGHT_GATE_NUMBER_CHANGE,
                Notification.NotificationType.FLIGHT_BAGGAGE_CLAIM,
                Notification.NotificationType.HOTEL_CHECK_IN,
                Notification.NotificationType.HOTEL_CHECK_OUT,
                Notification.NotificationType.FLIGHT_DEPARTURE_REMINDER,
                Notification.NotificationType.DESKTOP_BOOKING,
                Notification.NotificationType.HOTEL_GET_READY
        )

        for (notificationType in listOfNotificationTypes) {
            shadowNotificationManager.cancelAll()
            makeNotificationAndShow(context, notificationType, notificationType.name)
            val allNotifications = shadowNotificationManager.allNotifications
            assertEquals(1, allNotifications.size)
            val trackingLink: String = OmnitureTracking.setItinNotificationLink(makeNotification(notificationType.name, notificationType))
            assertLinkTrackedWhenNotificationShown(trackingLink, trackingLink, "event208", mockAnalyticsProvider)
        }
    }

    private fun makeNotificationAndShow(context: Context, notificationType: Notification.NotificationType, id: String) {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val notificationReceiver = TestNotificationReceiver(mockItineraryManager, null)

        val listener = notificationReceiver.makeValidTripSyncListener(context, makeNotification(id, notificationType), mockItineraryManager)
        listener.onSyncFinished(makeTrip(id))
    }

    private fun assertLinkTrackedWhenNotificationShown(linkName: String, rfrrId: String, event: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v28" to rfrrId,
                "&&c16" to rfrrId,
                "&&events" to event
        )

        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), NullSafeMockitoHamcrest.mapThat(CustomMatchers.hasEntries(expectedData)))
    }

    private fun makeNotification(uniqueId: String, type: Notification.NotificationType = Notification.NotificationType.FLIGHT_CANCELLED): Notification {
        val ourNotification = Mockito.mock(Notification::class.java)

        Mockito.`when`(ourNotification.uniqueId).thenReturn(uniqueId)
        Mockito.`when`(ourNotification.notificationType).thenReturn(type)
        Mockito.doNothing().`when`(ourNotification).didNotify()
        Mockito.`when`(ourNotification.imageType).thenReturn(Notification.ImageType.NONE)
        Mockito.`when`(ourNotification.toJson()).thenReturn(JSONObject())
        return ourNotification
    }

    private fun makeTrip(uniqueId: String): List<Trip> {
        val ourTrip = Trip()

        ourTrip.tripComponents.add(0, TripComponent())
        ourTrip.tripComponents[0].uniqueId = uniqueId
        return listOf(ourTrip)
    }

    class TestNotificationReceiver(val mockItineraryManager: ItineraryManager, val ourNotification: Notification?) : NotificationReceiver() {
        override fun getItineraryManagerInstance(): ItineraryManager {
            return mockItineraryManager
        }

        override fun findExistingNotification(notificationManager: com.expedia.bookings.notification.NotificationManager, deserialized: Notification): Notification {
            return ourNotification ?: Notification()
        }

        override fun makeNotification(): Notification {
            return Mockito.mock(Notification::class.java)
        }
    }

}
