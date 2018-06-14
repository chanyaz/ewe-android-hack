package com.expedia.bookings.notification

import android.content.Context
import com.activeandroid.ActiveAndroid
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.ItineraryManagerInterface
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PushNotificationUtilsV2Test {
    val sut = PushNotificationUtilsV2
    val context: Context = RuntimeEnvironment.application
    lateinit var mockOldAccessor: MockAccessor

    @Before
    fun setup() {
        ActiveAndroid.initialize(RuntimeEnvironment.application)
        mockOldAccessor = MockAccessor()
    }

    @Test
    fun generateFlightAlertWithNoLocalizationNotificationNullItinCard() {
        val mockManager = MockNotifcationManager()
        val body = "BODY"
        val title = "TITLE"
        val nID = "123"
        val type = "S_PUSH_BLAH_BLAH"
        assertFalse(mockManager.sheduleNotificationCalled)
        sut.generateFlightAlertWithNoLocalizationNotification(mockManager, body, title, nID, null, type)
        assertTrue(mockManager.sheduleNotificationCalled)

        val notification = mockManager.notification
        assertEquals("Push_123", notification.uniqueId)
        assertEquals("-1", notification.itinId)
        assertEquals(Notification.NotificationType.FLIGHT_ALERT, notification.notificationType)
        assertEquals(Notification.FLAG_PUSH, notification.flags)
        assertEquals(R.drawable.ic_stat_flight, notification.iconResId)
        assertEquals(Notification.ImageType.NONE, notification.imageType)
        assertEquals(body, notification.body)
        assertEquals(body, notification.ticker)
        assertEquals(title, notification.title)
        assertEquals("S_PUSH_BLAH_BLAH", notification.templateName)
    }

    @Test
    fun sanitizeUniqueIdWhiteSpaceTest() {
        var inputString = "123 4567"
        val expectedString = "1234567"
        var outputString = sut.sanitizeUniqueId(inputString)
        assertEquals(expectedString, outputString)

        inputString = "1\t\t23 45 6 7   "
        outputString = sut.sanitizeUniqueId(inputString)
        assertEquals(expectedString, outputString)
    }

    @Test
    fun sanitizeUniqueIdLargeTest() {
        var maxCount = 1024
        var inputString = buildString(maxCount)
        assertEquals(1024, inputString.length)
        var outputString = sut.sanitizeUniqueId(inputString)
        assertEquals(1024, outputString.length)

        maxCount = 1024
        inputString = buildString(maxCount) + "hi"
        assertTrue(inputString.contains("hi"))
        assertEquals(1026, inputString.length)
        outputString = sut.sanitizeUniqueId(inputString)
        assertEquals(1024, outputString.length)
        assertFalse(outputString.contains("hi"))

        maxCount = 1025
        inputString = buildString(maxCount)
        assertEquals(1025, inputString.length)
        outputString = sut.sanitizeUniqueId(inputString)
        assertEquals(1024, outputString.length)

        maxCount = 1023
        inputString = buildString(maxCount)
        assertEquals(1023, inputString.length)
        outputString = sut.sanitizeUniqueId(inputString)
        assertEquals(1023, outputString.length)
    }

    @Test
    fun testNotificationDisplayedForBooking() {
        val mockNotificationManager = MockNotifcationManager()
        mockOldAccessor.locKeyForDesktopBooking = true
        assertFalse(mockOldAccessor.wasGenerateDesktopBookingNotificationCalled)
        sut.generateNotification(context, 1, "body", emptyArray<String>(), "", "1", "", MockItineraryManagerInterface(), mockNotificationManager, mockOldAccessor)
        assertTrue(mockOldAccessor.wasGenerateDesktopBookingNotificationCalled)
    }

    private fun buildString(maxCount: Int): String {
        val inputStringBuilder = StringBuilder()
        for (i in 0 until maxCount) {
            inputStringBuilder.append(1)
        }
        return inputStringBuilder.toString()
    }

    @After
    fun tearDown() {
        ActiveAndroid.dispose()
    }

    class MockItineraryManagerInterface : ItineraryManagerInterface {
        override fun removeSyncListener(listener: ItineraryManager.ItinerarySyncListener) {
        }

        override fun getTripComponentFromFlightHistoryId(id: Int): TripFlight? {
            return null
        }

        override fun isSyncing(): Boolean {
            return true
        }

        override fun startSync(boolean: Boolean): Boolean {
            return false
        }

        override fun deepRefreshTrip(key: String, doSyncIfNotFound: Boolean): Boolean {
            return false
        }

        override fun getItinCardDataFromFlightHistoryId(fhid: Int): ItinCardData? {
            return null
        }

        override fun addSyncListener(listener: ItineraryManager.ItinerarySyncListener) {
        }

        override fun getItinCardDataFromItinId(id: String?): ItinCardData? {
            return null
        }
    }

    class MockNotifcationManager : INotificationManager {
        lateinit var notification: Notification
        var sheduleNotificationCalled = false
        override fun scheduleAll() {
        }

        override fun cancelAllExpired() {
        }

        override fun searchForExistingAndUpdate(notification: Notification) {
        }

        override fun wasFired(uniqueId: String): Boolean {
            return true
        }

        override fun scheduleNotification(notification: Notification) {
            this.notification = notification
            sheduleNotificationCalled = true
        }

        override fun cancelNotificationIntent(notification: Notification) {
        }

        override fun cancelAndDeleteNotification(notification: Notification) {
        }

        override fun dismissNotification(notification: Notification) {
        }

        override fun findExisting(notification: Notification): Notification? {
            return Notification()
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
    }

    class MockAccessor : IPushNotifcationUtilAccessor {
        var hasLocKeyForNewFlightAlertsReturn = false
        var locKeyForDesktopBooking = false
        var wasGenerateFlightAlertNotificationCalled = false
        var wasGenerateDesktopBookingNotificationCalled = false
        override fun hasLocKeyForNewFlightAlerts(locKey: String): Boolean {
            return hasLocKeyForNewFlightAlertsReturn
        }

        override fun generateFlightAlertNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?, titleArg: String, nID: String, data: ItinCardDataFlight) {
            wasGenerateFlightAlertNotificationCalled = true
        }

        override fun locKeyForDesktopBooking(locKey: String): Boolean {
            return locKeyForDesktopBooking
        }

        override fun generateDesktopBookingNotification(context: Context, fhid: Int, locKey: String, locKeyArgs: Array<String>?) {
            wasGenerateDesktopBookingNotificationCalled = true
        }
    }
}
