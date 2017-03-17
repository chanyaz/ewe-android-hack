package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.FrameLayout
import com.mobiata.android.util.SettingUtils
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

    lateinit private var context: Context
    lateinit private var parentView: ViewGroup
    lateinit private var headerView: View


    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = Robolectric.buildActivity(NewPhoneLaunchActivity::class.java).create().get()
        headerView = LayoutInflater.from(context).inflate(R.layout.snippet_launch_list_header, null)
        parentView = FrameLayout(context)
        givenCustomerSignedOut()
    }

    @After
    fun tearDown() {
        AbacusTestUtils.resetABTests()

        SettingUtils.save(context, R.string.preference_active_itin_on_launch, false)
        SettingUtils.save(context, R.string.preference_show_air_attach_message_on_launch_screen, false)
        SettingUtils.save(context, R.string.preference_member_deal_on_launch_screen, false)
        SettingUtils.save(context, R.string.preference_guest_itin_on_launch, false)
    }

    @Test
    fun itemViewPosition_showingHotels_signedInItin_memberDeals_popularHotels() {
        givenPopularHotelsCardEnabled()
        givenSignedInItinCardEnabled()
        givenPopularHotelsCardEnabled()
        givenMemberDealsCardEnabled()
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.ITIN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.MEMBER_ONLY_DEALS, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.POPULAR_HOTELS, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HEADER_VIEW, fifthPosition)

        val sixthPosition = sut.getItemViewType(5)
        assertEquals(LaunchDataItem.HOTEL_VIEW, sixthPosition)
    }

    @Test
    fun itemViewPosition_showingHotels_guestItin_signInCard_memberDeals_popularHotels() {
        givenSignInCardEnabled()
        givenPopularHotelsCardEnabled()
        givenMemberDealsCardEnabled()
        givenGuestItinCardEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.ITIN_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.POPULAR_HOTELS, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HEADER_VIEW, fifthPosition)

        val sixthPosition = sut.getItemViewType(5)
        assertEquals(LaunchDataItem.HOTEL_VIEW, sixthPosition)
    }

    @Test
    fun itemViewPosition_showing_hotels_airAttach_memberDeals_popularHotels() {
        givenPopularHotelsCardEnabled()
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
        assertEquals(LaunchDataItem.POPULAR_HOTELS, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HEADER_VIEW, fifthPosition)

        val sixthPosition = sut.getItemViewType(5)
        assertEquals(LaunchDataItem.HOTEL_VIEW, sixthPosition)
    }

    @Test
    fun getItemViewType_showingLobView_showingHotels_signedInItin() {
        givenSignedInItinCardEnabled()

        givenSignInCardEnabled()
        givenCustomerSignedIn()
        createSystemUnderTest()
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
    fun getItemViewType_showingPopularHotels_signInCard() {
        givenSignInCardEnabled()
        givenPopularHotelsCardEnabled()
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.POPULAR_HOTELS, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HEADER_VIEW, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fifthPosition)
    }

    @Test
    fun getItemViewType_showingPopularHotels() {
        givenPopularHotelsCardEnabled()
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val thirdPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.POPULAR_HOTELS, thirdPosition)

        val fourthPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, fourthPosition)

        val fifthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fifthPosition)
    }

    @Test
    fun onBindViewHolder_showingSignedInItinCard() {
        givenSignedInItinCardEnabled()
        givenSignInCardEnabled()
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
    fun onBindViewHolder_showingGuestItinCardt() {
        givenGuestItinCardEnabled()
        givenSignInCardEnabled()
        createSystemUnderTest()
        givenCustomerSignedOut()

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = sut
        givenWeHaveCurrentLocationAndHotels()

        val viewHolder = sut.onCreateViewHolder(recyclerView, LaunchDataItem.ITIN_VIEW
        ) as ItinLaunchCard
        sut.onBindViewHolder(viewHolder, 1)

        assertEquals("Have An Upcoming Trip?", viewHolder.firstLine.text.toString())
        assertEquals("Check the status of your existing trip and get updates in the app", viewHolder.secondLine.text.toString())
    }

    @Test
    fun getItemViewType_notShowingItinCard_guestWithTrips() {
        givenGuestItinCardEnabled()
        givenSignInCardEnabled()
        sut = TestLaunchListAdapter(context, headerView, trips = listOf(Trip()))
        sut.onCreateViewHolder(parentView, 0)
        givenCustomerSignedOut()
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
    fun onBindViewHolder_showingPopularHotels() {
        givenPopularHotelsCardEnabled()

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        createSystemUnderTest()
        recyclerView.adapter = sut
        givenWeHaveCurrentLocationAndHotels()

        val viewHolder = sut.onCreateViewHolder(recyclerView, LaunchDataItem.POPULAR_HOTELS) as BigImageLaunchViewHolder
        sut.onBindViewHolder(viewHolder, 1)

        assertEquals("Find Hotels Near You", viewHolder.titleView.text.toString())
        assertEquals("Recommended hotels tonight", viewHolder.subTitleView.text.toString())
    }

    @Test
    fun getItemViewType_showingLobView_showingHotels_noABTest() {
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.HEADER_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HOTEL_VIEW, thirdPosition)
    }

    @Test
    fun getItemViewType_showingHotels_signedInItin() {
        givenSignedInItinCardEnabled()
        givenSignInCardEnabled()
        createSystemUnderTest()
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
        givenSignInCardEnabled()
        givenSignedInItinCardEnabled()
        createSystemUnderTest()
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
        givenSignInCardEnabled()

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
    fun getItemViewType_showingLobView_showingCollectionView_guestItin() {
        givenSignedInItinCardEnabled()
        givenSignInCardEnabled()
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.ITIN_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HEADER_VIEW, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, fifthPosition)
    }

    @Test
    fun getItemViewType_showingLobView_showingCollectionView_signedIn() {
        givenSignInCardEnabled()
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
        givenSignInCardEnabled()
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
        givenSignedInItinCardEnabled()
        createSystemUnderTest(hasTripsInTwoWeeks = false)
        givenWeHaveStaffPicks()

        val mockItineraryManager: ItineraryManager = Mockito.spy(ItineraryManager.getInstance())
        sut.addSyncListener()

        var firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        var secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.HEADER_VIEW, secondPosition)

        var thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.COLLECTION_VIEW, thirdPosition)

        sut.hasTripsInTwoWeeks = true
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

        givenSignedInItinCardEnabled()
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
        givenPopularHotelsCardEnabled()
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()


        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.AIR_ATTACH_VIEW, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.POPULAR_HOTELS, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HEADER_VIEW, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fifthPosition)
    }

    @Test
    fun onBindViewHolder_FullWidthViews() {
        givenSignInCardEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels(numberOfHotels = 6)

        assertViewHolderIsFullSpan(0) // lob view
        assertViewHolderIsFullSpan(1) // sign in view
        assertViewHolderIsFullSpan(2) // header view

        // hotel cells
        assertViewHolderIsFullSpan(3)
        assertViewHolderIsHalfSpan(4)
        assertViewHolderIsHalfSpan(5)
        assertViewHolderIsHalfSpan(6)
        assertViewHolderIsHalfSpan(7)
        assertViewHolderIsFullSpan(8)
    }

    @Test
    fun itemCount_hotelStateOrder_signedIn() {
        givenSignInCardEnabled()
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
        givenSignInCardEnabled()
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
        givenSignInCardEnabled()
        createSystemUnderTest()
        userHasNoInternetConnection(false)

        val expectedCount = 1
        assertEquals(expectedCount, sut.itemCount)
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_NoFlightTrip() {
        givenAirAttachCardEnabled()
        givenPopularHotelsCardEnabled()
        createSystemUnderTest(isCustomerAirAttachedQualified = true, recentAirAttachFlightTrip = null)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()


        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchDataItem.POPULAR_HOTELS, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchDataItem.HEADER_VIEW, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchDataItem.HOTEL_VIEW, fourthPosition)
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
        var dataItems = ArrayList<LaunchDataItem>()
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
        var dataItems = ArrayList<LaunchDataItem>()
        for (i in 0..numberOfStaffPicks - 1) {
            dataItems.add(LaunchCollectionDataItem(CollectionLocation()))
        }
        sut.setListData(dataItems, headerTitle)
    }

    private fun givenWeHaveCurrentLocationAndHotels(numberOfHotels: Int = 5) {
        // show hotels
        val headerTitle = "Recommended Hotels"
        var dataItems = ArrayList<LaunchDataItem>()
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

    private fun createSystemUnderTest(hasTripsInTwoWeeks: Boolean = true, isCustomerAirAttachedQualified: Boolean = true, recentAirAttachFlightTrip: Trip? = Trip()) {
        sut = TestLaunchListAdapter(context, headerView, hasTripsInTwoWeeks, null, isCustomerAirAttachedQualified, recentAirAttachFlightTrip)
        sut.onCreateViewHolder(parentView, 0)
    }


    private fun givenCustomerSignedIn() {
        val mockUser = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(mockUser, context as Activity)
    }

    private fun givenCustomerSignedOut() {
        try {
            User.signOut(context)
        } catch (e: Exception) {
            // note: sign out triggers a notification clean-up which accesses the local DB.
            // As the DB isn't setup for the test it blows. We're just catching this so the test can still run.
        }
    }

    private fun givenSignedInItinCardEnabled() {
        SettingUtils.save(context, R.string.preference_active_itin_on_launch, true)
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppLaunchShowActiveItinCard, 1)
    }

    private fun givenGuestItinCardEnabled() {
        SettingUtils.save(context, R.string.preference_guest_itin_on_launch, true)
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppLaunchShowGuestItinCard, 1)
    }

    private fun givenMemberDealsCardEnabled() {
        SettingUtils.save(context, R.string.preference_member_deal_on_launch_screen, true)
    }

    private fun givenPopularHotelsCardEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen, 1)
    }

    private fun givenSignInCardEnabled() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen, 1)
    }

    private fun givenAirAttachCardEnabled() {
        SettingUtils.save(context, R.string.preference_show_air_attach_message_on_launch_screen, true)
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen, 1)
    }

    class TestLaunchListAdapter(context: Context?, header: View?, var hasTripsInTwoWeeks: Boolean = true, val trips: List<Trip>? = null, var isCustomerAirAttachedQualified: Boolean = true, var recentAirAttachFlightTrip: Trip? = Trip()) : LaunchListAdapter(context, header) {
        override fun customerHasTripsInNextTwoWeeks(): Boolean {
            return hasTripsInTwoWeeks
        }

        override fun isUserAirAttachQualified(): Boolean {
            return isCustomerAirAttachedQualified
        }

        override fun getRecentUpcomingFlightTrip(): Trip? {
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
