package com.expedia.bookings.launch.widget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.features.Features
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.marketing.meso.model.MesoAdResponse
import com.expedia.bookings.marketing.meso.model.MesoDestinationAdResponse
import com.expedia.bookings.marketing.meso.model.MesoHotelAdResponse
import com.expedia.bookings.marketing.meso.vm.MesoDestinationViewModel
import com.expedia.bookings.marketing.meso.vm.MesoHotelAdViewModel
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.test.ExcludeForBrands
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.FeatureTestUtils
import com.expedia.bookings.utils.LaunchNavBucketCache
import com.expedia.bookings.widget.LaunchScreenAddOnHotMIPCard
import com.expedia.bookings.widget.LaunchScreenHotelAttachCard
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.vm.launch.SignInPlaceHolderViewModel
import com.google.android.gms.ads.formats.NativeAd
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
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21), shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class LaunchListAdapterTest {

    private lateinit var adapterUnderTest: TestLaunchListAdapter
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
    fun itemViewPosition_showingMesoHotelAdWithData() {
        givenMesoHotelAdIsEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        adapterUnderTest.initMesoAd()
        adapterUnderTest.updateState()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_HOTEL_AD_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun itemViewPosition_notShowingMesoHotelAdWithoutData() {
        givenMesoHotelAdIsEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val adapterSize = adapterUnderTest.itemCount - 1

        for (i in 0..adapterSize) {
            assertNotEquals(adapterUnderTest.getItemViewType(i), LaunchDataItem.MESO_HOTEL_AD_VIEW)
        }
    }

    @Test
    fun itemViewPosition_showingMesoDestinationAd() {
        givenMesoDestinationAdEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        adapterUnderTest.initMesoAd()
        adapterUnderTest.updateState()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_DESTINATION_AD_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun mesoHotelAdCardIsTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val hotelName = "Really Great Fake Hotel"
        OmnitureTracking.trackMesoHotel(hotelName)

        val expectedEvar = mapOf(28 to "App.LS.MeSo")
        val expectedProp = mapOf(16 to "App.LS.MeSo")
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.MeSo", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.MeSo", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)

        val expectedHotelEvar = mapOf(12 to "App.LS.MeSo.B2P.Ad." + hotelName)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedHotelEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.TRAVELOCITY])
    fun itemViewPosition_ShowingSignedIn_CustomerFirstLaunchCard() {
        givenCustomerSignedIn()
        givenCustomerFirstGuaranteeCardEnabled()
        createSystemUnderTest(isItinLaunchCardEnabled = true)

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.ITIN_VIEW),
                LaunchDataItem(LaunchDataItem.CUSTOMER_FIRST_GUARANTEE),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.TRAVELOCITY])
    fun itemViewPosition_ShowingCustomerFirstLaunchCard() {
        givenCustomerFirstGuaranteeCardEnabled()
        createSystemUnderTest()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.CUSTOMER_FIRST_GUARANTEE),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun itemViewPosition_alwaysShowingMesoLMDSection() {
        givenMesoHotelAdIsEnabled()
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        adapterUnderTest.initMesoAd()
        adapterUnderTest.updateState()

        val results = (0 until adapterUnderTest.itemCount).asSequence()
                .filter { adapterUnderTest.getItemViewType(it) == LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW }

        assertTrue { results.count() == 1 }
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun itemViewPosition_showingHotels_signedInItin_memberDeals__last_minute_deals() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.ITIN_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun itemViewPosition_showingHotels_signInCard_memberDeals() {
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun itemViewPosition_showing_hotels_airAttach_memberDeals() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_showingLobView_showingHotels_signedInItin() {
        givenCustomerSignedIn()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.ITIN_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun getItemViewType_showingPopularHotels() {
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun onBindViewHolder_showingSignedInItinCard() {
        createSystemUnderTest()
        givenCustomerSignedIn()

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        createSystemUnderTest()
        recyclerView.adapter = adapterUnderTest
        givenWeHaveCurrentLocationAndHotels()

        val viewHolder = adapterUnderTest.onCreateViewHolder(recyclerView, LaunchDataItem.ITIN_VIEW) as ItinLaunchCard
        adapterUnderTest.onBindViewHolder(viewHolder, 1)

        assertEquals("You Have An Upcoming Trip!", viewHolder.firstLine.text.toString())
        assertEquals("Access your itineraries on the go and stay up to date on changes", viewHolder.secondLine.text.toString())
    }

    @Test
    fun getItemViewType_showingLobView_showingHotels_noABTest() {
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_showingHotels_signedInItin() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.ITIN_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_ShowingHotels_CustomerSignedIn_ActiveItin_AirAttach() {
        givenCustomerSignedIn()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.ITIN_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_showingHotels_showSignInAfterSignOut() {

        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        var expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)

        givenCustomerSignedOut()
        givenWeHaveCurrentLocationAndHotels()

        expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun getItemViewType_showingLobView_showingCollectionView() {
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_showingLobView_showingCollectionView_signedIn() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun getItemViewType_showingLobView_showingLoadingState() {
        createSystemUnderTest()
        givenWeHaveALoadingState()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LOADING_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testItinManagerSyncShowsActiveItin() {
        givenCustomerSignedIn()
        createSystemUnderTestWithNoRecentHotelAttach(isItinLaunchCardEnabled = false)
        givenWeHaveStaffPicks()

        val mockItineraryManager: ItineraryManager = Mockito.spy(ItineraryManager.getInstance())
        adapterUnderTest.addSyncListener()

        var expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)

        adapterUnderTest.setLaunchListLogic(TestLaunchListLogic(isItinLaunchCardEnabled = true))
        mockItineraryManager.onSyncFinished(ArrayList<Trip>())

        expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.ITIN_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun testCardAlreadyShown() {
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        assertFalse(adapterUnderTest.isStaticCardAlreadyShown(LaunchDataItem.ITIN_VIEW))
        assertFalse(adapterUnderTest.isStaticCardAlreadyShown(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW))

        adapterUnderTest.setLaunchListLogic(TestLaunchListLogic(isItinLaunchCardEnabled = true))
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        assertTrue(adapterUnderTest.isStaticCardAlreadyShown(LaunchDataItem.ITIN_VIEW))
        assertTrue(adapterUnderTest.isStaticCardAlreadyShown(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW))
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_ShowingAirAttach() {
        givenCustomerSignedIn()
        createSystemUnderTest()
        givenWeHaveStaffPicks()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_HidesHotelAttach_GivenPOSIsArgentina() {
        setPOS(PointOfSaleId.ARGENTINA)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_HidesHotelAttach_GivenPOSIsVietnam() {
        setPOS(PointOfSaleId.VIETNAM)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveStaffPicks()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.COLLECTION_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_AirAttach() {
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_MIP_ATTACH_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun onBindViewHolder_FullWidthViews() {
        createSystemUnderTest()
        givenWeHaveCurrentLocationAndHotels(numberOfHotels = 6)

        assertViewHolderIsFullSpan(0) // lob view
        assertViewHolderIsFullSpan(1) // sign in view
        assertViewHolderIsFullSpan(2)
        assertViewHolderIsFullSpan(3) // LMD header view
        assertViewHolderIsFullSpan(4) // collection header

        // hotel cells
        assertViewHolderIsFullSpan(5)
        assertViewHolderIsHalfSpan(6)
        assertViewHolderIsHalfSpan(7)
        assertViewHolderIsHalfSpan(8)
        assertViewHolderIsHalfSpan(9)
        assertViewHolderIsFullSpan(10)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun itemCount_hotelStateOrder_signedIn() {
        createSystemUnderTest()
        val numberOfHotels = 5
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels(numberOfHotels)

        val fixedItemCount = 6 // lob view, hotMip, header view, member deals, lmd header, lmd
        val expectedCount = fixedItemCount + numberOfHotels
        val actualCount = adapterUnderTest.itemCount
        assertEquals(expectedCount, actualCount)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun itemCount_collectionStateOrder_signedIn() {
        createSystemUnderTest()
        val numberOfStaffPicks = 5
        givenCustomerSignedIn()
        givenWeHaveStaffPicks(numberOfStaffPicks)

        val fixedItemCount = 6 // lob view, hotMip, header view, member deals, lmd header, lmd
        val expectedCount = fixedItemCount + numberOfStaffPicks
        assertEquals(expectedCount, adapterUnderTest.itemCount)
    }

    @Test
    fun itemCount_NoInternetConnection() {
        createSystemUnderTest()
        userHasNoInternetConnection(false)

        val expectedCount = 1
        assertEquals(expectedCount, adapterUnderTest.itemCount)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun getItemViewType_ShowingLobView_ShowingPopularHotels_NoFlightTrip() {
        createSystemUnderTest(recentAirAttachFlightTrip = null)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val expectedLaunchDataItemList = listOf(
                LaunchDataItem(LaunchDataItem.LOB_VIEW),
                LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS),
                LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS),
                LaunchDataItem(LaunchDataItem.HEADER_VIEW),
                LaunchDataItem(LaunchDataItem.HOTEL_VIEW))

        assertListCorrectInAdapter(expectedLaunchDataItemList, adapterUnderTest)
    }

    @Test
    fun testSignInPlaceholderCardButtonTexts() {
        createSystemUnderTest()
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapterUnderTest

        val viewHolder = adapterUnderTest.onCreateViewHolder(recyclerView, LaunchDataItem.SIGN_IN_VIEW) as SignInPlaceholderCard
        viewHolder.bind(makeSignInPlaceholderViewModel())

        assertEquals(View.GONE, viewHolder.button_one.visibility)
        assertEquals(View.GONE, viewHolder.button_two.visibility)
    }

    @Test
    fun lastMinuteDealsCardLaunchIsTracked() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        OmnitureTracking.trackLaunchLastMinuteDeal()
        val expectedEvar = mapOf(28 to "App.LS.LastMinuteDeals")
        val expectedProp = mapOf(16 to "App.LS.LastMinuteDeals")
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.LastMinuteDeals", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.LastMinuteDeals", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)
    }

    @Test
    fun earn2xMessagingCardShown() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelEarn2xMessaging)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val secondPosition = adapterUnderTest.getItemViewType(1)
        assertEquals(LaunchDataItem.EARN_2X_MESSAGING_BANNER, secondPosition)
    }

    @Test
    fun earn2xMessagingSignInShown() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelEarn2xMessaging)
        createSystemUnderTest()
        givenCustomerSignedOut()
        givenWeHaveCurrentLocationAndHotels()

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager
        val viewHolder = adapterUnderTest.onCreateViewHolder(recyclerView, LaunchDataItem.SIGN_IN_VIEW) as SignInPlaceholderCard
        adapterUnderTest.onBindViewHolder(viewHolder, 1)
        assertEquals(viewHolder.firstLineTextView.text, context.getString(R.string.launch_screen_sign_in_2x_title))
        assertEquals(viewHolder.secondLineTextView.text, context.getString(R.string.launch_screen_sign_in_2x_subtitle))
        assertEquals(viewHolder.button_one.text, context.getString(R.string.sign_in))
        assertEquals(viewHolder.button_two.text, context.getString(R.string.Create_Account))

        val secondPosition = adapterUnderTest.getItemViewType(1)
        assertEquals(LaunchDataItem.SIGN_IN_VIEW, secondPosition)
    }

    @Test
    fun earn2xMessagingCardNotShownSignedInNotBucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelEarn2xMessaging)
        createSystemUnderTest()
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()
        val secondPosition = adapterUnderTest.getItemViewType(1)
        assertNotEquals(LaunchDataItem.EARN_2X_MESSAGING_BANNER, secondPosition)
    }

    @Test
    fun earn2xMessagingCardNotShownSignedOut() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelEarn2xMessaging)
        createSystemUnderTest()
        givenCustomerSignedOut()
        givenWeHaveCurrentLocationAndHotels()
        val secondPosition = adapterUnderTest.getItemViewType(1)
        assertNotEquals(LaunchDataItem.EARN_2X_MESSAGING_BANNER, secondPosition)
    }

    @Test
    fun brandHeaderIsShown_givenUserIsBucketedIntoBottomNavBar() {
        LaunchNavBucketCache.cacheBucket(context, 1)
        createSystemUnderTest()

        val firstPosition = adapterUnderTest.getItemViewType(0)
        assertEquals(LaunchDataItem.BRAND_HEADER, firstPosition)
    }

    @Test
    fun brandHeaderIsHidden_givenUserIsNotBucketedIntoBottomNavBar() {
        LaunchNavBucketCache.cacheBucket(context, 0)
        createSystemUnderTest()

        val firstPosition = adapterUnderTest.getItemViewType(0)
        assertEquals(LaunchDataItem.LOB_VIEW, firstPosition)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun hotelAttachRedesignCardIsShown_givenFeatureIsEnabled() {
        givenHotelAttachCardEnabled()
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val layoutManager = LinearLayoutManager(context)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager

        val itemViewType = adapterUnderTest.getItemViewType(2)
        val viewHolder = adapterUnderTest.createViewHolder(recyclerView, itemViewType)
        assertTrue(viewHolder is LaunchScreenAddOnHotMIPCard)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun genericHotelAttachCardIsShown_givenFeatureIsNotEnabled() {
        createSystemUnderTest(isItinLaunchCardEnabled = true)
        givenCustomerSignedIn()
        givenWeHaveCurrentLocationAndHotels()

        val layoutManager = LinearLayoutManager(context)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager

        val itemViewType = adapterUnderTest.getItemViewType(2)
        val viewHolder = adapterUnderTest.createViewHolder(recyclerView, itemViewType)
        assertTrue(viewHolder is LaunchScreenHotelAttachCard)
    }

    private fun assertListCorrectInAdapter(expectedList: List<LaunchDataItem>, adapterUnderTest: TestLaunchListAdapter) {
        expectedList.forEachIndexed { index, launchDataItem ->
            assertEquals(launchDataItem.getKey(), adapterUnderTest.getItemViewType(index))
        }
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

        val itemViewType = adapterUnderTest.getItemViewType(position)
        val lobViewHolder = adapterUnderTest.createViewHolder(recyclerView, itemViewType)
        adapterUnderTest.onBindViewHolder(lobViewHolder, position)
        val layoutParams = lobViewHolder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        assertTrue(layoutParams.isFullSpan)
    }

    private fun assertViewHolderIsHalfSpan(position: Int) {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = layoutManager

        val itemViewType = adapterUnderTest.getItemViewType(position)
        val lobViewHolder = adapterUnderTest.createViewHolder(recyclerView, itemViewType)
        adapterUnderTest.onBindViewHolder(lobViewHolder, position)
        val layoutParams = lobViewHolder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        assertFalse(layoutParams.isFullSpan)
    }

    private fun givenWeHaveALoadingState() {
        val headerTitle = "Loading..."
        val dataItems = ArrayList<LaunchDataItem>()
        for (i in 1..5) {
            dataItems.add(LaunchDataItem(LaunchDataItem.LOADING_VIEW))
        }
        adapterUnderTest.setListData(dataItems, headerTitle)
    }

    private fun userHasNoInternetConnection(isOnline: Boolean) {
        adapterUnderTest.onHasInternetConnectionChange(isOnline)
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
        val testLaunchListLogic = TestLaunchListLogic(isItinLaunchCardEnabled, null, recentAirAttachFlightTrip)
        adapterUnderTest = TestLaunchListAdapter(context, headerView, testLaunchListLogic)
        adapterUnderTest.onCreateViewHolder(parentView, 0)
    }

    private fun createSystemUnderTestWithNoRecentHotelAttach(isItinLaunchCardEnabled: Boolean = false, recentAirAttachFlightTrip: Trip? = null) {
        val testLaunchListLogic = TestLaunchListLogic(isItinLaunchCardEnabled, null, recentAirAttachFlightTrip)
        adapterUnderTest = TestLaunchListAdapter(context, headerView, testLaunchListLogic)
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

    private fun givenHotelAttachCardEnabled() {
        FeatureTestUtils.enableFeature(context, Features.all.hotMipRedesign)
    }

    private fun givenMesoHotelAdIsEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.MesoAd, AbacusVariant.ONE.value)
    }

    private fun givenMesoDestinationAdEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.MesoAd, AbacusVariant.TWO.value)
    }

    private fun givenJoinRewardsLaunchCardEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.JoinRewardsLaunchCard)
    }

    private fun givenCustomerFirstGuaranteeCardEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.CustomerFirstGuarantee)
    }

    private fun setSystemLanguage(lang: String) {
        Locale.setDefault(Locale(lang))
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(context)
    }

    class TestLaunchListAdapter(context: Context?, header: View?, logic: LaunchListLogic) : LaunchListAdapter(context, header, logic) {
        override fun initMesoAd() {
            if (launchListLogic.showMesoHotelAd()) {
                mesoHotelAdViewModel = MesoHotelAdViewModel(context = context)
                mesoHotelAdViewModel.mesoHotelAdResponse = getMesoAdResponseMockData().HotelAdResponse
            } else if (launchListLogic.showMesoDestinationAd()) {
                mesoDestinationViewModel = MesoDestinationViewModel(context = context)
                mesoDestinationViewModel.mesoDestinationAdResponse = getMesoAdResponseMockData().DestinationAdResponse
            }
        }

        private fun getMesoAdResponseMockData(): MesoAdResponse {
            val mesoHotelAdResponse = MesoHotelAdResponse(object : NativeAd.Image() {
                override fun getDrawable(): Drawable? {
                    return null
                }

                override fun getUri(): Uri? {
                    return Uri.parse("https://images.trvl-media.com/hotels/22000000/21120000/21118500/21118500/985a38ba_z.jpg")
                }

                override fun getScale(): Double {
                    return 0.0
                }
            },
                    "Check out this hotel",
                    "123456",
                    "Really Great Fake Hotel",
                    "$200",
                    "33%",
                    "Ann Arbor, Michigan",
                    "0",
                    "$300")

            val mesoDestinationAdResponse = MesoDestinationAdResponse("", "", "", "", "")
            return MesoAdResponse(mesoHotelAdResponse, mesoDestinationAdResponse)
        }
    }

    class TestLaunchListLogic(var isItinLaunchCardEnabled: Boolean = false, val trips: List<Trip>? = null, var recentAirAttachFlightTrip: Trip? = Trip()) : LaunchListLogic() {
        override fun showItinCard(): Boolean {
            return isItinLaunchCardEnabled
        }

        override fun getUpcomingAirAttachQualifiedFlightTrip(): Trip? {
            return recentAirAttachFlightTrip
        }

        override fun getCustomerTrips(): List<Trip> {
            if (trips == null) {
                return emptyList()
            }
            return trips
        }
    }
}
