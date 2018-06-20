package com.expedia.bookings.launch.orbitz

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.launch.widget.LaunchCollectionDataItem
import com.expedia.bookings.launch.widget.LaunchDataItem
import com.expedia.bookings.launch.widget.LaunchHotelDataItem
import com.expedia.bookings.launch.widget.LaunchListAdapterTest
import com.expedia.bookings.launch.widget.LaunchListLogic
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.model.UserLoginStateChangedModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = [MultiBrand.ORBITZ])
class OrbitzLaunchListAdapterTest {

    private lateinit var adapterUnderTest: LaunchListAdapterTest.TestLaunchListAdapter
    private lateinit var notificationManager: NotificationManager
    private lateinit var context: Context
    private lateinit var parentView: ViewGroup
    private lateinit var headerView: View
    private lateinit var launchListLogic: LaunchListLogic

    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()
        headerView = LayoutInflater.from(context).inflate(R.layout.snippet_launch_list_header, null)
        parentView = FrameLayout(context)
        notificationManager = NotificationManager(context)
        context.getSharedPreferences("abacus_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        LaunchListLogic.getInstance().initialize(context)
        launchListLogic = LaunchListLogic.getInstance()
        givenCustomerSignedOut()
    }

    @After
    fun tearDown() {
        AbacusTestUtils.resetABTests()
    }

