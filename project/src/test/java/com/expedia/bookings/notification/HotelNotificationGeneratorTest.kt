package com.expedia.bookings.notification

import android.content.Context
import android.text.format.DateUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.utils.StringProvider
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelNotificationGeneratorTest {
    lateinit var sut: HotelNotificationGenerator
    lateinit var mTodayAtNoon: DateTime
    lateinit var mMidNight: DateTime
    lateinit var context: Context

    @Before
    fun setup() {
        mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        mMidNight = DateTime.now().plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        context = RuntimeEnvironment.application
        val notificationManagerMock: INotificationManager = makeMockNotificationManager()
        sut = HotelNotificationGenerator(context, stringProvider = StringProvider(context), notificationManager = notificationManagerMock)
    }

    @Test
    fun testIsDurationLongerThenInputsIsLonger() {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(10)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        assertTrue(sut.isDurationLongerThanDays(3, itinCardDataHotel))
    }

    @Test
    fun testIsDurationLongerThenInputIsShorter() {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        assertFalse(sut.isDurationLongerThanDays(3, itinCardDataHotel))
    }

    @Test
    fun testCheckinNotificationExpirationTime() {
        val checkInTime = mTodayAtNoon.plusDays(10)
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val checkinNotification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_CHECK_IN }
        val dayAfter = itinCardDataHotel.startDate.toMutableDateTime()
        dayAfter.addDays(1)
        dayAfter.hourOfDay = 0
        dayAfter.minuteOfDay = 1
        assertTrue(checkinNotification!!.expirationTimeMillis < dayAfter.millis)
    }

    @Test
    fun testCheckoutNotificationExpirationTime() {
        val checkInTime = mTodayAtNoon.plusDays(10)
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val checkoutNotifcation = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_CHECK_OUT }
        val dayAfter = itinCardDataHotel.endDate.toMutableDateTime()
        dayAfter.addDays(1)
        dayAfter.hourOfDay = 0
        dayAfter.minuteOfDay = 1
        assertTrue(checkoutNotifcation!!.expirationTimeMillis < dayAfter.millis)
    }

    @Test
    fun getReadyNotificationDoesNotShowTripsLessThanThreeDays() {
        val checkInTime = mTodayAtNoon.plusDays(7)
        val checkOutTime = mTodayAtNoon.plusDays(8)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val possibleNotification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_GET_READY }
        assertNull(possibleNotification)
    }

    @Test
    fun getReadyNotificationDoesShowTripsThreeDaysOrMore() {
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val possibleNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_GET_READY }
        assertNotNull(possibleNotification)
    }

    @Test
    fun getReadyNotificationDoesNotShowTripsWithTwoOrLessTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val possibleNotification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_GET_READY }
        assertNull(possibleNotification)
    }

    @Test
    fun getReadyNotificationDoesShowTripsWithMoreThanTwoTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        assertEquals(3, notifications.size)
        val possibleNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_GET_READY }
        assertNotNull(possibleNotification)
    }

    @Test
    fun nullCheckInAndOutForNotificationsTest() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val trip = itinCardDataHotel.tripComponent as TripHotel
        trip.checkOutTime = null
        trip.checkInTime = null
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val checkInNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_CHECK_IN }
        val checkOutNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_CHECK_OUT }
        assertEquals(checkInNotification.body, "Check in for your hotel booking for Orchard Hotel begins at " + JodaUtils.formatDateTime(context,
                itinCardDataHotel.startDate, DateUtils.FORMAT_SHOW_TIME) + " tomorrow. View your booking for details.")
        assertEquals(checkOutNotification.body, "Your check out time at Orchard Hotel is tomorrow at " + JodaUtils.formatDateTime(context,
                itinCardDataHotel.endDate, DateUtils.FORMAT_SHOW_TIME) + ". Tap for details.")
    }

    @Test
    fun activityInTripNotificationDoesShowTripsWhenDurationTwoDaysOrMore() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val possibleNotification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP }
        assertNotNull(possibleNotification)
    }

    @Test
    fun activityInTripNotificationDoesNotShowTripsWhenDurationLessThanTwoDays() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val possibleNotification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP }
        assertNull(possibleNotification)
    }

    @Test
    fun activityInTripNotificationDoesNotShowWhenNotBucketed() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val possibleNotification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP }
        assertNull(possibleNotification)
    }

    @Test
    fun testDoesInTripNotificationFireAtRightTime() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val checkInTime = mTodayAtNoon.plusDays(10)
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val lxNotificationTime = roundTime(checkInTime.plusHours(2))
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val notifcation = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP }
        assertEquals(notifcation.triggerTimeMillis, lxNotificationTime.millis)
    }

    @Test
    fun testNotificationExpTimings() {
        val checkInTime = mTodayAtNoon.plusDays(5)
        val checkOutTime = mTodayAtNoon.plusDays(8)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val checkInNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_CHECK_IN }
        val checkOutNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_CHECK_OUT }
        val getReadyNotification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_GET_READY }
        assertEquals(checkInNotification.expirationTimeMillis, endOfDay(checkInTime).millis)
        assertEquals(checkOutNotification.expirationTimeMillis, endOfDay(checkOutTime).millis)
        assertEquals(getReadyNotification.expirationTimeMillis, endOfDay(checkInTime).millis)
    }

    @Test
    fun testAllReadyFired() {
        val notificationManagerMock = makeMockNotificationManager(true)
        val newSut = HotelNotificationGenerator(context, stringProvider = StringProvider(context), notificationManager = notificationManagerMock)
        val checkInTime = mTodayAtNoon.plusDays(5)
        val checkOutTime = mTodayAtNoon.plusDays(8)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppTripsUserReviews)
        val link = "www.expedia.com"
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime, reviewLink = link)
        val notifications = newSut.generateNotifications(itinCardDataHotel)
        assertEquals(0, notifications.size)
    }

    @Test
    fun testCheckinNotification() {
        val checkInTime = mTodayAtNoon.plusDays(3)
        val checkOutTime = mTodayAtNoon.plusDays(4)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(happyItinCardDataHotel)
        val notification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_CHECK_IN }
        assertEquals(notification!!.title, "Hotel check in reminder")
        assertEquals(notification.body, "Check in for your hotel booking for " + happyItinCardDataHotel.propertyName
                + " begins at " + happyItinCardDataHotel.checkInTime + " tomorrow. View your booking for details.")
        val testTime = roundTime(checkInTime.minusDays(1))
        assertTrue(notification.triggerTimeMillis.equals(testTime.millis))
    }

    @Test
    fun testCheckoutMoreThan2dNotification() {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(10)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(happyItinCardDataHotel)
        val notification = notifications.first { it.notificationType == Notification.NotificationType.HOTEL_CHECK_OUT }
        assertTrue(notification.title.contains("Check out tomorrow at "))
        assertTrue(notification.body.contains("Your check out time at " + happyItinCardDataHotel.propertyName))
        val testTime = roundTime(checkOutTime.minusDays(1))
        assertTrue(notification.triggerTimeMillis.equals(testTime.millis))
    }

    @Test
    fun testCheckout2dOrLessNotification() {
        val checkInTime = mTodayAtNoon
        val checkOutTime = mMidNight.minusMinutes(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notifications = sut.generateNotifications(happyItinCardDataHotel)
        val notification = notifications.firstOrNull { it.notificationType == Notification.NotificationType.HOTEL_CHECK_OUT }
        assertTrue(notification!!.title.contains("Check out today at "))
        assertTrue(notification.body.contains("Your check out time at " + happyItinCardDataHotel.propertyName))
        val testTime = roundTime(checkOutTime.minusHours(12))
        assertTrue(notification.triggerTimeMillis.equals(testTime.millis))
    }

    @Test
    fun testRoundTime() {
        val checkOutTime = mTodayAtNoon.minusDays(1).minusSeconds(-10)
        assertEquals(sut.roundTime(checkOutTime), mTodayAtNoon.minusDays(1))
    }

    @Test
    fun testHasLastDayStarted() {
        val checkInTime = mTodayAtNoon.minusDays(2)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        assertTrue(sut.hasLastDayStarted(itinCardDataHotel))
    }

    @Test
    fun generateHotelReviewNotificationTest() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppTripsUserReviews)
        val link = "www.expedia.com"
        val itinCardDataHotel = givenHappyItinCardDataHotel(reviewLink = link)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val hotelReviewNotification = notifications.first {
            it.notificationType == Notification.NotificationType.HOTEL_REVIEW
        }
        assertFalse(hotelReviewNotification.ticker.isNullOrEmpty())
        assertEquals(link, hotelReviewNotification.deepLink)
    }

    @Test
    fun generateHotelReviewNotificationWhenEmptyStringTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews)
        val link = ""
        val itinCardDataHotel = givenHappyItinCardDataHotel(reviewLink = link)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val hotelReviewNotification = notifications.firstOrNull {
            it.notificationType == Notification.NotificationType.HOTEL_REVIEW
        }
        assertNull(hotelReviewNotification)
    }

    @Test
    fun generateHotelReviewNotificationValidStringNotBucketedTest() {
        val link = "www.expedia.com"
        val itinCardDataHotel = givenHappyItinCardDataHotel(reviewLink = link)
        val notifications = sut.generateNotifications(itinCardDataHotel)
        val hotelReviewNotification = notifications.firstOrNull {
            it.notificationType == Notification.NotificationType.HOTEL_REVIEW
        }
        assertNull(hotelReviewNotification)
    }

    private fun makeMockNotificationManager(fired: Boolean = false): INotificationManager {
        val mockManager = mock(INotificationManager::class.java)
        `when`(mockManager.wasFired(ArgumentMatchers.anyString())).thenReturn(fired)
        return mockManager
    }

    private fun givenHappyItinCardDataHotel(checkIn: DateTime = DateTime.now().plusDays(2), checkOut: DateTime? = null, reviewLink: String = ""): ItinCardDataHotel {
        val itinCardDataHotel = ItinCardDataHotelBuilder()
                .withCheckInDate(checkIn)
                .withCheckOutDate(checkOut)
                .withReviewLink(reviewLink).build()
        return itinCardDataHotel
    }

    private fun givenHappyItinCardDataHotel(traveler: Int): ItinCardDataHotel {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = ItinCardDataHotelBuilder()
                .withAdultCount(traveler)
                .withCheckInDate(checkInTime)
                .withCheckOutDate(checkOutTime).build()
        return itinCardDataHotel
    }

    private fun roundTime(time: DateTime): DateTime {
        var roundedTime = time.toMutableDateTime()
        roundedTime.setRounding(roundedTime.chronology.minuteOfHour())
        return roundedTime.toDateTime()
    }

    private fun endOfDay(time: DateTime): DateTime {
        val mutedTime = roundTime(time).toMutableDateTime()
        mutedTime.hourOfDay = 23
        mutedTime.minuteOfHour = 59
        return mutedTime.toDateTime()
    }
}
