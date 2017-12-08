package com.expedia.bookings.launch.activity

import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.test.CustomMatchers
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestBucketed
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestControl
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PhoneLaunchActivityTest {

    @Test
    fun startingHolidayFunIsTrackedInOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTests(AbacusUtils.HolidayFun)

        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().postCreate(null).resume().get()
        activity.findViewById<View>(R.id.holiday_fun_widget).callOnClick()

        assertLinkTracked("Holiday Promotion", "App.LS.HolidayPromo", withEventsString("event331"), mockAnalyticsProvider)

        activity.finish()

        AbacusTestUtils.unbucketTests(AbacusUtils.HolidayFun)
    }

    @Test
    fun holidayFunNotAvailableIfNotBucketed() {
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().postCreate(null).resume().get()
        assertEquals(View.GONE, activity.findViewById<View>(R.id.holiday_fun_widget).visibility)
        activity.finish()
    }

    @Test
    fun testNotificationClickOmnitureTracking() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val listOfNotificationTypes = listOf(
                Notification.NotificationType.ACTIVITY_START,
                Notification.NotificationType.CAR_DROP_OFF,
                Notification.NotificationType.CAR_PICK_UP,
                Notification.NotificationType.FLIGHT_CHECK_IN,
                Notification.NotificationType.FLIGHT_SHARE,
                Notification.NotificationType.FLIGHT_CANCELLED,
                Notification.NotificationType.FLIGHT_GATE_TIME_CHANGE,
                Notification.NotificationType.FLIGHT_GATE_NUMBER_CHANGE,
                Notification.NotificationType.FLIGHT_BAGGAGE_CLAIM,
                Notification.NotificationType.HOTEL_CHECK_IN,
                Notification.NotificationType.HOTEL_CHECK_OUT,
                Notification.NotificationType.FLIGHT_DEPARTURE_REMINDER,
                Notification.NotificationType.DESKTOP_BOOKING,
                Notification.NotificationType.HOTEL_GET_READY,
                Notification.NotificationType.HOTEL_ACTIVITY_CROSSSEll,
                Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP
        )

        for(notificationType in listOfNotificationTypes) {
            val notification = Notification()
            notification.itinId = notificationType.name
            notification.notificationType = notificationType
            OmnitureTracking.trackNotificationClick(notification)

            val trackingLink: String = OmnitureTracking.setItinNotificationLink(notification)
            assertLinkTrackedWhenNotificationClicked(trackingLink, trackingLink, "event212", mockAnalyticsProvider)
        }

    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsTrackedOnLaunchScreen_whenBucketedAndFeatureToggleEnabled() {
        val context = RuntimeEnvironment.application
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppBrandColors, R.string.preference_enable_launch_screen_brand_colors)
        OmnitureTracking.trackPageLoadLaunchScreen(0)
        assertStateTracked("App.LaunchScreen", withAbacusTestBucketed(15846), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsNotTrackedOnLaunchScreen_whenUnbucketedAndFeatureToggleDisabled() {
        val context = RuntimeEnvironment.application
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppBrandColors, R.string.preference_enable_launch_screen_brand_colors)
        OmnitureTracking.trackPageLoadLaunchScreen(0)

        OmnitureTestUtils.assertStateNotTracked(withAbacusTestBucketed(15846), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateNotTracked(withAbacusTestControl(15846), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsNotTracked_whenBucketedButFeatureNotEnabled() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        OmnitureTracking.trackPageLoadLaunchScreen(0)

        OmnitureTestUtils.assertStateNotTracked(withAbacusTestBucketed(15846), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateNotTracked(withAbacusTestControl(15846), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsTracked_whenNotBucketedButFeatureEnabled() {
        val context = RuntimeEnvironment.application
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        SettingUtils.save(context, R.string.preference_enable_launch_screen_brand_colors, true)
        OmnitureTracking.trackPageLoadLaunchScreen(0)

        assertStateTracked(withAbacusTestControl(15846), mockAnalyticsProvider)
    }

    private fun assertLinkTrackedWhenNotificationClicked(linkName: String, rfrrId: String, event: String, mockAnalyticsProvider: AnalyticsProvider) {
        val expectedData = mapOf(
                "&&linkType" to "o",
                "&&linkName" to linkName,
                "&&v11" to rfrrId,
                "&&events" to event
        )
        Mockito.verify(mockAnalyticsProvider).trackAction(Mockito.eq(linkName), NullSafeMockitoHamcrest.mapThat(CustomMatchers.hasEntries(expectedData)))
    }
}