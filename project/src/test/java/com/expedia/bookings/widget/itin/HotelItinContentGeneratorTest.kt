package com.expedia.bookings.widget.itin

import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.Property
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.spy
import org.mockito.Mockito.never
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class HotelItinContentGeneratorTest {

    lateinit private var activity: NewPhoneLaunchActivity

    lateinit var mTodayAtNoon: DateTime

    @Before
    fun before() {
        mTodayAtNoon = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0)
        activity = Robolectric.buildActivity(NewPhoneLaunchActivity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
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
    fun testDoesCrossSellNotificationsFireAtRightTime(){
        val checkInTime = mTodayAtNoon.plusDays(10)
        val testTime = checkInTime.minusDays(7)
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
    fun activityCrossNotificationDoesShowTripsWithMoreThanTwoTravelers() {
        val itinCardDataHotel = givenHappyItinCardDataHotel(3)
        val hotelItinGenerator = spy(makeHotelItinGenerator(itinCardDataHotel))
        val notifications = hotelItinGenerator.generateNotifications()
        assertEquals(4, notifications.size)
        verify(hotelItinGenerator, Mockito.times(1)).generateActivityCrossSellNotification()

    }

    @Test
    fun testCheckinNotification() {
        val checkInTime = mTodayAtNoon.minusDays(3)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notification = makeHotelItinGenerator(happyItinCardDataHotel).generateNotifications().get(0)
        assertEquals(notification.title, "Hotel check in reminder")
        assertTrue(notification.body.contains(" tomorrow. View your booking for details."))
        assertTrue(notification.triggerTimeMillis.equals(checkInTime.minusDays(1).millis))
    }

    @Test
    fun testCheckoutMorethan2dNotification() {
        val checkInTime = mTodayAtNoon.minusDays(4)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notification = makeHotelItinGenerator(happyItinCardDataHotel).generateNotifications().get(1)
        assertTrue(notification.title.contains("Check out at"))
        assertTrue(notification.body.contains("Check out time your hotel booking for " + happyItinCardDataHotel.propertyName))
        assertTrue(notification.triggerTimeMillis.equals(checkOutTime.minusDays(1).millis))

    }

    @Test
    fun testCheckout2dOrLessNotification() {
        val checkInTime = mTodayAtNoon.minusDays(2)
        val checkOutTime = mTodayAtNoon.minusDays(1)
        val happyItinCardDataHotel = givenHappyItinCardDataHotel(checkInTime, checkOutTime)
        val notification = makeHotelItinGenerator(happyItinCardDataHotel).generateNotifications().get(1)
        assertTrue(notification.title.contains("Check out at"))
        assertTrue(notification.body.contains("Check out time your hotel booking for " + happyItinCardDataHotel.propertyName))
        assertTrue(notification.triggerTimeMillis.equals(checkOutTime.minusHours(12).millis))

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
}
