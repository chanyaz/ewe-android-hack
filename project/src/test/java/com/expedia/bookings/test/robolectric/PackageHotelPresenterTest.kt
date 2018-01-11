package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Rule
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals


@RunWith(RobolectricRunner::class)
class PackageHotelPresenterTest {
    lateinit private var widget: PackageHotelPresenter
    lateinit private var activity: Activity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var params: PackageSearchParams
    val context = RuntimeEnvironment.application

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get
    lateinit var hotelResponse: BundleSearchResponse


    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testPackageSearchParamsTracked() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(hotelResponse)
        widget.trackEventSubject.onNext(Unit)

        val expectedEvars = mapOf(
                47 to "PKG|1R|RT|A1|C3|L1"
        )
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)

    }

    @Test
    fun testPackageSearchParamsTrackedWithNewTravelerForm() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(hotelResponse)
        widget.trackEventSubject.onNext(Unit)

        val expectedEvars = mapOf(
                47 to "PKG|1R|RT|A1|C1|YTH1|IL1|IS0"
        )
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testBundleTotalPriceWidgetTopVisibility() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))

        assertEquals(View.VISIBLE, widget.resultsPresenter.bundlePriceWidgetTop.visibility)
        assertEquals("/person", widget.resultsPresenter.bundlePriceWidgetTop.bundlePerPersonText.text)
        assertEquals("$0.00", widget.resultsPresenter.bundlePriceWidgetTop.bundleTotalPrice.text)
        assertEquals("View your trip", widget.resultsPresenter.bundlePriceWidgetTop.bundleTitleText.text)
        assertEquals(View.VISIBLE, widget.resultsPresenter.bundlePriceWidgetTop.bundleInfoIcon.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleWidgetTapTracking() {
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))

        widget.bundleSlidingWidget.bundlePriceWidget.performClick()

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleWidgetTopTapTracking() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        widget.resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(hotelResponse))

        widget.resultsPresenter.bundlePriceWidgetTop.performClick()

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(mapOf(28 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withProps(mapOf(16 to "App.Package.BundleWidget.Tap"))
                , mockAnalyticsProvider)
    }
}
