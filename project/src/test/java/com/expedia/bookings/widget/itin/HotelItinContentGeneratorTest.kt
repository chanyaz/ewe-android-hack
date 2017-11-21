package com.expedia.bookings.widget.itin

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.spy
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelItinContentGeneratorTest {

    lateinit private var activity: PhoneLaunchActivity
    lateinit var context: Context
    lateinit var mTodayAtNoon: DateTime

    @Before
    fun before() {
        mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
        context = RuntimeEnvironment.application
    }

    @Test
    fun hotelSoftChangeButtonOpensWebView() {

        val itinCardDataHotel = ItinCardDataHotelBuilder()
                .withBookingChangeUrl(getBookingChangeUrl())
                .build()
        itinCardDataHotel.tripComponent.parentTrip.setIsShared(false)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        val editRoomInfoButton = detailsView.findViewById<View>(R.id.edit_hotel_room_info)
        editRoomInfoButton.performClick()

        val shadowActivity = shadowOf(activity)
        val nextStartedActivityForResult = shadowActivity.nextStartedActivityForResult
        val intent = nextStartedActivityForResult.intent
        val intentUrl = intent.getStringExtra("ARG_URL")
        val webViewTitle = intent.getStringExtra("ARG_TITLE")
        val isWebViewSendingCookies = intent.getBooleanExtra("ARG_INJECT_EXPEDIA_COOKIES", false)
        val tripNumberToRefresh = intent.getStringExtra(Constants.ITIN_SOFT_CHANGE_TRIP_ID)
        val resultExtra = intent.getBooleanExtra("ARG_RETURN_FROM_SOFT_CHANGE_ROOM_BOOKING", false)

        assertEquals("com.expedia.bookings.activity.WebViewActivity", intent.component.className)
        assertEquals(Constants.ITIN_SOFT_CHANGE_WEBPAGE_CODE, nextStartedActivityForResult.requestCode)
        // note: WebViewActivity adds adobe_mc= param, hence contains() (not equals()) here
        assertTrue(intentUrl.contains(getBookingChangeUrl()))
        assertEquals("Edit Room Info", webViewTitle)
        assertTrue(isWebViewSendingCookies)
        assertEquals("1103274148635", tripNumberToRefresh)
        assertTrue(resultExtra)
    }

    @Test
    fun hotelRoomUpgradeButtonOpensWebView() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)


        val itinCardDataHotel = ItinCardDataHotelBuilder().isSharedItin(false)
                .withRoomUpgradeWebUrl(getRoomUpgradeWebUrl())
                .build()
        itinCardDataHotel.property.roomUpgradeOfferType = Property.RoomUpgradeType.HAS_UPGRADE_OFFERS
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        val roomUpgradeButton = detailsView.findViewById<View>(R.id.room_upgrade_button)
        roomUpgradeButton.performClick()

        val shadowActivity = shadowOf(activity)
        val nextStartedActivityForResult = shadowActivity.nextStartedActivityForResult
        val intent = nextStartedActivityForResult.intent
        val intentUrl = intent.getStringExtra("ARG_URL")
        val webViewTitle = intent.getStringExtra("ARG_TITLE")
        val isWebViewSendingCookies = intent.getBooleanExtra("ARG_INJECT_EXPEDIA_COOKIES", false)
        val tripNumberToRefresh = intent.getStringExtra(Constants.ITIN_ROOM_UPGRADE_TRIP_ID)

        assertEquals("com.expedia.bookings.activity.WebViewActivity", intent.component.className)
        assertEquals(Constants.ITIN_ROOM_UPGRADE_WEBPAGE_CODE, nextStartedActivityForResult.requestCode)
        // note: WebViewActivity adds adobe_mc= param, hence contains() (not equals()) here
        assertTrue(intentUrl.contains(getRoomUpgradeWebUrl()))
        assertEquals("Upgrade hotel room", webViewTitle)
        assertTrue(isWebViewSendingCookies)
        assertEquals("1103274148635", tripNumberToRefresh)
    }

    @Test
    fun hotelSoftChangeButtonAvailable() {

        val itinCardDataHotel = ItinCardDataHotelBuilder().withBookingChangeUrl(getBookingChangeUrl()).build()
        itinCardDataHotel.tripComponent.parentTrip.setIsShared(false)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        assertEquals(View.VISIBLE, detailsView.findViewById<View>(R.id.edit_hotel_room_info).visibility)
    }

    @Test
    fun hotelSoftChangeButtonGoneForSharedItin() {

        val itinCardDataHotel = ItinCardDataHotelBuilder().isSharedItin(true).build()
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        assertEquals(View.GONE, detailsView.findViewById<View>(R.id.edit_hotel_room_info).visibility)
    }

    @Test
    fun roomUpgradeButtonVisible() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)

        val itinCardDataHotel = ItinCardDataHotelBuilder().isSharedItin(false)
                .withRoomUpgradeWebUrl(getRoomUpgradeWebUrl()).build()
        itinCardDataHotel.property.roomUpgradeOfferType = Property.RoomUpgradeType.HAS_UPGRADE_OFFERS
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        assertEquals(View.VISIBLE, detailsView.findViewById<View>(R.id.room_upgrade_button).visibility)
    }

    @Test
    fun roomUpgradeButtonGoneFeatureOff() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)

        val itinCardDataHotel = ItinCardDataHotelBuilder().build()
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        assertEquals(View.GONE, detailsView.findViewById<View>(R.id.room_upgrade_button).visibility)
    }

    @Test
    fun roomUpgradeButtonGoneForSharedItin() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelUpgrade)

        val itinCardDataHotel = ItinCardDataHotelBuilder().isSharedItin(true).build()
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        val container = FrameLayout(activity)
        val detailsView = hotelItinGenerator.getDetailsView(null, container)

        assertEquals(View.GONE, detailsView.findViewById<View>(R.id.room_upgrade_button).visibility)
    }

    @Test
    fun testDontShowCancelLinkPastCheckInDate() {
        val oldCheckInDate = DateTime.now().minusHours(1)
        val itinCardDataHotel = givenHappyItinCardDataHotel(oldCheckInDate, null)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        Assert.assertTrue(hotelItinGenerator.itinCardData.isPastCheckInDate)
    }

    @Test
    fun testSummaryCheckInFuture4d() {
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_in_day_TEMPLATE,
                itinCardDataHotel.getFormattedDetailsCheckInDate(activity))
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckInFuture3d() {
        val checkInTime = mTodayAtNoon.plusDays(3)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_in_three_days)
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckInFuture2d() {
        val checkInTime = mTodayAtNoon.plusDays(2)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_in_two_days)
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckInFuture1d() {
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_in_tomorrow)
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckInToday() {
        val checkInTime = mTodayAtNoon
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_in_TEMPLATE,
                itinCardDataHotel.getFallbackCheckInTime(activity))
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckOutFuture4d() {
        val checkInTime = mTodayAtNoon.minusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(4)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val data = makeHotelItinGenerator(itinCardDataHotel).itinCardData
        val text = activity.getString(R.string.itin_card_hotel_summary_check_out_day_TEMPLATE,
                data.getFormattedDetailsCheckOutDate(activity))
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckOutFuture3d() {
        val checkInTime = mTodayAtNoon.minusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(3)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_out_three_days)
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckOutFuture2d() {
        val checkInTime = mTodayAtNoon.minusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(2)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_out_two_days)
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckOutFuture1d() {
        val checkInTime = mTodayAtNoon.minusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(1)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_out_tomorrow)
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckOutToday() {
        val checkInTime = mTodayAtNoon.minusDays(1)
        val checkOutTime = mTodayAtNoon
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val text = activity.getString(R.string.itin_card_hotel_summary_check_out_TEMPLATE,
                itinCardDataHotel.getFallbackCheckOutTime(activity))
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testSummaryCheckOutYesterday() {
        val checkInTime = mTodayAtNoon.minusDays(3)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val text = activity.getString(R.string.itin_card_hotel_summary_checked_out_day_TEMPLATE,
                happyItinCardDataHotel.getFormattedDetailsCheckOutDate(activity))
        val summaryText = getSummaryText(checkInTime, checkOutTime)
        Assert.assertEquals(summaryText, text)
    }

    @Test
    fun testIsDurationLongerThenInputsIsLonger() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(10)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        assertTrue(hotelItinGenerator.isDurationLongerThanDays(3))

    }

    @Test
    fun testIsDurationLongerThenInputIsShorter() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val checkInTime = mTodayAtNoon.plusDays(4)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        assertFalse { hotelItinGenerator.isDurationLongerThanDays(3) }

    }

    @Test
    fun testCheckinNotificationExpirationTime() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateGetReadyNotification()

    }

    @Test
    fun getReadyNotificationNotDoesShowTripsThreeDaysOrMoreForNotBucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, Mockito.times(0)).generateGetReadyNotification()

    }

    @Test
    fun getReadyNotificationDoesNotShowTripsWithTwoOrLessTravelers() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, never()).generateGetReadyNotification()
    }

    @Test
    fun getReadyNotificationDoesShowTripsWithMoreThanTwoTravelers() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateGetReadyNotification()
    }

    @Test
    fun getReadyNotificationDoesNotShowTripsWithMoreThanTwoTravelersForNotBuckted() {
        AbacusTestUtils.unbucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, Mockito.times(0)).generateGetReadyNotification()
    }

    @Test
    fun activityCrossNotificationDoesNotShowTripsLessThanThreeDays() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityCrossSellNotification()
    }

    @Test
    fun activityCrossNotificationDoesNotShowTripsThreeDaysOrMoreForNotBucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val checkInTime = mTodayAtNoon.plusDays(1)
        val checkOutTime = mTodayAtNoon.plusDays(5)
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, Mockito.times(0)).generateActivityCrossSellNotification()
    }

    @Test
    fun activityCrossNotificationDoesNotShowTripsWithTwoOrLessTravelers() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val itinCardDataHotel = givenHappyItinCardDataHotel(2)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, never()).generateActivityCrossSellNotification()
    }

    @Test
    fun nullCheckInAndOutForNotificationsTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityCrossSellNotification()
    }

    @Test
    fun activityCrossNotificationDoesNotShowWhenNotBuckted() {
        AbacusTestUtils.unbucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
        verify(hotelItinGenerator, Mockito.times(0)).generateActivityCrossSellNotification()
    }

    @Test
    fun activityInTripNotificationDoesShowTripsWhenBucketed() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidLXNotifications)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(3, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityInTripNotification()
    }

    @Test
    fun activityInTripNotificationDoesNotShowWhenNotBuckted() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidLXNotifications)
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(2, notifications.size)
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
        assertEquals(notifications.get(2).triggerTimeMillis, lxNotificationTime.millis)
    }

    @Test
    fun testNotificationExpTimings() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
    fun testCheckoutMorethan2dNotification() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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
        AbacusTestUtils.bucketTests(AbacusUtils.TripsHotelScheduledNotificationsV2)
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

    private fun getBookingChangeUrl(): String {
        return "https://www.expedia.com/trips/547796b5-7839-49d4-a10f-d860966a1396/" +
                "ordernumber/8098107084358/" +
                "orderlinenumber/32001010-8848-4389-956f-25b615826802" +
                "/change?blockHardChange=true&mobileWebView=true"
    }

    private fun getRoomUpgradeWebUrl(): String {
        return "https://www.expedia.com/trips/547796b5-7839-49d4-a10f-d860966a1396/" +
                "roomupgrade"
    }

    private fun makeHotelItinGenerator(itinCardDataHotel: ItinCardDataHotel): HotelItinContentGenerator {
        return HotelItinContentGenerator(activity, itinCardDataHotel, null)
    }

    private fun givenHappyItinCardDataHotel(checkIn: DateTime = DateTime.now().plusDays(2), checkOut: DateTime? = null): ItinCardDataHotel {
        val itinCardDataHotel = ItinCardDataHotelBuilder()
                .withCheckInDate(checkIn)
                .withCheckOutDate(checkOut).build()
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

    private fun getSummaryText(checkInDate: DateTime, checkOutDate: DateTime): String {
        val itinCardDataHotel = givenHappyItinCardDataHotel(checkInDate, checkOutDate)
        val hotelItinGenerator = makeHotelItinGenerator(itinCardDataHotel)
        return hotelItinGenerator.summaryText
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
