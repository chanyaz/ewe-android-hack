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
    fun testGenerateFlightAlertNotificationWithEventType() {
        assertEquals(0, getNotifications().size)

        val flightData = ItinCardDataFlightBuilder().build()
        PushNotificationUtils.generateFlightAlertNotification(context, 3213, "Your flight to SFO is delayed with a new arrival at 18:11.", "Flight delayed", "123123", flightData, "S_Push_Flight_gate_changed")
        val notification = getNotifications().first()

        assertEquals(flightData.id, notification.itinId)
        notificationAssertions(notification, "123123", "Your flight to SFO is delayed with a new arrival at 18:11.")
    }

    @Test
    fun testGenerateFlightAlertNotificationWithNoMatchingNotificationType() {
        assertEquals(0, getNotifications().size)

        val flightData = ItinCardDataFlightBuilder().build()
        PushNotificationUtils.generateFlightAlertNotification(context, 3213, "Your flight to SFO is delayed with a new arrival at 18:11.", "Flight delayed", "123123", flightData, "S_Push_Flight_gate_chang")

        assertEquals(emptyList(), getNotifications())
    }

    private fun getNotifications(): List<Notification> {
        return Select().from(Notification::class.java)
                .execute<Notification>()
    }

    private fun notificationAssertions(notification: Notification, uniqueId: String = "Push_123123", body: String = "Your flight to SFO is delayed with a new departure at 9:20PM.") {
        assertEquals(uniqueId, notification.uniqueId)
        assertEquals(body, notification.body)
        assertEquals(notification.body, notification.ticker)
        assertEquals("Flight delayed", notification.title)
        assertEquals(PushNotificationUtils.getLocStringForKey(context, "S_Push_Flight_delayed_title"), notification.title)
        assertEquals(Notification.FLAG_PUSH, notification.flags)
    }
}
