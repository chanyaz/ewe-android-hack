package com.expedia.bookings.launch.displaylogic

import android.app.Activity
import android.content.Context
import com.expedia.account.util.NetworkConnectivity
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.launch.widget.LaunchDataItem
import com.expedia.bookings.launch.widget.LaunchListLogic
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.LaunchNavBucketCache
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class LaunchListStateManagerTest {

    private lateinit var context: Context
    private lateinit var sut: LaunchListStateManager
    private lateinit var networkConnectivity: MockNetworkConnectivity
    private lateinit var userLoginStateChangedModel: UserLoginStateChangedModel
    private lateinit var userStateManager: UserStateManager
    private lateinit var launchListLogic: LaunchListLogic
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()
        networkConnectivity = MockNetworkConnectivity()
        notificationManager = NotificationManager(context)
        userLoginStateChangedModel = UserLoginStateChangedModel()
        userStateManager = Ui.getApplication(context).appComponent().userStateManager()
        launchListLogic = LaunchListLogic.getInstance()
        launchListLogic.initialize(context)
        sut = LaunchListStateManager(networkConnectivity, userLoginStateChangedModel, userStateManager, launchListLogic)
        bucketLaunchScreenItem()
        networkConnectivity.networkConnected = true
        givenCustomerSignedOut()
    }

    private fun bucketLaunchScreenItem() {
        LaunchNavBucketCache.cacheBucket(context, 1)
    }

    @Test
    fun testLaunchListStateWhenNoInternetConnection() {
        givenCustomerSignedIn()
        networkConnectivity.networkConnected = false
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is NoInternet)
        Assert.assertEquals("NoInternet", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForNoInternet(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateWhenSignInNoHistory() {
        givenCustomerSignedIn()
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is SignInNoHistory)
        Assert.assertEquals("SignInNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForSignInNoHist(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateWhenSignInNoHistory_2XBanner() {
        givenCustomerSignedIn()
        given2XBannerEnabled()
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is SignInNoHistory)
        Assert.assertEquals("SignInNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForSignInNoHist_2XBanner(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateWhenSignInNoHistory_MesoDestinationAd() {
        givenCustomerSignedIn()
        givenMesoDestinationAdEnabled()
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is SignInNoHistory)
        Assert.assertEquals("SignInNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForSignInNoHist_MesoDestinationAd(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateWhenSignInNoHistory_MesoHotelAd() {
        givenCustomerSignedIn()
        givenMesoHotelAdEnabled()
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is SignInNoHistory)
        Assert.assertEquals("SignInNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForSignInNoHist_MesoHotelAd(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateWhenGuestNoHistory() {
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is GuestNoHistory)
        Assert.assertEquals("GuestNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForGuestNoHist(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateChangedWhenInternetConnectionChanged() {
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is GuestNoHistory)
        Assert.assertEquals("GuestNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForGuestNoHist(), sut.launchListStateLiveData.value!!.launchItemList)

        networkConnectivity.networkConnected = false
        sut.onHasInternetConnectionChange(false)
        Assert.assertTrue(sut.launchListStateLiveData.value is NoInternet)
        Assert.assertEquals("NoInternet", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForNoInternet(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    @Test
    fun testLaunchListStateChangedWhenUserLoginStateChanged() {
        sut.updateLaunchListState()
        Assert.assertTrue(sut.launchListStateLiveData.value is GuestNoHistory)
        Assert.assertEquals("GuestNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForGuestNoHist(), sut.launchListStateLiveData.value!!.launchItemList)

        givenCustomerSignedIn()
        userLoginStateChangedModel.userLoginStateChanged.onNext(true)
        Assert.assertTrue(sut.launchListStateLiveData.value is SignInNoHistory)
        Assert.assertEquals("SignInNoHist", sut.launchListStateLiveData.value!!.trackName)
        assertListItemsEquals(createItemsForSignInNoHist(), sut.launchListStateLiveData.value!!.launchItemList)
    }

    private fun assertListItemsEquals(expectedItems: List<LaunchDataItem>, actualItems: List<LaunchDataItem>) {
        Assert.assertEquals(expectedItems.size, actualItems.size)
        for (i in expectedItems.indices) {
            Assert.assertEquals(expectedItems[i].getKey(), actualItems[i].getKey())
        }
    }

    private fun createItemsForNoInternet(): List<LaunchDataItem> {
        return listOf(
                LaunchDataItem(LaunchDataItem.BRAND_HEADER),
                LaunchDataItem(LaunchDataItem.LOB_VIEW)
        )
    }

    private fun createItemsForGuestNoHist(): List<LaunchDataItem> {
        return listOf(
                LaunchDataItem(LaunchDataItem.BRAND_HEADER),
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS)
        )
    }

    private fun createItemsForSignInNoHist(): List<LaunchDataItem> {
        return listOf(
                LaunchDataItem(LaunchDataItem.BRAND_HEADER),
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS)
        )
    }

    private fun createItemsForSignInNoHist_2XBanner(): List<LaunchDataItem> {
        return listOf(
                LaunchDataItem(LaunchDataItem.BRAND_HEADER),
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.EARN_2X_MESSAGING_BANNER),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS)
        )
    }

    private fun createItemsForSignInNoHist_MesoDestinationAd(): List<LaunchDataItem> {
        return listOf(
                LaunchDataItem(LaunchDataItem.BRAND_HEADER),
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_DESTINATION_AD_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS)
        )
    }

    private fun createItemsForSignInNoHist_MesoHotelAd(): List<LaunchDataItem> {
        return listOf(
                LaunchDataItem(LaunchDataItem.BRAND_HEADER),
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_HOTEL_AD_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS)
        )
    }

    private fun givenCustomerSignedIn() {
        val mockUser = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(mockUser, context as Activity)
    }

    private fun givenCustomerSignedOut() {
        try {
            UserStateManager(context, UserLoginStateChangedModel(), notificationManager).signOut()
        } catch (e: Exception) {
        }
    }

    private fun given2XBannerEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelEarn2xMessaging)
    }

    private fun givenMesoHotelAdEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.MesoAd, AbacusVariant.ONE.value)
    }

    private fun givenMesoDestinationAdEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.MesoAd, AbacusVariant.TWO.value)
    }

    private class MockNetworkConnectivity : NetworkConnectivity {
        var networkConnected = true
        override fun isOnline(): Boolean {
            return networkConnected
        }
    }
}
