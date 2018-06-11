package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class LaunchListLogicTest {

    private lateinit var context: Context
    private lateinit var launchListLogic: LaunchListLogic

    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()
        launchListLogic = LaunchListLogic.getInstance()
        launchListLogic.initialize(context)
    }

    @After
    fun tearDown() {
        AbacusTestUtils.resetABTests()
    }

    @Test
    fun signInCard() {
        assertEquals(true, launchListLogic.showSignInCard())
        givenCustomerSignedIn()
        assertEquals(false, launchListLogic.showSignInCard())
    }

    @Test
    fun mesoHotelAd() {
        assertEquals(false, launchListLogic.showMesoHotelAd())
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.MesoAd, AbacusVariant.ONE.value)
        assertEquals(true, launchListLogic.showMesoHotelAd())
    }

    @Test
    fun activeItin() {
        val testLaunchListLogic = TestLaunchListLogic()
        testLaunchListLogic.initialize(context)
        givenCustomerSignedIn()
        assertEquals(false, testLaunchListLogic.showItinCard())
        // No great way to mock situation for it showing
    }

    @Test
    fun airAttachMessage() {
        assertEquals(false, launchListLogic.showAirAttachMessage())
        val testLaunchListLogic = TestLaunchListLogic()
        testLaunchListLogic.initialize(context)
        givenCustomerSignedIn()
        assertEquals(true, testLaunchListLogic.showAirAttachMessage())
    }

    @Test
    fun memberDeal() {
        assertEquals(false, launchListLogic.showMemberDeal())
        givenCustomerSignedIn()
        assertEquals(true, launchListLogic.showMemberDeal())
    }

    @Test
    fun lastMinuteDeal() {
        assertEquals(false, launchListLogic.showLastMinuteDeal())
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppLastMinuteDeals, AbacusVariant.ONE.value)
        assertEquals(true, launchListLogic.showLastMinuteDeal())
    }

    @Test
    fun mesoDestinationAd() {
        assertEquals(false, launchListLogic.showMesoDestinationAd())
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.MesoAd, AbacusVariant.TWO.value)
        assertEquals(true, launchListLogic.showMesoDestinationAd())
    }

    @Test
    fun earn2xBanner() {
        givenCustomerSignedIn()
        assertEquals(false, launchListLogic.show2XBanner())
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelEarn2xMessaging, AbacusVariant.ONE.value)
        assertEquals(true, launchListLogic.show2XBanner())
    }

    private fun givenCustomerSignedIn() {
        val mockUser = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(mockUser, context as Activity)
    }

    class TestLaunchListLogic : LaunchListLogic() {
        override fun getUpcomingAirAttachQualifiedFlightTrip(): Trip {
            return Trip()
        }
    }
}
