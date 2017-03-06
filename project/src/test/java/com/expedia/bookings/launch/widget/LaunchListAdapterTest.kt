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
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.PopularHotelsTonightCard
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowResourcesEB::class))
class LaunchListAdapterTest {

    lateinit private var sut: LaunchListAdapter

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

    @Test
    fun getItemViewType_ShowingLobView_ShowingHotels() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()


        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.SIGN_IN_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HOTEL_VIEW.ordinal, fourthPosition)
    }

    @Test
    fun getItemViewType_ShowingPopularHotelsAndSignInCard() {
        
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen, AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen)
        SettingUtils.save(context, R.string.preference_show_popular_hotels_on_launch_screen, true)
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.SIGN_IN_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.POPULAR_HOTELS.ordinal, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, fourthPosition)

        val fifthPosition = sut.getItemViewType(4)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.COLLECTION_VIEW.ordinal, fifthPosition)
    }

    @Test
    fun getItemViewType_ShowingPopularHotels() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen)
        SettingUtils.save(context, R.string.preference_show_popular_hotels_on_launch_screen, true)
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val thirdPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.POPULAR_HOTELS.ordinal, thirdPosition)

        val fourthPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, fourthPosition)

        val fifthPosition = sut.getItemViewType(3)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.COLLECTION_VIEW.ordinal, fifthPosition)
    }

    @Test
    fun getItemViewType_ShowingPopularHotelsWithVariant1() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen,
                AbacusUtils.TripsPopularHotelsVariant.VARIANT1.ordinal)
        SettingUtils.save(context, R.string.preference_show_popular_hotels_on_launch_screen, true)

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        createSystemUnderTest()
        recyclerView.adapter = sut
        givenWeHaveCurrentLocationAndHotels()

        val viewHolder = sut.onCreateViewHolder(recyclerView, LaunchListAdapter.LaunchListViewsEnum.POPULAR_HOTELS.ordinal) as PopularHotelsTonightCard
        sut.onBindViewHolder(viewHolder, 1)

        assertEquals("Find Hotels Near You", viewHolder.firstLineTextView.text.toString())
        assertEquals("Popular Hotels Tonight", viewHolder.secondLineTextView.text.toString())
    }

    @Test
    fun getItemViewType_ShowingPopularHotelsWithVariant2() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen,
                AbacusUtils.TripsPopularHotelsVariant.VARIANT2.ordinal)

        SettingUtils.save(context, R.string.preference_show_popular_hotels_on_launch_screen, true)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        createSystemUnderTest()
        recyclerView.adapter = sut
        givenWeHaveCurrentLocationAndHotels()

        val viewHolder = sut.onCreateViewHolder(recyclerView, LaunchListAdapter.LaunchListViewsEnum.POPULAR_HOTELS.ordinal) as PopularHotelsTonightCard
        sut.onBindViewHolder(viewHolder, 1)

        assertEquals("Popular Hotels Tonight", viewHolder.firstLineTextView.text.toString())
        assertEquals("Find Hotels Near You", viewHolder.secondLineTextView.text.toString())
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingHotels_NoAB_Test() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()


        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HOTEL_VIEW.ordinal, thirdPosition)
    }

    @Test
    fun getItemViewType_ShowingHotels_CustomerSignedIn() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HOTEL_VIEW.ordinal, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HOTEL_VIEW.ordinal, fourthPosition)
    }

    @Test
    fun getItemViewType_ShowingHotels_ShowSignInAfterSignOut() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, sut.getItemViewType(0))
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, sut.getItemViewType(1))
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HOTEL_VIEW.ordinal, sut.getItemViewType(2))

        givenCustomerSignedOut()
        givenWeHaveCurrentLocationAndHotels()
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, sut.getItemViewType(0))
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.SIGN_IN_VIEW.ordinal, sut.getItemViewType(1))
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, sut.getItemViewType(2))
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HOTEL_VIEW.ordinal, sut.getItemViewType(3))
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingCollectionView() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.SIGN_IN_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, thirdPosition)

        val fourthPosition = sut.getItemViewType(3)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.COLLECTION_VIEW.ordinal, fourthPosition)
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingCollectionView_CustomerSignedIn() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.COLLECTION_VIEW.ordinal, thirdPosition)
    }

    @Test
    fun getItemViewType_ShowingLobView_ShowingLoadingState() {
        
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        givenWeHaveALoadingState()

        val firstPosition = sut.getItemViewType(0)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOB_VIEW.ordinal, firstPosition)

        val secondPosition = sut.getItemViewType(1)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.SIGN_IN_VIEW.ordinal, secondPosition)

        val thirdPosition = sut.getItemViewType(2)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.HEADER_VIEW.ordinal, thirdPosition)

        // 2..100 should be loading view as we're in a loading state
        val fourthPosition = sut.getItemViewType(4)
        assertEquals(LaunchListAdapter.LaunchListViewsEnum.LOADING_VIEW.ordinal, fourthPosition)
    }

    @Test
    fun onBindViewHolder_FullWidthViews() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
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
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
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
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
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
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen)
        createSystemUnderTest()
        userHasNoInternetConnection(false)

        val expectedCount = 1
        assertEquals(expectedCount, sut.itemCount)
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
        val loadingListOfNumbers = arrayListOf<Any>(1, 2, 3, 4, 5)
        sut.setListData(loadingListOfNumbers, headerTitle)
    }

    private fun userHasNoInternetConnection(isOnline: Boolean) {
        sut.onHasInternetConnectionChange(isOnline)
    }

    private fun givenWeHaveStaffPicks(numberOfStaffPicks: Int = 5) {
        val collectionList = ArrayList<Any>()
        val headerTitle = "Staff picks"
        var i = 0
        while (i < numberOfStaffPicks) {
            collectionList.add(CollectionLocation())
            i++
        }
        sut.setListData(collectionList, headerTitle)
    }

    private fun givenWeHaveCurrentLocationAndHotels(numberOfHotels: Int = 5) {
        // show hotels
        val headerTitle = "Recommended Hotels"
        val hotelsList = ArrayList<Any>()
        var i = 0
        while (i < numberOfHotels) {
            hotelsList.add(createMockHotel())
            i++
        }
        sut.setListData(hotelsList, headerTitle)
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

    private fun createSystemUnderTest() {
        sut = LaunchListAdapter(context, headerView)
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
}
