package com.expedia.bookings.notification

import android.app.AlarmManager
import android.content.Context
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.activeandroid.ActiveAndroid
import com.activeandroid.query.Select
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(
        constants = BuildConfig::class,
        application = ActiveAndroidNotifcationTestApp::class,
        sdk = [23]
)
class NotificationManagerTest {
    lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var frozenTime: DateTime

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        ActiveAndroid.initialize(context)
        notificationManager = NotificationManager(context)
        frozenTime = DateTime.now().plusDays(1).withTimeAtStartOfDay().plusHours(12)
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
    fun testCancelNotificationIntent() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertNull(shadowAlarmManager.nextScheduledAlarm)
        val notification = makeNotification("testCancelNotification", Notification.NotificationType.ACTIVITY_START)
        shadowAlarmManager.set(AlarmManager.RTC_WAKEUP, 1000, NotificationReceiver.generateSchedulePendingIntent(context, notification))
        assertNotNull(shadowAlarmManager.nextScheduledAlarm)
        notificationManager.cancelNotificationIntent(notification)
        assertNull(shadowAlarmManager.nextScheduledAlarm)
    }

    @RequiresApi(23)
    @Test
    fun testDismissNotification() {
        val testId = "123abc"
        val notificationA = makeNotification(testId, Notification.NotificationType.HOTEL_CHECK_OUT)
        val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        assertEquals(0, mNotifyMgr.activeNotifications.size)
        val testBuilder = NotificationCompat.Builder(context, "")
        mNotifyMgr.notify(testId, 0, testBuilder.build())
        assertEquals(1, mNotifyMgr.activeNotifications.size)
        assertEquals(testId, mNotifyMgr.activeNotifications[0].tag)
        notificationManager.dismissNotification(notificationA)
        assertEquals(0, mNotifyMgr.activeNotifications.size)
    }

    @Test
    fun testSearchForExistingAndUpdate() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        assertEquals(1, getNotifications().size)
        assertEquals(Notification.StatusType.NOTIFIED, notificationManager.findExisting(notificationB)?.status)
    }

    @Test
    fun testSearchForExistingAndUpdateTitleChange() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT, title = "newTitle")
        notificationB.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        assertEquals(1, getNotifications().size)
        assertEquals(Notification.StatusType.NEW, getNotifications()[0].status)
        assertEquals(Notification.StatusType.NEW, notificationManager.findExisting(notificationB)?.status)
    }

    @Test
    fun testSearchForExistingAndUpdateTickerChange() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT, ticker = "test")
        notificationB.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        notificationB.save()
        assertEquals(1, getNotifications().size)
        assertEquals(Notification.StatusType.NEW, notificationB.status)
        assertEquals(Notification.StatusType.NEW, notificationManager.findExisting(notificationB)?.status)
    }
    @Test
    fun testSearchForExistingAndUpdateBodyChange() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT, body = "test")
        notificationB.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        notificationB.save()
        assertEquals(1, getNotifications().size)
        assertEquals(Notification.StatusType.NEW, notificationB.status)
        assertEquals(Notification.StatusType.NEW, notificationManager.findExisting(notificationB)?.status)
    }
    @Test
    fun testSearchForExistingAndUpdateTriggerChange() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT, trigger = frozenTime.millis + 2000)
        notificationB.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        assertEquals(1, getNotifications().size)
        assertEquals(Notification.StatusType.NEW, notificationB.status)
        assertEquals(Notification.StatusType.NEW, notificationManager.findExisting(notificationB)?.status)
    }

    @Test
    fun testSearchForExistingAndUpdateExpPastChange() {
        val yesterday = frozenTime.minusDays(2).millis
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT, time = yesterday)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT, time = yesterday - 20000)
        notificationB.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        assertEquals(1, getNotifications().size)
        assertEquals(Notification.StatusType.NOTIFIED, notificationManager.findExisting(notificationB)?.status)
    }

    @Test
    fun testSearchForExistingAndUpdateUniqueIDChange() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationA.status = Notification.StatusType.NOTIFIED
        notificationA.save()
        val notificationB = makeNotification("noteB", Notification.NotificationType.HOTEL_CHECK_OUT)
        notificationB.status = Notification.StatusType.NEW
        assertTrue(notificationManager.hasExisting(notificationA))
        assertEquals(1, getNotifications().size)
        notificationManager.searchForExistingAndUpdate(notificationB)
        assertTrue(notificationManager.hasExisting(notificationB))
        assertEquals(2, getNotifications().size)
        assertEquals(Notification.StatusType.NEW, getNotifications()[1].status)
        assertEquals(Notification.StatusType.NEW, notificationManager.findExisting(notificationB)?.status)
    }

    @Test
    fun testNotificationOtherCases() {
        val notificationA = makeNotification("noteA", Notification.NotificationType.HOTEL_CHECK_OUT)
        val notificationB = makeNotification("noteB", Notification.NotificationType.HOTEL_CHECK_OUT)
        assertNotNull(notificationA)
        assertNotEquals(notificationA, notificationB)
    }

    @Test
    fun testScheduleAll() {
        val notificationA = makeNotification(id = "A123",
                type = Notification.NotificationType.FLIGHT_CHECK_IN, time = frozenTime.plusDays(1).millis + 1000)
        val notificationB = makeNotification(id = "B123",
                type = Notification.NotificationType.FLIGHT_CHECK_IN, time = frozenTime.plusDays(1).millis + 1000)
        val notificationC = makeNotification(id = "C123", type = Notification.NotificationType.FLIGHT_CHECK_IN,
                time = frozenTime.plusDays(1).millis + 1000)
        val notificationD = makeNotification(id = "D123", type = Notification.NotificationType.FLIGHT_CHECK_IN,
                time = frozenTime.plusDays(1).millis + 1000)
        val notificationE = makeNotification(id = "E123", type = Notification.NotificationType.FLIGHT_CHECK_IN,
                time = frozenTime.plusDays(1).millis + 1000)
        notificationA.save()
        notificationB.save()
        notificationC.save()
        notificationD.save()
        notificationE.save()
        assertEquals(5, getNotifications().size)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
        notificationManager.scheduleAll()
        assertEquals(5, shadowAlarmManager.scheduledAlarms.size)
    }

    private fun makeNotification(
        id: String,
        type: Notification.NotificationType,
        status: Notification.StatusType = Notification.StatusType.NEW,
        time: Long = frozenTime.millis,
        title: String = "",
        ticker: String = "",
        body: String = "",
        trigger: Long = frozenTime.minusHours(1).millis
    ): Notification {
        val notification = Notification()
        notification.uniqueId = id
        notification.triggerTimeMillis = trigger
        notification.itinId = id
        notification.notificationType = type
        notification.status = status
        notification.expirationTimeMillis = time
        notification.title = title
        notification.ticker = ticker
        notification.body = body
        return notification
    }

    private fun getNotifications(): List<Notification> {
        return Select().from(Notification::class.java).execute()
    }
}
