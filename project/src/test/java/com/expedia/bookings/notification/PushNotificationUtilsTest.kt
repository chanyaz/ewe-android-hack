package com.expedia.bookings.notification

import android.content.Context
import com.activeandroid.ActiveAndroid
import com.activeandroid.query.Select
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(
        constants = BuildConfig::class,
        application = ActiveAndroidNotifcationTestApp::class,
        sdk = [23]
)
class PushNotificationUtilsTest {
    lateinit var context: Context

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        ActiveAndroid.initialize(context)
    }

    @After
    fun tearDown() {
        ActiveAndroid.dispose()
    }

    @Test
    fun generateFlightAlertNotificationNullDataTest() {
        assertEquals(0, getNotifications().size)
        PushNotificationUtils.generateFlightAlertNotification(context, 3213, "S_Push_Flight_delayed_with_new_departure_time",
                arrayOf("SFO", "9:20PM"), "S_Push_Flight_delayed_title", "123123", null)
        val notification = getNotifications().first()
        assertEquals("-1", notification.itinId)
        notificationAssertions(notification)
    }

    @Test
    fun generateFlightAlertNotificationWithDataTest() {
        assertEquals(0, getNotifications().size)
        val flightData = ItinCardDataFlightBuilder().build()
        PushNotificationUtils.generateFlightAlertNotification(context, 3213, "S_Push_Flight_delayed_with_new_departure_time",
                arrayOf("SFO", "9:20PM"), "S_Push_Flight_delayed_title", "123123", flightData)
        val notification = getNotifications().first()
        assertEquals(flightData.id, notification.itinId)
        notificationAssertions(notification)
    }

    private fun getNotifications(): List<Notification> {
        return Select().from(Notification::class.java)
                .execute<Notification>()
    }

    private fun notificationAssertions(notification: Notification) {
        assertEquals("Push_123123", notification.uniqueId)
        assertEquals(PushNotificationUtils.getLocNewString(context, Notification.NotificationType.FLIGHT_DELAYED,
                "S_Push_Flight_delayed_with_new_departure_time", arrayOf("SFO", "9:20PM")), notification.body)
        assertEquals(notification.body, notification.ticker)
        assertEquals(PushNotificationUtils.getLocStringForKey(context, "S_Push_Flight_delayed_title"), notification.title)
        assertEquals(Notification.FLAG_PUSH, notification.flags)
    }
}
