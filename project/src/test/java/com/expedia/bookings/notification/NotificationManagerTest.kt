package com.expedia.bookings.notification

import android.app.AlarmManager
import android.content.Context
import com.activeandroid.ActiveAndroid
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class NotificationManagerTest {
    lateinit var context: Context
    lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        ActiveAndroid.initialize(context)
        notificationManager = NotificationManager(context)
    }

    @After
    fun tearDown() {
        ActiveAndroid.dispose()
    }

    @Test
    fun testScheduleNotification() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        assertNull(shadowAlarmManager.nextScheduledAlarm)
        val notification = makeNotification("testScheduleNotification", Notification.NotificationType.ACTIVITY_START)
        notificationManager.scheduleNotification(notification)
        assertNotNull(shadowAlarmManager.nextScheduledAlarm)
    }

    @Test
    fun testCancelNotification() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        assertNull(shadowAlarmManager.nextScheduledAlarm)
        val notification = makeNotification("testCancelNotification", Notification.NotificationType.ACTIVITY_START)
        shadowAlarmManager.set(AlarmManager.RTC_WAKEUP, 1000, NotificationReceiver.generateSchedulePendingIntent(context, notification))
        assertNotNull(shadowAlarmManager.nextScheduledAlarm)
        notificationManager.cancelNotification(notification)
        assertNull(shadowAlarmManager.nextScheduledAlarm)
    }

    /*@Test
    fun testFindExisting() {
        val notification = makeNotification("testFindExisting", Notification.NotificationType.ACTIVITY_START)
        notification.save()
        assertEquals(notification, notificationManager.findExisting(notification))
    }*/

    /*@Test
    fun testFindExistingReturnsNull() {
        val notification = makeNotification("testFindExisting", Notification.NotificationType.ACTIVITY_START)
        assertNull(notificationManager.findExisting(notification))
    }*/

    /*@Test
    fun testHasExisting() {
        val notification = makeNotification("testHasExisting", Notification.NotificationType.ACTIVITY_START)
        notification.save()
        assertEquals(true, notificationManager.hasExisting(notification))
    }*/

    /*@Test
    fun testHasExistingReturnsFalse() {
        val notification = makeNotification("testHasExisting", Notification.NotificationType.ACTIVITY_START)
        assertEquals(false, notificationManager.hasExisting(notification))
    }*/

    /*@Test
    fun testDismissExisting() {
        val notification = makeNotification("testDismissExisting", Notification.NotificationType.ACTIVITY_START)
        notification.save()
        assertEquals(Notification.StatusType.NEW, notification.status)
        notificationManager.dismissExisting(notification)
        val notificationAfterDismissal = notificationManager.findExisting(notification)
        assertEquals(Notification.StatusType.DISMISSED, notificationAfterDismissal?.status)
    }*/

    /*@Test
    fun testScheduleAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertNull(shadowAlarmManager.nextScheduledAlarm)

        val notification = makeNotification("testScheduleAll", Notification.NotificationType.ACTIVITY_START)
        notification.save()
        val notification2 = makeNotification("testScheduleAll2", Notification.NotificationType.ACTIVITY_START)
        notification2.save()
        val notification3 = makeNotification("testScheduleAll3", Notification.NotificationType.ACTIVITY_START)
        notification3.save()
        notificationManager.scheduleAll()
        assertEquals(3, shadowAlarmManager.scheduledAlarms.size)
    }*/

    /*@Test
    fun testCancelExpired() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertNull(shadowAlarmManager.nextScheduledAlarm)

        val notification = makeNotification("testCancelExpired", Notification.NotificationType.ACTIVITY_START, Notification.StatusType.NEW, (System.currentTimeMillis() - 10000))
        notification.save()
        notificationManager.scheduleNotification(notification)
        val notification2 = makeNotification("testCancelExpired2", Notification.NotificationType.ACTIVITY_START, Notification.StatusType.NOTIFIED, (System.currentTimeMillis() - 10000))
        notification2.save()
        notificationManager.scheduleNotification(notification2)
        val notification3 = makeNotification("testCancelExpired3", Notification.NotificationType.ACTIVITY_START, Notification.StatusType.NEW)
        notification3.save()
        notificationManager.scheduleNotification(notification3)
        assertEquals(3, shadowAlarmManager.scheduledAlarms.size)

        notificationManager.cancelAllExpired()
        assertEquals(Notification.StatusType.EXPIRED, notificationManager.findExisting(notification)?.status)
        assertEquals(Notification.StatusType.EXPIRED, notificationManager.findExisting(notification2)?.status)
        assertEquals(Notification.StatusType.NEW, notificationManager.findExisting(notification3)?.status)
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)
    }*/

    /*@Test
    fun testDeleteAll() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertNull(shadowAlarmManager.nextScheduledAlarm)

        val notification = makeNotification("testDeleteAll", Notification.NotificationType.ACTIVITY_START)
        notification.save()
        notificationManager.scheduleNotification(notification)
        val notification2 = makeNotification("testDeleteAll2", Notification.NotificationType.ACTIVITY_START)
        notification2.save()
        notificationManager.scheduleNotification(notification2)
        assertEquals(2, shadowAlarmManager.scheduledAlarms.size)

        notificationManager.deleteAll()
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
        assertNull(notificationManager.findExisting(notification))
        assertNull(notificationManager.findExisting(notification2))
    }

    @Test
    fun testdeleteAllWithItinId() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertNull(shadowAlarmManager.nextScheduledAlarm)

        val notification = makeNotification("testDeleteAll", Notification.NotificationType.ACTIVITY_START)
        notification.save()
        notificationManager.scheduleNotification(notification)
        val notification2 = makeNotification("testDeleteAll2", Notification.NotificationType.ACTIVITY_START)
        notification2.save()
        notificationManager.scheduleNotification(notification2)
        assertEquals(2, shadowAlarmManager.scheduledAlarms.size)

        notificationManager.deleteAll("testDeleteAll2")
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)
        assertNotNull(notificationManager.findExisting(notification))
        assertNull(notificationManager.findExisting(notification2))
    }*/

    private fun makeNotification(id: String,
                                 type: Notification.NotificationType,
                                 status: Notification.StatusType = Notification.StatusType.NEW,
                                 time: Long = System.currentTimeMillis() + 10000): Notification {
        val notification = Notification()
        notification.uniqueId = id
        notification.itinId = id
        notification.notificationType = type
        notification.status = status
        notification.expirationTimeMillis = time
        return notification
    }
}