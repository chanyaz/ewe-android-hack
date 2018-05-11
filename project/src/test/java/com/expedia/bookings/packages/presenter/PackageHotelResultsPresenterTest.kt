package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.HotelClientFilterView
import com.expedia.bookings.widget.PackageHotelServerFilterView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageHotelResultsPresenterTest {
    private lateinit var activity: Activity
    private lateinit var packageHotelResultsPresenter: PackageHotelResultsPresenter
    private val context = RuntimeEnvironment.application
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
    }

    @Test
    fun testMapDetailedPriceNotVisible() {
        packageHotelResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_results_presenter,
                null) as PackageHotelResultsPresenter

        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsList())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsMap())
        assertEquals(View.VISIBLE, packageHotelResultsPresenter.mapPriceIncludesTaxesBottomMessage.visibility)
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPriceIncludesTaxesTopMessage.visibility)
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPricePerPersonMessage.visibility)
    }

    @Test
    fun testMapDetailedPriceVisible() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        packageHotelResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_results_presenter,
                null) as PackageHotelResultsPresenter

        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsList())
        packageHotelResultsPresenter.showWithTracking(BaseHotelResultsPresenter.ResultsMap())
        assertEquals(View.GONE, packageHotelResultsPresenter.mapPriceIncludesTaxesBottomMessage.visibility)
        assertEquals(View.VISIBLE, packageHotelResultsPresenter.mapPriceIncludesTaxesTopMessage.visibility)
        assertEquals(View.VISIBLE, packageHotelResultsPresenter.mapPricePerPersonMessage.visibility)
    }

    @Test
    fun testServerSideFilterDoneButtonTracking() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        packageHotelResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_results_presenter,
                null) as PackageHotelResultsPresenter
        assertTrue(packageHotelResultsPresenter.filterView is PackageHotelServerFilterView)
        packageHotelResultsPresenter.filterView.doneButton.performClick()
        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Filter.Apply")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    fun testClientSideFilterDoneButtonTracking() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        packageHotelResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_results_presenter,
                null) as PackageHotelResultsPresenter

        assertTrue(packageHotelResultsPresenter.filterView is HotelClientFilterView)
        packageHotelResultsPresenter.filterView.doneButton.performClick()
        val expectedEvars = mapOf(28 to "App.Package.Hotels.Search.Filter.Apply")

        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }
}
