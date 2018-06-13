package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.packages.util.PackageServicesManager
import com.expedia.bookings.packages.vm.PackageHotelResultsViewModel
import com.expedia.bookings.packages.widget.PackageHotelServerFilterView
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.HotelClientFilterView
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageHotelResultsPresenterTest {
    private lateinit var activity: Activity
    private lateinit var packageHotelResultsPresenter: PackageHotelResultsPresenter
    private val context = RuntimeEnvironment.application
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
    }

    @Test
    fun testMapDetailedPriceNotVisible() {
        inflate()

        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsList())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsMap())
        assertEquals(View.VISIBLE, packageHotelResultsPresenter.mapPriceIncludesTaxesBottomMessage.visibility)
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPriceIncludesTaxesTopMessage.visibility)
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPricePerPersonMessage.visibility)
    }

    @Test
    fun testMapDetailedPriceVisible() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        inflate()

        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsList())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsMap())
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPriceIncludesTaxesBottomMessage.visibility)
        assertEquals(View.VISIBLE, packageHotelResultsPresenter.mapPriceIncludesTaxesTopMessage.visibility)
        assertEquals(View.VISIBLE, packageHotelResultsPresenter.mapPricePerPersonMessage.visibility)
    }

    @Test
    fun testHotelResultsCellOnMapCarouselMapPriceIncludesTaxesBottomMessageNotVisible() {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelResultsCellOnMapCarousel)
        inflate()

        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsList())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsMap())
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPriceIncludesTaxesBottomMessage.visibility)
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPriceIncludesTaxesTopMessage.visibility)
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPricePerPersonMessage.visibility)
    }

    @Test
    fun testFloatingPillFilterButtonClick() {
        inflate()
        packageHotelResultsPresenter.floatingPill.filterButton.performClick()

        assertEquals("Sort & Filter", packageHotelResultsPresenter.filterView.toolbar.title)
    }

    @Test
    fun testFloatingPillToggleViewButtonClickWhenShowMapIsTrue() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        inflate()
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        packageHotelResultsPresenter.floatingPill.toggleViewButton.performClick()

        val controlEvar = mapOf(18 to "App.Package.Hotels.Search.Map")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testFloatingPillToggleViewButtonClickWhenShowMapIsFalse() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        inflate()
        packageHotelResultsPresenter.floatingPill.setToggleState(false)
        packageHotelResultsPresenter.floatingPill.toggleViewButton.performClick()

        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Expand.List")
        OmnitureTestUtils.assertLinkTracked("Search Results Map View", "App.Package.Hotels.Search.Expand.List", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testShouldUsePill() {
        inflate()
        assertFalse(packageHotelResultsPresenter.shouldUsePill())
    }

    @Test
    fun testTrackScrollDepthAndResetScrollTrackingCalledOnDone() {
        inflate()
        val mockedPresenter = spy(packageHotelResultsPresenter)

        mockedPresenter.bindSubscriptionsAndAddListeners()

        mockedPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        mockedPresenter.filterViewModel.doneObservable.onNext(Unit)

        verify(mockedPresenter, atLeastOnce()).trackScrollDepth()
        assertFalse(packageHotelResultsPresenter.hotelScrollListener.hasUserScrolled())
    }

    @Test
    fun testTrackScrollDepthCalledOnHeaderClick() {
        inflate()
        val mockedPresenter = spy(packageHotelResultsPresenter)

        mockedPresenter.bindSubscriptionsAndAddListeners()

        mockedPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        mockedPresenter.pricingHeaderSelectedSubject.onNext(Unit)

        verify(mockedPresenter, atLeastOnce()).trackScrollDepth()
    }

    @Test
    fun testShouldUsePillWhenBucketed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
        inflate()
        assertTrue(packageHotelResultsPresenter.shouldUsePill())
    }

    @Test
    fun testFloatingPillVisibleWithHotelResultsObservable() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)
        inflate()
        packageHotelResultsPresenter.viewModel = PackageHotelResultsViewModel(activity, PackageServicesManager(context, mockPackageServiceRule.services!!))

        packageHotelResultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse, false))

        assertEquals(View.VISIBLE, packageHotelResultsPresenter.floatingPill.visibility)
    }

    @Test
    fun testFloatingPillVisibleWithFilterResultsObservable() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)
        inflate()
        packageHotelResultsPresenter.viewModel = PackageHotelResultsViewModel(activity, PackageServicesManager(context, mockPackageServiceRule.services!!))

        packageHotelResultsPresenter.viewModel.filterResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse, true))

        assertEquals(View.VISIBLE, packageHotelResultsPresenter.floatingPill.visibility)
    }

    @Test
    fun testServerSideFilterDoneButtonTracking() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        inflate()
        assertTrue(packageHotelResultsPresenter.filterView is PackageHotelServerFilterView)
        packageHotelResultsPresenter.filterView.doneButton.performClick()
        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Filter.Apply")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testClientSideFilterDoneButtonTracking() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        inflate()
        assertTrue(packageHotelResultsPresenter.filterView is HotelClientFilterView)
        packageHotelResultsPresenter.filterView.doneButton.performClick()
        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Filter.Apply")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testScrollDepthWhenNullHotel() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        inflate()
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())
        packageHotelResultsPresenter.trackScrollDepth()

        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Scroll.SC=n|RS=5|RV=1")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testScrollDepthWhenNotNullHotel() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        inflate()
        val hotelSearchResponse = getHotelSearchResponse()
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(hotelSearchResponse)
        packageHotelResultsPresenter.trackScrollDepth(1)

        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Scroll.SC=n|RS=5|RV=1|RC=1")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testPackagesHotelScrollListener() {
        inflate()
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())

        packageHotelResultsPresenter.hotelScrollListener.onScrollStateChanged(packageHotelResultsPresenter.recyclerView, RecyclerView.SCROLL_STATE_SETTLING)
        assertFalse(packageHotelResultsPresenter.hotelScrollListener.hasUserScrolled())

        packageHotelResultsPresenter.hotelScrollListener.onScrollStateChanged(packageHotelResultsPresenter.recyclerView, RecyclerView.SCROLL_STATE_DRAGGING)
        assertTrue(packageHotelResultsPresenter.hotelScrollListener.hasUserScrolled())
    }

    private fun inflate() {
        packageHotelResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_results_presenter,
                null) as PackageHotelResultsPresenter
    }

    private fun getHotelSearchResponse(): HotelSearchResponse {
        val bundleSearchResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hoteSearchResponse = HotelSearchResponse()
        hoteSearchResponse.hotelList = bundleSearchResponse.getHotels()
        return hoteSearchResponse
    }
}
