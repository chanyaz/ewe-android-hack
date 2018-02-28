package com.expedia.bookings.widget.itin

import android.content.Context
import android.text.format.DateUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelItinContentGeneratorTest {

    lateinit var context: Context
    lateinit var mTodayAtNoon: DateTime

    @Before
    fun before() {
        mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        context = RuntimeEnvironment.application
    }

    @After
    fun tearDown() {
        AbacusTestUtils.resetABTests()
    }

    @Test
    fun testIsDurationLongerThenInputsIsLonger() {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(10)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        assertTrue(hotelItinGenerator.isDurationLongerThanDays(3))
    }

    @Test
    fun testIsDurationLongerThenInputIsShorter() {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        assertFalse { hotelItinGenerator.isDurationLongerThanDays(3) }
    }

    @Test
    fun testCheckinNotificationExpirationTime() {
        val checkInTime = mTodayAtNoon.plusDays(10)
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        val checkinNotifcation = notifications.get(0)
        val dayAfter = itinCardDataHotel.startDate.toMutableDateTime()
        dayAfter.addDays(1)
        dayAfter.hourOfDay = 0
        dayAfter.minuteOfDay = 1
        assertTrue(checkinNotifcation.expirationTimeMillis < dayAfter.millis)
    }

    @Test
    fun testCheckoutNotificationExpirationTime() {
        val checkInTime = mTodayAtNoon.plusDays(10)
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        val checkoutNotifcation = notifications.get(1)
        val dayAfter = itinCardDataHotel.endDate.toMutableDateTime()
        dayAfter.addDays(1)
        dayAfter.hourOfDay = 0
        dayAfter.minuteOfDay = 1
        assertTrue(checkoutNotifcation.expirationTimeMillis < dayAfter.millis)
    }

    @Test
    fun testDoesCrossSellNotificationsFireAtRightTime() {
        val checkInTime = mTodayAtNoon.plusDays(10)
        val testTime = roundTime(checkInTime.minusDays(7))
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(notifications.get(3).triggerTimeMillis, testTime.millis)
    }

    @Test
    fun getReadyNotificationDoesNotShowTripsLessThanThreeDays() {
        val checkInTime = mTodayAtNoon.plusDays(7)
        val checkOutTime = mTodayAtNoon.plusDays(8)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, never()).generateGetReadyNotification()
    }

    @Test
    fun getReadyNotificationDoesShowTripsThreeDaysOrMore() {
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateGetReadyNotification()
    }

    @Test
    fun getReadyNotificationDoesNotShowTripsWithTwoOrLessTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, never()).generateGetReadyNotification()
    }

    @Test
    fun getReadyNotificationDoesShowTripsWithMoreThanTwoTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateGetReadyNotification()
    }

    @Test
    fun activityCrossNotificationDoesNotShowTripsLessThanThreeDays() {
        val checkInTime = mTodayAtNoon.plusDays(7)
        val checkOutTime = mTodayAtNoon.plusDays(8)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, never()).generateActivityCrossSellNotification()
    }

    @Test
    fun activityCrossNotificationDoesShowTripsThreeDaysOrMore() {
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityCrossSellNotification()
    }

    @Test
    fun activityCrossNotificationDoesNotShowTripsWithTwoOrLessTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, never()).generateActivityCrossSellNotification()
    }

    @Test
    fun nullCheckInAndOutForNotificationsTest() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val trip = itinCardDataHotel.tripComponent as TripHotel
        trip.checkOutTime = null
        trip.checkInTime = null
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()

        assertEquals(notifications[0].body, "Check in for your hotel booking for Orchard Hotel begins at " + JodaUtils.formatDateTime(context,
                itinCardDataHotel.startDate, DateUtils.FORMAT_SHOW_TIME) + " tomorrow. View your booking for details.")
        assertEquals(notifications[1].body, "Your check out time at Orchard Hotel is tomorrow at " + JodaUtils.formatDateTime(context,
                                itinCardDataHotel.endDate, DateUtils.FORMAT_SHOW_TIME) + ". Tap for details.")
    }

    @Test
    fun activityCrossNotificationDoesShowTripsWithMoreThanTwoTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityCrossSellNotification()
    }

    @Test
    fun activityInTripNotificationDoesShowTripsWhenDurationTwoDaysOrMore() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(5, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityInTripNotification()
        assertEquals(notifications[4].notificationType, Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP)
    }

    @Test
    fun activityInTripNotificationDoesNotShowTripsWhenDurationLessThanTwoDays() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(0)).generateActivityInTripNotification()
    }

    @Test
    fun activityInTripNotificationDoesNotShowWhenNotBucketed() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(0)).generateActivityInTripNotification()
    }

    @Test
    fun testDoesInTripNotificationFireAtRightTime() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val checkInTime = mTodayAtNoon.plusDays(10)
        val checkOutTime = mTodayAtNoon.plusDays(20)
        val lxNotificationTime = roundTime(checkInTime.plusHours(2))
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(notifications[4].triggerTimeMillis, lxNotificationTime.millis)
        assertEquals(notifications[4].notificationType, Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP)
    }

    @Test
    fun testNotificationExpTimings() {
        val checkInTime = mTodayAtNoon.minusDays(5)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        Assert.assertEquals(notifications[0].expirationTimeMillis, endOfDay(checkInTime).millis)
        Assert.assertEquals(notifications[1].expirationTimeMillis, endOfDay(checkOutTime).millis)
        Assert.assertEquals(notifications[2].expirationTimeMillis, endOfDay(checkInTime).millis)
        Assert.assertEquals(notifications[3].expirationTimeMillis, endOfDay(checkInTime).millis)
    }

    @Test
    fun testCheckinNotification() {
        val checkInTime = mTodayAtNoon.minusDays(3)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notification = makeHotelItinGenerator(happyItinCardDataHotel).generateNotifications().get(0)
        assertEquals(notification.title, "Hotel check in reminder")
        assertEquals(notification.body, "Check in for your hotel booking for " + happyItinCardDataHotel.propertyName
                + " begins at " + happyItinCardDataHotel.checkInTime + " tomorrow. View your booking for details.")
        val testTime = roundTime(checkInTime.minusDays(1))
        assertTrue(notification.triggerTimeMillis.equals(testTime.millis))
    }

    @Test
    fun testCheckoutMoreThan2dNotification() {
        val checkInTime = mTodayAtNoon.minusDays(4)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val generatorSpy = Mockito.spy(makeHotelItinGenerator(happyItinCardDataHotel))
        Mockito.`when`(generatorSpy.hasLastDayStarted()).thenReturn(false)
        val notification = generatorSpy.generateNotifications()[1]
        assertTrue(notification.title.contains("Check out tomorrow at "))
        assertTrue(notification.body.contains("Your check out time at " + happyItinCardDataHotel.propertyName))
        val testTime = roundTime(checkOutTime.minusDays(1))
        assertTrue(notification.triggerTimeMillis.equals(testTime.millis))
    }

    @Test
    fun testCheckout2dOrLessNotification() {
        val checkInTime = mTodayAtNoon.minusDays(2)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val generatorSpy = Mockito.spy(makeHotelItinGenerator(happyItinCardDataHotel))
        Mockito.`when`(generatorSpy.hasLastDayStarted()).thenReturn(true)
        val notification = generatorSpy.generateNotifications()[1]
        assertTrue(notification.title.contains("Check out today at "))
        assertTrue(notification.body.contains("Your check out time at " + happyItinCardDataHotel.propertyName))
        val testTime = roundTime(checkOutTime.minusHours(12))
        assertTrue(notification.triggerTimeMillis.equals(testTime.millis))
    }

    @Test
    fun testRoundTime() {
        val checkInTime = mTodayAtNoon.minusDays(2)
        val checkOutTime = mTodayAtNoon.minusDays(1).minusSeconds(-10)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        Assert.assertEquals(hotelItinGenerator.roundTime(checkOutTime), mTodayAtNoon.minusDays(1))
    }

    @Test
    fun testHasLastDayStarted() {
        val checkInTime = mTodayAtNoon.minusDays(2)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        assertTrue(hotelItinGenerator.hasLastDayStarted())
    }

    @Test
    fun generateHotelReviewNotificationTest() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppTripsUserReviews)
        val link = "www.expedia.com"
        val itinCardDataHotel = givenHappyItinCardDataHotel(reviewLink = link)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        val hotelReviewNotification = notifications.filter {
            it.notificationType == Notification.NotificationType.HOTEL_REVIEW
        }
        assertEquals(1, hotelReviewNotification.size)
        assertFalse(hotelReviewNotification[0].ticker.isNullOrEmpty())
        assertEquals(link, hotelReviewNotification[0].deepLink)
    }

    @Test
    fun generateHotelReviewNotificationWhenEmptyStringTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews)
        val link = ""
        val itinCardDataHotel = givenHappyItinCardDataHotel(reviewLink = link)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        val hotelReviewNotifications = notifications.filter {
            it.notificationType == Notification.NotificationType.HOTEL_REVIEW
        }
        assertEquals(0, hotelReviewNotifications.size)
    }

    @Test
    fun generateHotelReviewNotificationValidStringNotBucketedTest() {
        val link = "www.expedia.com"
        val itinCardDataHotel = givenHappyItinCardDataHotel(reviewLink = link)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val notifications = hotelItinGenerator.generateNotifications()
        val hotelReviewNotifications = notifications.filter {
            it.notificationType == Notification.NotificationType.HOTEL_REVIEW
        }
        assertEquals(0, hotelReviewNotifications.size)
    }

    private fun makeHotelItinGenerator(itinCardDataHotel: ItinCardDataHotel): HotelItinContentGenerator {
        return HotelItinContentGenerator(context, itinCardDataHotel, null)
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
        roundedTime.setZoneRetainFields(DateTimeZone.getDefault())
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
