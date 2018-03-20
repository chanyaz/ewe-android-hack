package com.expedia.bookings.launch.activity

import android.app.Activity
import android.content.Context
import android.support.design.widget.TabLayout
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateNotTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.activity.DeepLinkWebViewActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.user.RestrictedProfileSource
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.fragment.ItinItemListFragment
import com.expedia.bookings.itin.triplist.TripListFragment
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.test.CustomMatchers
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.NullSafeMockitoHamcrest
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestBucketed
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestControl
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.LaunchNavBucketCache
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PhoneLaunchActivityTest {

    private val context = RuntimeEnvironment.application

    @Before
    fun setUp() {
        context.getSharedPreferences("abacus_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

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
                Notification.NotificationType.FLIGHT_DELAYED,
                Notification.NotificationType.HOTEL_REVIEW
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
    fun disableLoginScreenIsTrackedOnLaunchScreen_whenBucketed() {
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().postCreate(null).resume().get()
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(activity, AbacusUtils.DisableSignInPageAsFirstScreen)
        OmnitureTracking.trackPageLoadLaunchScreen(null)
        assertStateTracked("App.LaunchScreen", withAbacusTestBucketed(25030), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsTrackedOnLaunchScreen_whenBucketed() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        OmnitureTracking.trackPageLoadLaunchScreen(null)
        assertStateTracked("App.LaunchScreen", withAbacusTestBucketed(15846), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun topNavigationIsHidden_givenUserIsBucketedIntoBottomNav() {
        LaunchNavBucketCache.cacheBucket(context, 1)
        val startedActivity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start()
        val activity = startedActivity.get()
        assertEquals(View.GONE, activity.toolbar.visibility)
        assertEquals(View.VISIBLE, activity.bottomTabLayout.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun bottomNavigationIsHidden_givenUserIsNotBucketedIntoBottomNav() {
        LaunchNavBucketCache.cacheBucket(context, 0)
        val startedActivity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start()
        val activity = startedActivity.get()
        assertEquals(View.VISIBLE, activity.toolbar.visibility)
        assertEquals(View.GONE, activity.bottomTabLayout.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun bottomNavigationIsHidden_givenUserIsInUnbucketedState() {
        LaunchNavBucketCache.cacheBucket(context, -1)
        val startedActivity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start()
        val activity = startedActivity.get()
        assertEquals(View.VISIBLE, activity.toolbar.visibility)
        assertEquals(View.GONE, activity.bottomTabLayout.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun navigationTabsAreTracked() {
        val startedActivity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start()
        val activity = startedActivity.get()

        selectTabAndAssertTracked(PhoneLaunchActivity.PAGER_POS_ITIN, activity.toolbar.tabLayout, "Trips")
        selectTabAndAssertTracked(PhoneLaunchActivity.PAGER_POS_ACCOUNT, activity.toolbar.tabLayout, "Info")
        selectTabAndAssertTracked(PhoneLaunchActivity.PAGER_POS_LAUNCH, activity.toolbar.tabLayout, "Shop")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun bottomNavTabsAreTracked_givenUserIsBucketedIntoTest() {
        LaunchNavBucketCache.cacheBucket(context, 1)
        val startedActivity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start()
        val activity = startedActivity.get()

        selectTabAndAssertTracked(PhoneLaunchActivity.PAGER_POS_ITIN, activity.bottomTabLayout, "Trips")
        selectTabAndAssertTracked(PhoneLaunchActivity.PAGER_POS_ACCOUNT, activity.bottomTabLayout, "Info")
        selectTabAndAssertTracked(PhoneLaunchActivity.PAGER_POS_LAUNCH, activity.bottomTabLayout, "Shop")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun brandColorsIsNotTrackedOnLaunchScreen_whenUnbucketed() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        OmnitureTracking.trackPageLoadLaunchScreen(null)
        assertStateNotTracked(withAbacusTestBucketed(15846), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.ORBITZ))
    fun testRewardLaunchTileIsTrackedOnOmniture() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        OmnitureTracking.trackPageLoadLaunchScreen(null)
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

    @Test
    fun testNotificationDeepLinkJump() {
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().postCreate(null).resume().get()
        val shadowActivity = Shadows.shadowOf(activity)
        activity.handleNotificationJump(deepLinkUrl = "www.expedia.com")
        val nextIntent = shadowActivity.peekNextStartedActivity()
        assertEquals(DeepLinkWebViewActivity::class.java.name, nextIntent.component.className)
        assertTrue(nextIntent.getStringExtra("ARG_URL").startsWith("www.expedia.com"))
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

    @Test
    fun isTripFoldersEnabledDoesntChange() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripFoldersFragment)
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().get()
        assertTrue(activity.isTripFoldersEnabled)
        AbacusTestUtils.unbucketTests(AbacusUtils.TripFoldersFragment)
        assertTrue(activity.isTripFoldersEnabled)
        AbacusTestUtils.bucketTests(AbacusUtils.TripFoldersFragment)
        assertTrue(activity.isTripFoldersEnabled)
    }

    @Test
    fun testGoToTripList() {
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().get()
        activity.viewPager.currentItem = PhoneLaunchActivity.PAGER_POS_LAUNCH
        activity.goToTripList()
        assertEquals(PhoneLaunchActivity.PAGER_POS_ITIN, activity.viewPager.currentItem)

        activity.viewPager.currentItem = PhoneLaunchActivity.PAGER_POS_ITIN
        activity.goToTripList()
        assertEquals(PhoneLaunchActivity.PAGER_POS_ITIN, activity.viewPager.currentItem)
    }

    @Test
    fun pagerAdapterGetItinItemListFragment() {
        AbacusTestUtils.unbucketTests(AbacusUtils.TripFoldersFragment)
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().get()
        assertTrue(activity.pagerAdapter.getItem(1) is ItinItemListFragment)
    }

    @Test
    fun pagerAdapterGetTripListFragment() {
        AbacusTestUtils.bucketTests(AbacusUtils.TripFoldersFragment)
        val activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().start().get()
        assertTrue(activity.pagerAdapter.getItem(1) is TripListFragment)
    }

    private fun selectTabAndAssertTracked(index: Int, tabLayout: TabLayout, link: String) {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        tabLayout.getTabAt(index)?.select()
        assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Global.$link")), mockAnalyticsProvider)
        assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Global.$link")), mockAnalyticsProvider)
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
