package com.expedia.bookings.launch.activity

import android.app.Activity
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateNotTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.user.RestrictedProfileSource
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.test.CustomMatchers
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestBucketed
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PhoneLaunchActivityTest {

    @Test
    fun testNotificationClickOmnitureTrackingNoTemplateName() {
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

        for (notificationType in listOfNotificationTypes) {
            val notification = Notification()
            notification.itinId = notificationType.name
            notification.notificationType = notificationType
            OmnitureTracking.trackNotificationClick(notification)

            val trackingLink: String = OmnitureTracking.setItinNotificationLink(notification)
            assertLinkTrackedWhenNotificationClicked(trackingLink, trackingLink, "event212", mockAnalyticsProvider)
        }
    }

    @Test
    fun testNotificationClickOmnitureTrackingWithTemplateName() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val notification = Notification()
        notification.itinId = "testID"
        notification.notificationType = Notification.NotificationType.ACTIVITY_START
        notification.templateName = "templateTest"
        OmnitureTracking.trackNotificationClick(notification)

        val trackingLink: String = OmnitureTracking.setItinNotificationLink(notification)
        assertLinkTrackedWhenNotificationClicked(trackingLink, trackingLink, "event212", mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsTrackedOnLaunchScreen_whenBucketed() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        OmnitureTracking.trackPageLoadLaunchScreen()
        assertStateTracked("App.LaunchScreen", withAbacusTestBucketed(15846), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsNotTrackedOnLaunchScreen_whenUnbucketed() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        OmnitureTracking.trackPageLoadLaunchScreen()
        assertStateNotTracked(withAbacusTestBucketed(15846), mockAnalyticsProvider)
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

    @Test
    fun gotoActivitiesCrossSellForFlightTest() {
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinFlightCardData = ItinCardDataFlightBuilder().build()
        Mockito.`when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinFlightCardData)
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).get()
        activity.gotoActivitiesCrossSell("TEST_ITIN_ID", mockItinManager)
        val shadowActivity = Shadows.shadowOf(activity)
        assertNull(shadowActivity.peekNextStartedActivity())
    }

    @Test
    fun gotoActivitiesCrossSellForNullTest() {
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        Mockito.`when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(null)
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).get()
        activity.gotoActivitiesCrossSell("TEST_ITIN_ID", mockItinManager)
        val shadowActivity = Shadows.shadowOf(activity)
        assertNull(shadowActivity.peekNextStartedActivity())
    }

    @Test
    fun gotoActivitiesCrossSellForHotelTest() {
        val mockItinManager = Mockito.mock(ItineraryManager::class.java)
        val testItinHotelCardData = ItinCardDataHotelBuilder().build()
        Mockito.`when`(mockItinManager.getItinCardDataFromItinId("TEST_ITIN_ID")).thenReturn(testItinHotelCardData)
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).get()
        activity.gotoActivitiesCrossSell("TEST_ITIN_ID", mockItinManager)
        val shadowActivity = Shadows.shadowOf(activity)
        val intent = shadowActivity.peekNextStartedActivity()
        assertTrue(intent.hasExtra("startDateStr"))
        assertTrue(intent.hasExtra("endDateStr"))
        assertTrue(intent.hasExtra("location"))
    }

    class TestAccountSettingsFragment : AccountSettingsFragment() {
        var didCallRefreshUserInfo = false
            private set

        override fun refreshUserInfo() {
            didCallRefreshUserInfo = true
        }
    }

    private class TestRestrictedProfileSource : RestrictedProfileSource(Activity()) {
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
