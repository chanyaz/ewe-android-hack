package com.expedia.bookings.launch.activity

import android.app.Activity
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.user.RestrictedProfileSource
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.test.CustomMatchers
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestBucketed
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestControl
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
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
                Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP,
                Notification.NotificationType.FLIGHT_DELAYED
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

    @Test
    fun testRefreshUserInfoCalledWhenAccountTabSelected() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())

        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().postCreate(null).resume().get()
        val settingsFragment = TestAccountSettingsFragment()

        activity.onAccountFragmentAttached(settingsFragment)
        activity.toolbar.tabLayout.getTabAt(PhoneLaunchActivity.PAGER_POS_ACCOUNT)?.select()

        assertTrue(settingsFragment.didCallRefreshUserInfo)

        activity.finish()
    }

    class TestAccountSettingsFragment: AccountSettingsFragment() {
        var didCallRefreshUserInfo = false
            private set

        override fun refreshUserInfo() {
            didCallRefreshUserInfo = true
        }
    }

    private class TestRestrictedProfileSource: RestrictedProfileSource(Activity()) {
        override fun isRestrictedProfile(): Boolean = true
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