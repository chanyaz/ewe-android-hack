package com.expedia.bookings.notification

import com.activeandroid.ActiveAndroid
import com.expedia.bookings.R
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

    @Before
    fun setup() {
        ActiveAndroid.initialize(RuntimeEnvironment.application)
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
}
