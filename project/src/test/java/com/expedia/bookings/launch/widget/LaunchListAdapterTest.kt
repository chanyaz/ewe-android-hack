package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.ProWizardBucketCache
import com.expedia.bookings.widget.FrameLayout
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.launch.SignInPlaceHolderViewModel
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class LaunchListAdapterTest {

    lateinit private var sut: TestLaunchListAdapter
    lateinit private var notificationManager: NotificationManager

    lateinit private var context: Context
    lateinit private var parentView: ViewGroup
    lateinit private var headerView: View


    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()
        headerView = LayoutInflater.from(context).inflate(R.layout.snippet_launch_list_header, null)
        parentView = FrameLayout(context)
        notificationManager = NotificationManager(context)
        givenCustomerSignedOut()
    }

    @After
    fun tearDown() {
        AbacusTestUtils.resetABTests()
    }

    @Test
    fun itemViewPosition_showingHotels_signedInItin_memberDeals__last_minute_deals() {
        givenMemberDealsCardEnabled()
        givenLastMinuteDealIsEnabled()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.ITIN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.LAST_MINUTE_DEALS, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HEADER_VIEW, fifthPosition)

        val sixthPosition = sut.getItemViewType(5)
        assertEquals(LaunchDataItem.HOTEL_VIEW, sixthPosition)

    }

    @Test
    fun itemViewPosition_showingHotels_signInCard_memberDeals() {
        givenMemberDealsCardEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fourthPosition)
    }

    @Test
    fun itemViewPosition_showing_hotels_airAttach_memberDeals() {
        givenAirAttachCardEnabled()
        givenMemberDealsCardEnabled()
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.AIR_ATTACH_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HEADER_VIEW, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fifthPosition)
    }

    @Test
    fun getItemViewType_showingLobView_showingHotels_signedInItin() {
        givenCustomerSignedIn()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.ITIN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fourthPosition)
    }

    @Test
    fun getItemViewType_showingPopularHotels() {
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fourthPosition)
    }

    @Test
    fun onBindViewHolder_showingSignedInItinCard() {
        createSystemUnderTest()
        givenCustomerSignedIn()

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        createSystemUnderTest()
        recyclerView.adapter = sut
        givenWeHaveCurrentLocationAndHotels()

        val viewHolder = sut.onCreateViewHolder(recyclerView, LaunchDataItem.ITIN_VIEW) as ItinLaunchCard
        sut.onBindViewHolder(viewHolder, 1)

        assertEquals("You Have An Upcoming Trip!", viewHolder.firstLine.text.toString())
        assertEquals("Access your itineraries on the go and stay up to date on changes", viewHolder.secondLine.text.toString())
    }

    @Test
    fun getItemViewType_showingLobView_showingHotels_noABTest() {
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fourthPosition)
    }

    @Test
    fun getItemViewType_showingHotels_signedInItin() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.ITIN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fourthPosition)
    }

    @Test
    fun getItemViewType_ShowingHotels_CustomerSignedIn_ActiveItin_AirAttach() {
        givenAirAttachCardEnabled()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.ITIN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.AIR_ATTACH_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HEADER_VIEW, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fifthPosition)
    }

    @Test
    fun getItemViewType_showingHotels_showSignInAfterSignOut() {

        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchDataItem.LOB_VIEW, sut.getItemViewType(0))
        assertEquals(LaunchDataItem.HEADER_VIEW, sut.getItemViewType(1))
        assertEquals(LaunchDataItem.HOTEL_VIEW, sut.getItemViewType(2))

        givenCustomerSignedOut()
        givenWeHaveCurrentLocationAndHotels()
        assertEquals(LaunchDataItem.LOB_VIEW, sut.getItemViewType(0))
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, sut.getItemViewType(1))
        assertEquals(LaunchDataItem.HEADER_VIEW, sut.getItemViewType(2))
        assertEquals(LaunchDataItem.HOTEL_VIEW, sut.getItemViewType(3))
    }

    @Test
    fun getItemViewType_showingLobView_showingCollectionView() {
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fourthPosition)
    }

    @Test
    fun getItemViewType_showingLobView_showingCollectionView_signedIn() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.HEADER_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, thirdPosition)
    }

    @Test
    fun getItemViewType_showingLobView_showingLoadingState() {
        createSystemUnderTest()
        givenWeHaveALoadingState()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        // 2..100 should be loading view as we're in a loading state
        val fourthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.LOADING_VIEW, fourthPosition)
    }

    @Test
    fun testItinManagerSyncShowsActiveItin() {
        givenCustomerSignedIn()
        createSystemUnderTest(isItinLaunchCardEnabled = false)
        givenWeHaveStaffPicks()

        val mockItineraryManager: ItineraryManager = Mockito.spy(ItineraryManager.getInstance())
        sut.addSyncListener()

        var firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        var secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.HEADER_VIEW, secondPosition)

        var thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, thirdPosition)

        sut.isItinLaunchCardEnabled = true
        mockItineraryManager.onSyncFinished(ArrayList<Trip>())

        firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.ITIN_VIEW, secondPosition)

        thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fourthPosition)
    }

    @Test
    fun testCardAlreadyShown() {
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        assertFalse(sut.isStaticCardAlreadyShown(LaunchDataItem.ITIN_VIEW))
        assertFalse(sut.isStaticCardAlreadyShown(LaunchDataItem.AIR_ATTACH_VIEW))

        sut.isItinLaunchCardEnabled = true
        givenAirAttachCardEnabled()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        assertTrue(sut.isStaticCardAlreadyShown(LaunchDataItem.ITIN_VIEW))
        assertTrue(sut.isStaticCardAlreadyShown(LaunchDataItem.AIR_ATTACH_VIEW))
    }

    @Test
    fun getItemViewType_ShowingAirAttach() {
        givenAirAttachCardEnabled()
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.AIR_ATTACH_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fourthPosition)
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_AirAttach() {
        givenAirAttachCardEnabled()
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()


        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.AIR_ATTACH_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fourthPosition)
    }

    @Test
    fun onBindViewHolder_FullWidthViews() {
        createSystemUnderTest()
        givenLastMinuteDealIsEnabled()
        givenWeHaveCurrentLocationAndHotels(numberOfHotels = 6)

        assertViewHolderIsFullSpan(0) // lob view
        assertViewHolderIsFullSpan(1) // sign in view
        assertViewHolderIsFullSpan(2)
        assertViewHolderIsFullSpan(3) // header view

        // hotel cells
        assertViewHolderIsFullSpan(4)
        assertViewHolderIsHalfSpan(5)
        assertViewHolderIsHalfSpan(6)
        assertViewHolderIsHalfSpan(7)
        assertViewHolderIsHalfSpan(8)
        assertViewHolderIsFullSpan(9)
    }

    @Test
    fun itemCount_hotelStateOrder_signedIn() {
        createSystemUnderTest()
        val numberOfHotels = 5
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels(numberOfHotels)

        val fixedItemCount = 2 // lob view and header view
        val expectedCount = fixedItemCount + numberOfHotels
        val actualCount = sut.itemCount
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun itemCount_collectionStateOrder_signedIn() {
        createSystemUnderTest()
        val numberOfStaffPicks = 5
        givenCustomerSignedIn()
        givenWeHaveStaffPicks(numberOfStaffPicks)

        val fixedItemCount = 2 // lob view and header view
        val expectedCount = fixedItemCount + numberOfStaffPicks
        assertEquals(expectedCount, sut.itemCount)
    }

    @Test
    fun itemCount_NoInternetConnection() {
        createSystemUnderTest()
        userHasNoInternetConnection(false)

        val expectedCount = 1
        assertEquals(expectedCount, sut.itemCount)
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_NoFlightTrip() {
        givenAirAttachCardEnabled()
        createSystemUnderTest(isCustomerAirAttachedQualified = true, recentAirAttachFlightTrip = null)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()


        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.HEADER_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HOTEL_VIEW, thirdPosition)
    }

    @Test
    fun testSignInPlaceholderCardButtonTexts() {
        createSystemUnderTest()
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = sut

        val viewHolder = sut.onCreateViewHolder(recyclerView, LaunchDataItem.SIGN_IN_VIEW) as SignInPlaceholderCard
        viewHolder.bind(makeSignInPlaceholderViewModel())

        assertEquals(View.GONE, viewHolder.button_one.visibility)
        assertEquals(View.GONE, viewHolder.button_two.visibility)
    }

    // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/6330
    @Test
    fun testDelayedCardsNoInternet() {
        ProWizardBucketCache.cacheBucket(context, 1)
        createSystemUnderTest()
        sut.onHasInternetConnectionChange(false)
        sut.setListData(ArrayList<LaunchDataItem>(), "")

        val list = ArrayList<LaunchDataItem>()
        list.add(LaunchDataItem(LaunchDataItem.ITIN_VIEW))
        assertEquals(0, sut.itemCount, "FAILURE: adapter list must be empty to simulate crash scenario")

        sut.addDelayedStaticCards(list)
        assertEquals(1, sut.itemCount)
        ProWizardBucketCache.cacheBucket(context, ProWizardBucketCache.NO_BUCKET_VALUE)
    }

    private fun makeSignInPlaceholderViewModel(): SignInPlaceHolderViewModel {
        return SignInPlaceHolderViewModel(getBrandForSignInView(),
                context.getString(R.string.earn_rewards_and_unlock_deals), "", "")
    }

    private fun getBrandForSignInView(): String {
        return Phrase.from(context, R.string.shop_as_a_member_TEMPLATE)
                .putOptional("brand", BuildConfig.brand).format().toString()
    }

    private fun assertViewHolderIsFullSpan(position: Int) {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager

        val itemViewType = sut.getItemViewType(position)
        val lobViewHolder = sut.createViewHolder(recyclerView, itemViewType)
        sut.onBindViewHolder(lobViewHolder, position)
        val layoutParams = lobViewHolder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        assertTrue(layoutParams.isFullSpan)
    }

    private fun assertViewHolderIsHalfSpan(position: Int) {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager

        val itemViewType = sut.getItemViewType(position)
        val lobViewHolder = sut.createViewHolder(recyclerView, itemViewType)
        sut.onBindViewHolder(lobViewHolder, position)
        val layoutParams = lobViewHolder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        assertFalse(layoutParams.isFullSpan)
    }

    private fun givenWeHaveALoadingState() {
        val headerTitle = "Loading..."
        val dataItems = ArrayList<LaunchDataItem>()
        for (i in 1..5) {
            dataItems.add(LaunchDataItem(LaunchDataItem.LOADING_VIEW))
        }
        sut.setListData(dataItems, headerTitle)
    }

    private fun userHasNoInternetConnection(isOnline: Boolean) {
        sut.onHasInternetConnectionChange(isOnline)
    }

    private fun givenWeHaveStaffPicks(numberOfStaffPicks: Int = 5) {
        val headerTitle = "Staff picks"
        val dataItems = ArrayList<LaunchDataItem>()
        for (i in 0..numberOfStaffPicks - 1) {
            dataItems.add(LaunchCollectionDataItem(CollectionLocation()))
        }
        sut.setListData(dataItems, headerTitle)
    }

    private fun givenWeHaveCurrentLocationAndHotels(numberOfHotels: Int = 5) {
        // show hotels
        val headerTitle = "Recommended Hotels"
        val dataItems = ArrayList<LaunchDataItem>()
        for (i in 0..numberOfHotels - 1) {
            dataItems.add(LaunchHotelDataItem(createMockHotel()))
        }
        sut.setListData(dataItems, headerTitle)
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

    private fun createSystemUnderTest(isItinLaunchCardEnabled: Boolean = false, isCustomerAirAttachedQualified: Boolean = true, recentAirAttachFlightTrip: Trip? = Trip()) {
        sut = TestLaunchListAdapter(context, headerView, isItinLaunchCardEnabled, null, isCustomerAirAttachedQualified, recentAirAttachFlightTrip)
        sut.onCreateViewHolder(parentView, 0)
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

    private fun givenMemberDealsCardEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppShowMemberPricingCardOnLaunchScreen, 1)
    }

    private fun givenLastMinuteDealIsEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppLastMinuteDeals, 1)
        SettingUtils.save(context, R.string.preference_enable_last_minute_deals, true)
    }

    private fun givenAirAttachCardEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen, 1)
    }

    class TestLaunchListAdapter(context: Context?, header: View?, var isItinLaunchCardEnabled: Boolean = false, val trips: List<Trip>? = null, var isCustomerAirAttachedQualified: Boolean = true, var recentAirAttachFlightTrip: Trip? = Trip()) : LaunchListAdapter(context, header) {

        override fun showActiveItinLaunchScreenCard(): Boolean {
            return isItinLaunchCardEnabled
        }

        override fun isPOSAndBrandAirAttachEnabled(): Boolean {
            return isCustomerAirAttachedQualified
        }

        override fun getUpcomingAirAttachQualifiedFlightTrip(): Trip? {
            return recentAirAttachFlightTrip
        }

        override fun getCustomerTrips(): List<Trip> {
            if (trips == null) {
                return emptyList()
            }

            return trips;
        }
    }

}
