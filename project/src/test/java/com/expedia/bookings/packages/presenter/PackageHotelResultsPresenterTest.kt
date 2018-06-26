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
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.hotel.data.HotelAdapterItem
import com.expedia.bookings.packages.util.PackageServicesManager
import com.expedia.bookings.packages.vm.PackageHotelResultsViewModel
import com.expedia.bookings.packages.widget.PackageHotelServerFilterView
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.AbacusTestUtils
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
        val filterviewmodel = packageHotelResultsPresenter.createFilterViewModel()
        packageHotelResultsPresenter.filterView.initViewModel(filterviewmodel)
        filterviewmodel.userFilterChoices = UserFilterChoices()
        filterviewmodel.userFilterChoices.name = "Test_Hotel"
        filterviewmodel.userFilterChoices.isVipOnlyAccess = true
        filterviewmodel.presetFilterOptionsUpdatedSubject.onNext(filterviewmodel.userFilterChoices)
        packageHotelResultsPresenter.filterView.doneButton.performClick()

        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Filter.Apply")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testServerSideFilterDoneButtonTrackingNotDoneIfFiltersNotSet() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        packageHotelResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_results_presenter,
                null) as PackageHotelResultsPresenter
        assertTrue(packageHotelResultsPresenter.filterView is PackageHotelServerFilterView)
        val filterviewmodel = packageHotelResultsPresenter.createFilterViewModel()
        packageHotelResultsPresenter.filterView.initViewModel(filterviewmodel)
        packageHotelResultsPresenter.filterView.doneButton.performClick()
        OmnitureTestUtils.assertLinkNotTracked("Search Results Sort", "App.Package.Hotels.Search.Filter.Apply", mockAnalyticsProvider)
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

    @Test
    fun testInfiniteScrollListener() {

        val testObserver = TestObserver.create<Unit>()

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering, AbacusVariant.TWO.value)
        inflate()
        packageHotelResultsPresenter.viewModel = PackageHotelResultsViewModel(activity, PackageServicesManager(context, mockPackageServiceRule.services!!))
        packageHotelResultsPresenter.viewModel.nextPageObservable.subscribe(testObserver)
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())

        assertEquals(53, packageHotelResultsPresenter.adapter.itemCount)
        assertEquals(HotelAdapterItem.SPACER, packageHotelResultsPresenter.adapter.getItemViewType(52))

        packageHotelResultsPresenter.endlessScrollListener.onLoadMore(1, packageHotelResultsPresenter.adapter.itemCount, packageHotelResultsPresenter.recyclerView)

        assertEquals(1, testObserver.valueCount())
    }

    @Test
    fun testScrolling() {
        val testObserver = TestObserver.create<Unit>()

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering, AbacusVariant.TWO.value)
        inflate()
        packageHotelResultsPresenter.viewModel = PackageHotelResultsViewModel(activity, PackageServicesManager(context, mockPackageServiceRule.services!!))
        packageHotelResultsPresenter.viewModel.nextPageObservable.subscribe(testObserver)
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())

        packageHotelResultsPresenter.endlessScrollListener.resetState()
        assertFalse(packageHotelResultsPresenter.endlessScrollListener.isLoading())
    }

    @Test
    fun TestNextPageSearchErrorResponseHandler() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering, AbacusVariant.TWO.value)
        inflate()
        packageHotelResultsPresenter.viewModel = PackageHotelResultsViewModel(activity, PackageServicesManager(context, mockPackageServiceRule.services!!))
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())

        packageHotelResultsPresenter.adapter.addInfiniteLoader()
        assertEquals(HotelAdapterItem.LOADING, packageHotelResultsPresenter.adapter.getItemViewType(52))

        packageHotelResultsPresenter.viewModel.nextPageSearchErrorResponseHandler.onNext(Triple(PackageProductSearchType.MultiItemHotels, PackageApiError.Code.mid_could_not_find_results, ApiCallFailing.PackageHotelSearch(PackageApiError.Code.mid_could_not_find_results.name)))
        assertEquals(HotelAdapterItem.SPACER, packageHotelResultsPresenter.adapter.getItemViewType(52))
    }

    @Test
    fun testLastVisibleItemPositionInEndlessScrollListener() {
        inflate()
        packageHotelResultsPresenter.viewModel = PackageHotelResultsViewModel(activity, PackageServicesManager(context, mockPackageServiceRule.services!!))
        packageHotelResultsPresenter.adapter.resultsSubject.onNext(getHotelSearchResponse())

        var lastVisibleItem = packageHotelResultsPresenter.endlessScrollListener.getLastVisibleItem(intArrayOf(0, 1, 2))

        assertEquals(2, lastVisibleItem)

        lastVisibleItem = packageHotelResultsPresenter.endlessScrollListener.getLastVisibleItem(intArrayOf(0))

        assertEquals(0, lastVisibleItem)
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
