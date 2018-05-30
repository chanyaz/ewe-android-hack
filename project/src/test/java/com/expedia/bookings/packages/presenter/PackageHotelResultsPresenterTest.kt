package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageHotelResultsPresenterTest {
    private lateinit var activity: Activity
    private lateinit var packageHotelResultsPresenter: PackageHotelResultsPresenter
    private val context = RuntimeEnvironment.application

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
}