    @Test
    fun itemViewPosition_NotShowingRewardLaunchCardInSpanish() {
        setSystemLanguage("es")
        givenCustomerSignedIn()
        createSystemUnderTest()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(3))
    }

    @Test
    fun itemViewPosition_ShowingRewardLaunchCard() {
        givenCustomerSignedIn()
        createSystemUnderTest()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
    }

    @Test
    fun itemViewPosition_ShowingJoinRewardsLaunchCard() {
        givenCustomerSignedIn()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser(LoyaltyMembershipTier.NONE))
        givenJoinRewardsLaunchCardEnabled()
        createSystemUnderTest()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.JOIN_REWARDS_CARD_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
    }

    @Test
    fun itemViewPosition_NotShowingJoinRewardsLaunchCardInSpanish() {
        setSystemLanguage("es")
        givenCustomerSignedIn()
        givenJoinRewardsLaunchCardEnabled()
        createSystemUnderTest()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(3))
    }

    @Test
    fun itemViewPosition_showingHotels_signedInItin_memberDeals__last_minute_deals() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.ITIN_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.LAST_MINUTE_DEALS, adapterUnderTest.getItemViewType(5))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(6))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(7))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(8))
    }

    @Test
    fun itemViewPosition_showing_hotels_airAttach_memberDeals() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(5))
    }

    @Test
    fun getItemViewType_showingLobView_showingHotels_signedInItin() {
        givenCustomerSignedIn()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.ITIN_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(5))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(6))
    }

    @Test
    fun getItemViewType_showingHotels_signedInItin() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.ITIN_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(5))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(6))
    }

    @Test
    fun getItemViewType_ShowingHotels_CustomerSignedIn_ActiveItin_AirAttach() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.ITIN_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(5))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(6))
    }

    @Test
    fun getItemViewType_showingHotels_showSignInAfterSignOut() {

        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(5))

        givenCustomerSignedOut()
        givenWeHaveCurrentLocationAndHotels()
        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(3))
    }

    @Test
    fun getItemViewType_showingLobView_showingCollectionView_signedIn() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.COLLECTION_VIEW, adapterUnderTest.getItemViewType(5))
    }

    @Test
    fun getItemViewType_ShowingAirAttach() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.COLLECTION_VIEW, adapterUnderTest.getItemViewType(5))
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_AirAttach() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(4))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(5))
    }

    @Test
    fun itemCount_hotelStateOrder_signedIn() {
        createSystemUnderTest()
        val numberOfHotels = 5
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels(numberOfHotels)

        val fixedItemCount = 5 // lob view, header view, member deals, rewards card, hot mip
        val expectedCount = fixedItemCount + numberOfHotels
        val actualCount = adapterUnderTest.itemCount
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun itemCount_collectionStateOrder_signedIn() {
        createSystemUnderTest()
        val numberOfStaffPicks = 5
        givenCustomerSignedIn()
        givenWeHaveStaffPicks(numberOfStaffPicks)

        val fixedItemCount = 5 // lob view, header view, member deals, rewards card, hot mip
        val expectedCount = fixedItemCount + numberOfStaffPicks
        assertEquals(expectedCount, adapterUnderTest.itemCount)
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_NoFlightTrip() {
        createSystemUnderTest(recentAirAttachFlightTrip = null)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, adapterUnderTest.getItemViewType(0))
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, adapterUnderTest.getItemViewType(1))
        assertEquals(LaunchDataItem.REWARD_CARD_VIEW, adapterUnderTest.getItemViewType(2))
        assertEquals(LaunchDataItem.HEADER_VIEW, adapterUnderTest.getItemViewType(3))
        assertEquals(LaunchDataItem.HOTEL_VIEW, adapterUnderTest.getItemViewType(4))
    }

    private fun givenWeHaveStaffPicks(numberOfStaffPicks: Int = 5) {
        val headerTitle = "Staff picks"
        val dataItems = ArrayList<LaunchDataItem>()
        for (i in 0..numberOfStaffPicks - 1) {
            dataItems.add(LaunchCollectionDataItem(CollectionLocation()))
        }
        adapterUnderTest.setListData(dataItems, headerTitle)
    }

    private fun givenWeHaveCurrentLocationAndHotels(numberOfHotels: Int = 5) {
        // show hotels
        val headerTitle = "Recommended Hotels"
        val dataItems = ArrayList<LaunchDataItem>()
        for (i in 0..numberOfHotels - 1) {
            dataItems.add(LaunchHotelDataItem(createMockHotel()))
        }
        adapterUnderTest.setListData(dataItems, headerTitle)
    }

    private fun createMockHotel(): Hotel {
        val rate = HotelRate()
        rate.averageRate = 1f
        rate.surchargeTotal = 1f
        rate.surchargeTotalForEntireStay = 1f
        rate.averageBaseRate = 1f
        rate.nightlyRateTotal = 1f
        rate.discountPercent = 1f
        rate.total = 1f
        rate.currencyCode = "USD"
        rate.currencySymbol = "USD"
        rate.discountMessage = ""
        rate.priceToShowUsers = 1f
        rate.strikethroughPriceToShowUsers = 1f
        rate.totalMandatoryFees = 1f
        rate.totalPriceWithMandatoryFees = 1f
        rate.userPriceType = ""
        rate.checkoutPriceType = ""
        rate.roomTypeCode = ""
        rate.ratePlanCode = ""

        val hotel = Hotel()
        hotel.localizedName = "Hotel"
        hotel.lowRateInfo = rate
        hotel.largeThumbnailUrl = ""
        hotel.hotelGuestRating = 5f
        return hotel
    }

    private fun createSystemUnderTest(isItinLaunchCardEnabled: Boolean = false, recentAirAttachFlightTrip: Trip? = Trip()) {
        val testLaunchListLogic = LaunchListAdapterTest.TestLaunchListLogic(isItinLaunchCardEnabled, null, recentAirAttachFlightTrip)
        adapterUnderTest = LaunchListAdapterTest.TestLaunchListAdapter(context, headerView, testLaunchListLogic)
        adapterUnderTest.onCreateViewHolder(parentView, 0)
    }

    private fun givenCustomerSignedIn() {
        val mockUser = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(mockUser, context as Activity)
    }

    private fun givenCustomerSignedOut() {
        try {
            UserStateManager(context, UserLoginStateChangedModel(), notificationManager).signOut()
        } catch (e: Exception) {
            // note: sign out triggers a notification clean-up which accesses the local DB.
            // As the DB isn't setup for the test it blows. We're just catching this so the test can still run.
        }
    }

    private fun givenJoinRewardsLaunchCardEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.JoinRewardsLaunchCard)
    }

    private fun setSystemLanguage(lang: String) {
        Locale.setDefault(Locale(lang))
    }
}
